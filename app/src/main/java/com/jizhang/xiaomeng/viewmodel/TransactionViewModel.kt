package com.jizhang.xiaomeng.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.jizhang.xiaomeng.JiZhangBenApplication
import com.jizhang.xiaomeng.data.db.AppDatabase
import com.jizhang.xiaomeng.data.dao.CategorySummary
import com.jizhang.xiaomeng.data.model.Category
import com.jizhang.xiaomeng.data.model.RecurringTransaction
import com.jizhang.xiaomeng.data.model.Transaction
import com.jizhang.xiaomeng.data.model.TransactionType
import com.jizhang.xiaomeng.data.repository.CategoryRepository
import com.jizhang.xiaomeng.data.repository.RecurringTransactionRepository
import com.jizhang.xiaomeng.data.repository.TransactionRepository
import com.jizhang.xiaomeng.ui.utils.AutoBackupManager
import com.jizhang.xiaomeng.ui.utils.CsvUtils
import com.jizhang.xiaomeng.ui.utils.TodayWidgetProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.util.Calendar

class TransactionViewModel(application: Application) : AndroidViewModel(application) {
    private val transactionRepository: TransactionRepository
    private val categoryRepository: CategoryRepository
    private val recurringTransactionRepository: RecurringTransactionRepository

    val allTransactions: Flow<List<Transaction>>
    val allCategories: Flow<List<Category>>
    val allRecurringTransactions: Flow<List<RecurringTransaction>>

    private val calendar = Calendar.getInstance()
    val currentYear = calendar.get(Calendar.YEAR)
    val currentMonth = calendar.get(Calendar.MONTH)

    init {
        val app = application as JiZhangBenApplication
        transactionRepository = app.transactionRepository
        categoryRepository = app.categoryRepository
        recurringTransactionRepository = app.recurringTransactionRepository
        allTransactions = transactionRepository.allTransactions
        allCategories = categoryRepository.allCategories
        allRecurringTransactions = recurringTransactionRepository.allRecurringTransactions
    }

    fun getCategoriesByType(type: TransactionType): LiveData<List<Category>> {
        return categoryRepository.getCategoriesByType(type).asLiveData()
    }

    fun getTransactionsByBook(book: String): Flow<List<Transaction>> {
        return transactionRepository.getTransactionsByBook(book)
    }

    fun getTransactionsByMonth(year: Int, month: Int): LiveData<List<Transaction>> {
        return transactionRepository.getTransactionsByMonth(year, month).asLiveData()
    }

    fun getTransactionsByBookAndMonth(book: String, year: Int, month: Int): LiveData<List<Transaction>> {
        return transactionRepository.getTransactionsByBookAndMonth(book, year, month).asLiveData()
    }

    fun getTransactionsByBookAndDateRange(
        book: String,
        startDate: Long,
        endDate: Long
    ): Flow<List<Transaction>> {
        return transactionRepository.getTransactionsByBookAndDateRange(book, startDate, endDate)
    }

    fun getTransactionsByBookAndYear(book: String, year: Int): Flow<List<Transaction>> {
        return transactionRepository.getTransactionsByBookAndYear(book, year)
    }

    fun getTotalIncomeByMonth(year: Int, month: Int): LiveData<Double?> {
        return transactionRepository.getTotalAmountByTypeAndMonth(TransactionType.INCOME, year, month).asLiveData()
    }

    fun getTotalExpenseByMonth(year: Int, month: Int): LiveData<Double?> {
        return transactionRepository.getTotalAmountByTypeAndMonth(TransactionType.EXPENSE, year, month).asLiveData()
    }

    fun getTotalIncomeByBookAndMonth(book: String, year: Int, month: Int): LiveData<Double?> {
        return transactionRepository.getTotalAmountByTypeAndBookAndMonth(TransactionType.INCOME, book, year, month).asLiveData()
    }

    fun getTotalExpenseByBookAndMonth(book: String, year: Int, month: Int): LiveData<Double?> {
        return transactionRepository.getTotalAmountByTypeAndBookAndMonth(TransactionType.EXPENSE, book, year, month).asLiveData()
    }

    fun getExpenseCategorySummary(year: Int, month: Int): LiveData<List<CategorySummary>> {
        return transactionRepository.getCategorySummaryByTypeAndMonth(TransactionType.EXPENSE, year, month).asLiveData()
    }

