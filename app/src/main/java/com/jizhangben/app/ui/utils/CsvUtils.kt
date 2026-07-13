package com.jizhangben.app.ui.utils

import com.jizhangben.app.data.model.Category
import com.jizhangben.app.data.model.Transaction
import com.jizhangben.app.data.model.TransactionType
import java.io.InputStream
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CsvUtils {

    private const val CSV_HEADER = "id,type,amount,category_name,note,date,created_at"
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    private val dateOnlyFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    fun exportToCsv(
        transactions: List<Transaction>,
        categories: List<Category>,
        outputStream: OutputStream
    ): Int {
        val categoryMap = categories.associateBy { it.id }
        var count = 0

        OutputStreamWriter(outputStream).use { writer ->
            writer.write(CSV_HEADER)
            writer.write("\n")

            for (transaction in transactions) {
                val categoryName = categoryMap[transaction.categoryId]?.name ?: "未知分类"
                val line = listOf(
                    transaction.id.toString(),
                    transaction.type.name,
                    String.format(Locale.US, "%.2f", transaction.amount),
                    escapeCsv(categoryName),
                    escapeCsv(transaction.note),
                    dateFormat.format(transaction.date),
                    dateFormat.format(transaction.createdAt)
                ).joinToString(",")
                writer.write(line)
                writer.write("\n")
                count++
            }
            writer.flush()
        }
        return count
    }

    fun importFromCsv(
        inputStream: InputStream,
        categories: List<Category>
    ): ImportResult {
        val categoryMap = categories.associateBy { it.name.lowercase(Locale.getDefault()) }
        val transactions = mutableListOf<Transaction>()
        var totalLines = 0
        var successCount = 0
        var failedCount = 0

        val content = inputStream.bufferedReader().use { it.readText() }
        val lines = content.lines().filter { it.isNotBlank() }

        val format = detectCsvFormat(lines)

        var isFirstLine = true
        for (line in lines) {
            val currentLine = line
            if (currentLine.isBlank()) continue

            if (isFirstLine) {
                isFirstLine = false
                if (isHeaderLine(currentLine, format)) {
                    continue
                }
            }

            totalLines++
            try {
                val transaction = parseLine(currentLine, format, categoryMap)
                if (transaction != null) {
                    transactions.add(transaction)
                    successCount++
                } else {
                    failedCount++
                }
            } catch (e: Exception) {
                failedCount++
            }
        }

        return ImportResult(
            transactions = transactions,
            totalLines = totalLines,
            successCount = successCount,
            failedCount = failedCount,
            format = format
        )
    }

    private fun detectCsvFormat(lines: List<String>): CsvFormat {
        if (lines.isEmpty()) return CsvFormat.CUSTOM

        val firstLine = lines.first()

        if (firstLine.contains("交易时间") || firstLine.contains("交易类型") || firstLine.contains("收/支")) {
            return CsvFormat.ALIPAY
        }
        if (firstLine.contains("交易时间") || firstLine.contains("收支类型") || firstLine.contains("支付方式")) {
            return CsvFormat.WECHAT
        }
        if (firstLine.contains("交易时间") || firstLine.contains("交易金额") || firstLine.contains("交易状态")) {
            return CsvFormat.QQ
        }
        if (firstLine.contains("type") && firstLine.contains("amount") && firstLine.contains("category_name")) {
            return CsvFormat.CUSTOM
        }

        return CsvFormat.CUSTOM
    }

    private fun isHeaderLine(line: String, format: CsvFormat): Boolean {
        return when (format) {
            CsvFormat.CUSTOM -> line.contains("type") && line.contains("amount")
            CsvFormat.ALIPAY -> line.contains("交易时间") || line.contains("交易号")
            CsvFormat.WECHAT -> line.contains("交易时间") || line.contains("收支类型")
            CsvFormat.QQ -> line.contains("交易时间") || line.contains("交易金额")
        }
    }

    private fun parseLine(
        line: String,
        format: CsvFormat,
        categoryMap: Map<String, Category>
    ): Transaction? {
        val columns = parseCsvLine(line)
        if (columns.isEmpty()) return null

        return when (format) {
            CsvFormat.CUSTOM -> parseCustomFormat(columns, categoryMap)
            CsvFormat.ALIPAY -> parseAlipayFormat(columns, categoryMap)
            CsvFormat.WECHAT -> parseWechatFormat(columns, categoryMap)
            CsvFormat.QQ -> parseQqFormat(columns, categoryMap)
        }
    }

    private fun parseCustomFormat(
        columns: List<String>,
        categoryMap: Map<String, Category>
    ): Transaction? {
        if (columns.size < 6) return null

        val typeStr = columns.getOrNull(1) ?: return null
        val amountStr = columns.getOrNull(2) ?: return null
        val categoryName = columns.getOrNull(3) ?: return null
        val note = columns.getOrNull(4) ?: ""
        val dateStr = columns.getOrNull(5) ?: return null

        val type = try {
            TransactionType.valueOf(typeStr.uppercase())
        } catch (e: Exception) {
            return null
        }

        val amount = amountStr.replace("¥", "").replace(",", "").trim().toDoubleOrNull() ?: return null
        if (amount <= 0) return null

        val date = try {
            dateFormat.parse(dateStr) ?: Date()
        } catch (e: Exception) {
            try {
                dateOnlyFormat.parse(dateStr) ?: Date()
            } catch (e2: Exception) {
                Date()
            }
        }

        val category = categoryMap[categoryName.lowercase(Locale.getDefault())]
        val categoryId = category?.id ?: run {
            if (type == TransactionType.EXPENSE) {
                categoryMap.values.firstOrNull { it.type == TransactionType.EXPENSE }?.id
            } else {
                categoryMap.values.firstOrNull { it.type == TransactionType.INCOME }?.id
            } ?: -1L
        }

        if (categoryId == -1L) return null

        return Transaction(
            amount = amount,
            type = type,
            categoryId = categoryId,
            note = note,
            date = date
        )
    }

    private fun parseAlipayFormat(
        columns: List<String>,
        categoryMap: Map<String, Category>
    ): Transaction? {
        if (columns.size < 5) return null

        var dateStr = ""
        var amountStr = ""
        var typeStr = ""
        var note = ""
        var status = ""

        for (i in columns.indices) {
            val col = columns[i].trim()
            when {
                col.matches(Regex("\\d{4}-\\d{2}-\\d{2}.*")) -> dateStr = col
                col.contains("¥") || (col.toDoubleOrNull() != null && col.contains(".") && i > 2) -> {
                    if (amountStr.isEmpty()) amountStr = col
                }
                col == "收入" || col == "支出" || col == "不计收支" -> typeStr = col
                status.isEmpty() && (col == "交易成功" || col == "交易关闭" || col == "退款成功") -> status = col
            }
        }

        if (dateStr.isEmpty() || amountStr.isEmpty()) {
            var dateIdx = -1
            var amountIdx = -1
            var typeIdx = -1
            var noteIdx = -1

            for (i in columns.indices) {
                val col = columns[i].trim()
                if (col.matches(Regex("\\d{4}-\\d{2}-\\d{2}.*")) && dateIdx == -1) {
                    dateIdx = i
                } else if (col.contains("¥") && amountIdx == -1) {
                    amountIdx = i
                } else if ((col == "收入" || col == "支出") && typeIdx == -1) {
                    typeIdx = i
                }
            }

            if (dateIdx == -1 || amountIdx == -1) return null

            dateStr = columns[dateIdx].trim()
            amountStr = columns[amountIdx].trim()
            if (typeIdx != -1) typeStr = columns[typeIdx].trim()

            if (amountIdx + 1 < columns.size && noteIdx == -1) {
                noteIdx = amountIdx + 1
                if (noteIdx < columns.size) {
                    note = columns[noteIdx].trim()
                }
            }
        }

        val type = when (typeStr) {
            "收入" -> TransactionType.INCOME
            "支出" -> TransactionType.EXPENSE
            else -> TransactionType.EXPENSE
        }

        val amount = amountStr
            .replace("¥", "")
            .replace(",", "")
            .replace("+", "")
            .replace("-", "")
            .trim()
            .toDoubleOrNull() ?: return null
        if (amount <= 0) return null

        val date = try {
            dateFormat.parse(dateStr) ?: Date()
        } catch (e: Exception) {
            try {
                dateOnlyFormat.parse(dateStr) ?: Date()
            } catch (e2: Exception) {
                Date()
            }
        }

        val defaultCategory = categoryMap.values.firstOrNull { it.type == type }
        val categoryId = defaultCategory?.id ?: -1L
        if (categoryId == -1L) return null

        return Transaction(
            amount = amount,
            type = type,
            categoryId = categoryId,
            note = note.ifEmpty { "支付宝导入" },
            date = date
        )
    }

    private fun parseWechatFormat(
        columns: List<String>,
        categoryMap: Map<String, Category>
    ): Transaction? {
        if (columns.size < 5) return null

        var dateStr = ""
        var typeStr = ""
        var amountStr = ""
        var note = ""

        for (i in columns.indices) {
            val col = columns[i].trim()
            when {
                col.matches(Regex("\\d{4}-\\d{2}-\\d{2}.*")) -> dateStr = col
                col == "收入" || col == "支出" || col == "/ 收入" || col == "/ 支出" -> typeStr = col.replace("/ ", "")
                col.startsWith("¥") || (col.contains(".") && col.replace(",", "").toDoubleOrNull() != null) -> {
                    if (amountStr.isEmpty()) amountStr = col
                }
            }
        }

        if (dateStr.isEmpty() || amountStr.isEmpty()) {
            var dateIdx = -1
            var typeIdx = -1
            var amountIdx = -1

            for (i in columns.indices) {
                val col = columns[i].trim()
                if (col.matches(Regex("\\d{4}-\\d{2}-\\d{2}.*")) && dateIdx == -1) {
                    dateIdx = i
                } else if ((col == "收入" || col == "支出") && typeIdx == -1) {
                    typeIdx = i
                } else if (col.startsWith("¥") && amountIdx == -1) {
                    amountIdx = i
                }
            }

            if (dateIdx == -1 || amountIdx == -1) return null

            dateStr = columns[dateIdx].trim()
            amountStr = columns[amountIdx].trim()
            if (typeIdx != -1) typeStr = columns[typeIdx].trim()

            if (dateIdx + 1 < columns.size) {
                note = columns[dateIdx + 1].trim()
            }
        }

        val type = when (typeStr) {
            "收入" -> TransactionType.INCOME
            "支出" -> TransactionType.EXPENSE
            else -> TransactionType.EXPENSE
        }

        val amount = amountStr
            .replace("¥", "")
            .replace(",", "")
            .trim()
            .toDoubleOrNull() ?: return null
        if (amount <= 0) return null

        val date = try {
            dateFormat.parse(dateStr) ?: Date()
        } catch (e: Exception) {
            try {
                dateOnlyFormat.parse(dateStr) ?: Date()
            } catch (e2: Exception) {
                Date()
            }
        }

        val defaultCategory = categoryMap.values.firstOrNull { it.type == type }
        val categoryId = defaultCategory?.id ?: -1L
        if (categoryId == -1L) return null

        return Transaction(
            amount = amount,
            type = type,
            categoryId = categoryId,
            note = note.ifEmpty { "微信导入" },
            date = date
        )
    }

    private fun parseQqFormat(
        columns: List<String>,
        categoryMap: Map<String, Category>
    ): Transaction? {
        if (columns.size < 4) return null

        var dateStr = ""
        var amountStr = ""
        var note = ""
        var type = TransactionType.EXPENSE

        var dateIdx = -1
        var amountIdx = -1

        for (i in columns.indices) {
            val col = columns[i].trim()
            if (col.matches(Regex("\\d{4}-\\d{2}-\\d{2}.*")) && dateIdx == -1) {
                dateIdx = i
                dateStr = col
            } else if (col.contains("¥") && amountIdx == -1) {
                amountIdx = i
                amountStr = col
            }
        }

        if (dateIdx == -1) {
            for (i in columns.indices) {
                val col = columns[i].trim()
                if (col.matches(Regex("\\d{4}-\\d{2}-\\d{2}.*"))) {
                    dateStr = col
                    dateIdx = i
                    break
                }
            }
        }

        if (amountIdx == -1) {
            for (i in columns.indices) {
                val col = columns[i].trim()
                if (col.startsWith("¥") || (col.contains(".") && col.replace("¥", "").replace(",", "").toDoubleOrNull() != null)) {
                    amountStr = col
                    amountIdx = i
                    break
                }
            }
        }

        if (dateStr.isEmpty() || amountStr.isEmpty()) return null

        if (amountStr.contains("+")) {
            type = TransactionType.INCOME
        }

        val amount = amountStr
            .replace("¥", "")
            .replace(",", "")
            .replace("+", "")
            .replace("-", "")
            .trim()
            .toDoubleOrNull() ?: return null
        if (amount <= 0) return null

        val date = try {
            dateFormat.parse(dateStr) ?: Date()
        } catch (e: Exception) {
            try {
                dateOnlyFormat.parse(dateStr) ?: Date()
            } catch (e2: Exception) {
                Date()
            }
        }

        if (dateIdx + 1 < columns.size && dateIdx + 1 != amountIdx) {
            note = columns[dateIdx + 1].trim()
        }

        val defaultCategory = categoryMap.values.firstOrNull { it.type == type }
        val categoryId = defaultCategory?.id ?: -1L
        if (categoryId == -1L) return null

        return Transaction(
            amount = amount,
            type = type,
            categoryId = categoryId,
            note = note.ifEmpty { "QQ导入" },
            date = date
        )
    }

    private fun parseCsvLine(line: String): List<String> {
        val result = mutableListOf<String>()
        var current = StringBuilder()
        var inQuotes = false
        var i = 0

        while (i < line.length) {
            val c = line[i]
            when {
                c == '"' && inQuotes -> {
                    if (i + 1 < line.length && line[i + 1] == '"') {
                        current.append('"')
                        i++
                    } else {
                        inQuotes = false
                    }
                }
                c == '"' && !inQuotes -> {
                    inQuotes = true
                }
                c == ',' && !inQuotes -> {
                    result.add(current.toString())
                    current = StringBuilder()
                }
                else -> {
                    current.append(c)
                }
            }
            i++
        }
        result.add(current.toString())
        return result
    }

    private fun escapeCsv(value: String): String {
        return if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            "\"${value.replace("\"", "\"\"")}\""
        } else {
            value
        }
    }

    enum class CsvFormat(val displayName: String) {
        CUSTOM("小萌记账"),
        ALIPAY("支付宝"),
        WECHAT("微信"),
        QQ("QQ")
    }

    data class ImportResult(
        val transactions: List<Transaction>,
        val totalLines: Int,
        val successCount: Int,
        val failedCount: Int,
        val format: CsvFormat
    )
}
