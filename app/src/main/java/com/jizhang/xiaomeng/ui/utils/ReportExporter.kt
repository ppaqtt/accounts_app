package com.jizhang.xiaomeng.ui.utils

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

data class ReportTopItem(
    val name: String,
    val amount: Double,
    val percent: Float
)

data class ReportExportData(
    val year: Int,
    val month: Int,
    val income: Double,
    val expense: Double,
    val balance: Double,
    val prevIncome: Double,
    val prevExpense: Double,
    val topCategories: List<ReportTopItem>,
    val dailyExpenses: Map<Int, Double>,
    val firstDayOffset: Int,
    val daysInMonth: Int,
    val summaryLines: List<String>
)

object ReportExporter {

    private const val WIDTH = 1080
    private const val PADDING = 64f
    private const val BG = 0xFFFFFFFF.toInt()
    private const val TITLE_COLOR = 0xFF1F2937.toInt()
    private const val SUBTITLE_COLOR = 0xFF6B7280.toInt()
    private const val ACCENT = 0xFFEF4444.toInt()
    private const val INCOME_COLOR = 0xFF10B981.toInt()
    private const val CARD_BG = 0xFFF8FAFC.toInt()

    fun shareLongImage(context: Context, data: ReportExportData): Boolean {
        return try {
            val bitmap = render(data)
            val dir = File(context.cacheDir, "reports").apply { mkdirs() }
            val file = File(dir, "xiaomeng_report_${data.year}_${data.month + 1}.png")
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            bitmap.recycle()

            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "分享报表长图"))
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun render(data: ReportExportData): Bitmap {
        val topCount = data.topCategories.take(5).size
        val heatRows = if (data.daysInMonth == 0) 0 else
            ((data.firstDayOffset + data.daysInMonth) + 6) / 7
        val height = 80 + 56 + 44 + 60 + 220 + 48 +
                56 + 40 + data.summaryLines.size * 46 + 40 +
                56 + topCount * 76 + 24 +
                56 + 44 + heatRows * 88 + 30 +
                70

        val bitmap = Bitmap.createBitmap(WIDTH, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(BG)

        val titlePaint = textPaint(52f, TITLE_COLOR, bold = true)
        val subPaint = textPaint(32f, SUBTITLE_COLOR)
        val labelPaint = textPaint(30f, SUBTITLE_COLOR)
        val valuePaint = textPaint(40f, TITLE_COLOR, bold = true)
        val sectionPaint = textPaint(38f, TITLE_COLOR, bold = true)
        val bodyPaint = textPaint(32f, TITLE_COLOR)
        val smallPaint = textPaint(26f, SUBTITLE_COLOR)

        var y = 80f
        canvas.drawText("小萌记账 · 月度报告", PADDING, y, titlePaint)
        y += 56f
        canvas.drawText("${data.year}年${data.month + 1}月", PADDING, y, subPaint)
        y += 44f

        // 收支汇总卡片
        val cardRect = RectF(PADDING, y, WIDTH - PADDING, y + 200f)
        canvas.drawRoundRect(cardRect, 28f, 28f, fillPaint(CARD_BG))
        val colWidth = (WIDTH - PADDING * 2) / 3f
        drawMetric(canvas, PADDING + 16f, y + 64f, "支出", money(data.expense), ACCENT, labelPaint, valuePaint)
        drawMetric(canvas, PADDING + colWidth + 16f, y + 64f, "收入", money(data.income), INCOME_COLOR, labelPaint, valuePaint)
        drawMetric(
            canvas,
            PADDING + colWidth * 2 + 16f,
            y + 64f,
            "结余",
            money(data.balance),
            if (data.balance >= 0) INCOME_COLOR else ACCENT,
            labelPaint,
            valuePaint
        )
        y += 240f

        // 月度对比
        canvas.drawText("月度对比", PADDING, y, sectionPaint)
        y += 56f
        val expenseChange = changeText(data.expense, data.prevExpense, "支出")
        val incomeChange = changeText(data.income, data.prevIncome, "收入")
        canvas.drawText(expenseChange, PADDING, y, bodyPaint)
        y += 44f
        canvas.drawText(incomeChange, PADDING, y, bodyPaint)
        y += 44f
        data.summaryLines.forEach { line ->
            canvas.drawText(line, PADDING, y, smallPaint)
            y += 46f
        }
        y += 30f

        // 分类 Top5
        canvas.drawText("分类 Top5", PADDING, y, sectionPaint)
        y += 56f
        if (data.topCategories.isEmpty()) {
            canvas.drawText("本月暂无支出记录", PADDING, y, smallPaint)
            y += 60f
        } else {
            data.topCategories.take(5).forEachIndexed { index, item ->
                canvas.drawText("${index + 1}. ${item.name}", PADDING, y, bodyPaint)
                canvas.drawText(
                    "¥${String.format(Locale.CHINA, "%,.2f", item.amount)}  ${String.format(Locale.CHINA, "%.0f", item.percent)}%",
                    WIDTH - PADDING - 260f,
                    y,
                    bodyPaint
                )
                y += 30f
                val barBg = RectF(PADDING, y, WIDTH - PADDING, y + 12f)
                canvas.drawRoundRect(barBg, 6f, 6f, fillPaint(0xFFE5E7EB.toInt()))
                val barWidth = (WIDTH - PADDING * 2) * (item.percent / 100f).coerceIn(0f, 1f)
                if (barWidth > 0f) {
                    val bar = RectF(PADDING, y, PADDING + barWidth, y + 12f)
                    canvas.drawRoundRect(bar, 6f, 6f, fillPaint(ACCENT))
                }
                y += 46f
            }
        }
        y += 24f

        // 消费日历热力图
        canvas.drawText("消费日历热力图", PADDING, y, sectionPaint)
        y += 44f
        val cellWidth = (WIDTH - PADDING * 2) / 7f
        val cellGap = 8f
        val weekLabels = listOf("一", "二", "三", "四", "五", "六", "日")
        weekLabels.forEachIndexed { index, label ->
            canvas.drawText(
                label,
                PADDING + index * cellWidth + cellWidth / 2f - 12f,
                y,
                smallPaint
            )
        }
        y += 20f

        val maxDaily = data.dailyExpenses.values.maxOrNull() ?: 0.0
        val totalCells = heatRows * 7
        for (cell in 0 until totalCells) {
            val day = cell - data.firstDayOffset + 1
            val col = cell % 7
            val row = cell / 7
            if (day in 1..data.daysInMonth) {
                val amount = data.dailyExpenses[day] ?: 0.0
                val ratio = if (maxDaily > 0.0) (amount / maxDaily).toFloat() else 0f
                val color = heatColor(ratio)
                val left = PADDING + col * cellWidth
                val top = y + row * 88f
                val rect = RectF(left + cellGap / 2, top, left + cellWidth - cellGap / 2, top + 72f)
                canvas.drawRoundRect(rect, 12f, 12f, fillPaint(color))
                val textColor = if (ratio > 0.55f) Color.WHITE else 0xFF374151.toInt()
                val dayPaint = textPaint(28f, textColor)
                val textX = rect.centerX() - dayPaint.measureText(day.toString()) / 2f
                canvas.drawText(day.toString(), textX, rect.centerY() + 10f, dayPaint)
            }
        }
        y += heatRows * 88f + 20f

        canvas.drawText("由小萌记账生成 · ECB 参考汇率", PADDING, y, smallPaint)

        return bitmap
    }

    private fun drawMetric(
        canvas: Canvas,
        x: Float,
        y: Float,
        label: String,
        value: String,
        valueColor: Int,
        labelPaint: Paint,
        valuePaint: Paint
    ) {
        canvas.drawText(label, x, y, labelPaint)
        val paint = Paint(valuePaint).apply { color = valueColor }
        canvas.drawText(value, x, y + 50f, paint)
    }

    private fun changeText(current: Double, previous: Double, label: String): String {
        if (previous <= 0.0) {
            return if (current > 0.0) "$label：本月开始有记录" else "$label：暂无记录"
        }
        val percent = (current - previous) / previous * 100.0
        val direction = if (percent >= 0) "增加" else "减少"
        return "$label：${String.format(Locale.CHINA, "%.0f", kotlin.math.abs(percent))}% $direction（上月 ¥${String.format(Locale.CHINA, "%,.2f", previous)}）"
    }

    private fun money(value: Double): String =
        "¥" + String.format(Locale.CHINA, "%,.2f", value)

    private fun heatColor(ratio: Float): Int {
        val r = (255 + (211 - 255) * ratio).toInt()
        val g = (205 + (47 - 205) * ratio).toInt()
        val b = (210 + (47 - 210) * ratio).toInt()
        return Color.rgb(r, g, b)
    }

    private fun fillPaint(color: Int): Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = color
        style = Paint.Style.FILL
    }

    private fun textPaint(size: Float, color: Int, bold: Boolean = false): Paint =
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = color
            textSize = size
            typeface = if (bold) Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            else Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }
}