    fun getIncomeCategorySummary(year: Int, month: Int): LiveData<List<CategorySummary>> {
        return transactionRepository.getCategorySummaryByTypeAndMonth(TransactionType.INCOME, year, month).asLiveData()
    }

    fun getExpenseCategorySummary(book: String, year: Int, month: Int): LiveData<List<CategorySummary>> {
        return transactionRepository.getCategorySummaryByTypeAndBookAndMonth(TransactionType.EXPENSE, book, year, month).asLiveData()
    }

    fun getIncomeCategorySummary(book: String, year: Int, month: Int): LiveData<List<CategorySummary>> {
        return transactionRepository.getCategorySummaryByTypeAndBookAndMonth(TransactionType.INCOME, book, year, month).asLiveData()
    }

    fun getTotalIncomeByYear(year: Int): LiveData<Double?> {
        return transactionRepository.getTotalAmountByTypeAndYear(TransactionType.INCOME, year).asLiveData()
    }

    fun getTotalExpenseByYear(year: Int): LiveData<Double?> {
        return transactionRepository.getTotalAmountByTypeAndYear(TransactionType.EXPENSE, year).asLiveData()
    }

    fun getTotalIncomeByBookAndYear(book: String, year: Int): LiveData<Double?> {
        return transactionRepository.getTotalAmountByTypeAndBookAndYear(TransactionType.INCOME, book, year).asLiveData()
    }

    fun getTotalExpenseByBookAndYear(book: String, year: Int): LiveData<Double?> {
        return transactionRepository.getTotalAmountByTypeAndBookAndYear(TransactionType.EXPENSE, book, year).asLiveData()
    }

    fun getExpenseCategorySummaryByYear(year: Int): LiveData<List<CategorySummary>> {
        return transactionRepository.getCategorySummaryByTypeAndYear(TransactionType.EXPENSE, year).asLiveData()
    }

    fun getIncomeCategorySummaryByYear(year: Int): LiveData<List<CategorySummary>> {
        return transactionRepository.getCategorySummaryByTypeAndYear(TransactionType.INCOME, year).asLiveData()
    }

    fun getExpenseCategorySummaryByBookAndYear(book: String, year: Int): LiveData<List<CategorySummary>> {
        return transactionRepository.getCategorySummaryByTypeAndBookAndYear(TransactionType.EXPENSE, book, year).asLiveData()
    }

    fun getIncomeCategorySummaryByBookAndYear(book: String, year: Int): LiveData<List<CategorySummary>> {
        return transactionRepository.getCategorySummaryByTypeAndBookAndYear(TransactionType.INCOME, book, year).asLiveData()
    }

    fun insertTransaction(transaction: Transaction) = viewModelScope.launch {
        transactionRepository.insertTransaction(transaction)
        refreshWidget(transaction.bookId)
    }

    fun updateTransaction(transaction: Transaction) = viewModelScope.launch {
        transactionRepository.updateTransaction(transaction)
        refreshWidget(transaction.bookId)
    }

    fun deleteTransaction(transaction: Transaction) = viewModelScope.launch {
        transactionRepository.deleteTransaction(transaction)
        refreshWidget(transaction.bookId)
    }

    fun deleteTransactionsByIds(ids: List<Long>, book: String) = viewModelScope.launch {
        transactionRepository.deleteTransactionsByIds(ids)
        refreshWidget(book)
    }

    fun clearTransactions(book: String) = viewModelScope.launch {
        transactionRepository.deleteTransactionsByBook(book)
        refreshWidget(book)
    }

    fun clearAllTransactions(book: String) = viewModelScope.launch {
        transactionRepository.deleteAllTransactions()
        refreshWidget(book)
    }

    fun renameBookTransactions(oldBook: String, newBook: String) = viewModelScope.launch {
        transactionRepository.renameBookTransactions(oldBook, newBook)
    }

    suspend fun getTransactionById(id: Long): Transaction? {
        return transactionRepository.getTransactionById(id)
    }

    suspend fun getCategoryById(id: Long): Category? {
        return categoryRepository.getCategoryById(id)
    }

    fun insertCategory(category: Category) = viewModelScope.launch {
        categoryRepository.insertCategory(category)
    }

    fun updateCategory(category: Category) = viewModelScope.launch {
        categoryRepository.updateCategory(category)
    }

    fun deleteCategory(category: Category) = viewModelScope.launch {
        categoryRepository.deleteCategory(category)
    }

    /**
     * 删除分类前先把该分类下的账单迁移到同类型的“其他”分类，避免外键级联或删除失败导致数据丢失。
     */
    suspend fun deleteCategorySafely(category: Category): Boolean = withContext(Dispatchers.IO) {
        try {
            val categories = categoryRepository.getAllCategoriesOnce()
            val candidates = categories.filter { it.type == category.type && it.id != category.id }
            val fallback = candidates.firstOrNull { it.name == "其他" }
                ?: candidates.firstOrNull { it.isDefault }
                ?: candidates.firstOrNull()
                ?: Category(
                    name = if (category.name == "其他") "其他分类" else "其他",
                    icon = "more_horiz",
                    type = category.type,
                    isDefault = true
                ).let { newCategory ->
                    newCategory.copy(id = categoryRepository.insertCategory(newCategory))
                }

            transactionRepository.reassignCategory(category.id, fallback.id)
            categoryRepository.deleteCategory(category)
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun exportToCsv(outputStream: OutputStream): Int = withContext(Dispatchers.IO) {
        val transactions = transactionRepository.allTransactions.first()
        val categories = categoryRepository.allCategories.first()
        CsvUtils.exportToCsv(transactions, categories, outputStream)
    }

    suspend fun importFromCsv(inputStream: InputStream, book: String): CsvUtils.ImportResult = withContext(Dispatchers.IO) {
        val categories = categoryRepository.allCategories.first()
        val parsed = CsvUtils.importFromCsv(inputStream, categories)
        val existing = transactionRepository.getTransactionsByBook(book).first()
        val keys = existing.map { it.dedupeKey() }.toMutableSet()

        val toInsert = mutableListOf<Transaction>()
        var duplicateCount = 0
        for (transaction in parsed.transactions) {
            val withBook = transaction.copy(bookId = book)
            if (keys.add(withBook.dedupeKey())) {
                toInsert.add(withBook)
            } else {
                duplicateCount++
            }
        }
        transactionRepository.insertTransactions(toInsert)
        refreshWidget(book)

        parsed.copy(
            transactions = toInsert,
            successCount = toInsert.size,
            failedCount = parsed.failedCount,
            duplicateCount = duplicateCount
        )
    }

    suspend fun backupDatabase(): String? = withContext(Dispatchers.IO) {
        val app = getApplication<JiZhangBenApplication>()
        app.database.openHelper.writableDatabase
            .query("PRAGMA wal_checkpoint(FULL)")
            .use { it.moveToFirst() }
        val dbFile = app.getDatabasePath("jizhangben_database")
        AutoBackupManager(app).performBackup(dbFile)
    }

    suspend fun restoreDatabase(backupFile: File): Boolean = withContext(Dispatchers.IO) {
        val app = getApplication<JiZhangBenApplication>()
        AutoBackupManager(app).stageRestore(backupFile)
    }

    fun refreshWidgetForBook(book: String) = viewModelScope.launch {
        refreshWidget(book)
    }

    fun closeDatabase() {
        (getApplication() as JiZhangBenApplication).database.close()
    }

    private fun Transaction.dedupeKey(): String {
        val day = date.time / 86_400_000L
        return "$type|$amount|$day|${note.trim()}"
    }

    private suspend fun refreshWidget(book: String) {
        val app = getApplication<JiZhangBenApplication>()
        val transactions = transactionRepository.getTransactionsByBook(book).first()
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val start = calendar.timeInMillis
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val end = calendar.timeInMillis

        val today = transactions.filter { it.date.time in start..end }
        val income = today.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
        val expense = today.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
        TodayWidgetProvider.saveTodayData(app, income, expense)
        TodayWidgetProvider.refresh(app)
    }

    // 周期性账单
    fun insertRecurringTransaction(transaction: RecurringTransaction) = viewModelScope.launch {
        recurringTransactionRepository.insert(transaction)
    }

    fun updateRecurringTransaction(transaction: RecurringTransaction) = viewModelScope.launch {
        recurringTransactionRepository.update(transaction)
    }

    fun deleteRecurringTransaction(transaction: RecurringTransaction) = viewModelScope.launch {
        recurringTransactionRepository.delete(transaction)
    }
}
