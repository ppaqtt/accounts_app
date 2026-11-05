package com.jizhang.xiaomeng.data.repository

import com.jizhang.xiaomeng.data.dao.CategorySummary
import com.jizhang.xiaomeng.data.dao.TransactionDao
import com.jizhang.xiaomeng.data.model.Transaction
import com.jizhang.xiaomeng.data.model.TransactionType
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

class TransactionRepository(private val transactionDao: TransactionDao) {
    val allTransactions: Flow<List<Transaction>> = transactionDao.getAllTransactions()

    fun getTransactionsByType(type: TransactionType): Flow<List<Transaction>> {
        return transactionDao.getTransactionsByType(type)
    }

    fun getTransactionsByBook(book: String): Flow<List<Transaction>> {
        return transactionDao.getTransactionsByBook(book)
    }

    fun getTransactionsByBookAndType(book: String, type: TransactionType): Flow<List<Transaction>> {
        return transactionDao.getTransactionsByBookAndType(book, type)
    }

    fun getTransactionsByMonth(year: Int, month: Int): Flow<List<Transaction>> {
        val (startDate, endDate) = monthRange(year, month)
        return transactionDao.getTransactionsByDateRange(startDate, endDate)
    }

    fun getTransactionsByBookAndMonth(book: String, year: Int, month: Int): Flow<List<Transaction>> {
        val (startDate, endDate) = monthRange(year, month)
        return transactionDao.getTransactionsByBookAndDateRange(book, startDate, endDate)
    }

    fun getTransactionsByBookAndDateRange(book: String, startDate: Long, endDate: Long): Flow<List<Transaction>> {
        return transactionDao.getTransactionsByBookAndDateRange(book, startDate, endDate)
    }

    suspend fun getTransactionById(id: Long): Transaction? {
        return transactionDao.getTransactionById(id)
    }

    suspend fun insertTransaction(transaction: Transaction): Long {
        return transactionDao.insertTransaction(transaction)
    }

    suspend fun insertTransactions(transactions: List<Transaction>) {
        transactionDao.insertTransactions(transactions)
    }

    suspend fun updateTransaction(transaction: Transaction) {
        transactionDao.updateTransaction(transaction)
    }

    suspend fun deleteTransaction(transaction: Transaction) {
        transactionDao.deleteTransaction(transaction)
    }

    suspend fun deleteTransactionsByIds(ids: List<Long>) {
        transactionDao.deleteTransactionsByIds(ids)
    }

    suspend fun deleteTransactionsByBook(book: String) {
        transactionDao.deleteTransactionsByBook(book)
    }

    suspend fun renameBookTransactions(oldBook: String, newBook: String) {
        transactionDao.renameBookTransactions(oldBook, newBook)
    }

    suspend fun deleteAllTransactions() {
        transactionDao.deleteAllTransactions()
    }

    fun getTotalAmountByTypeAndMonth(type: TransactionType, year: Int, month: Int): Flow<Double?> {
        val (startDate, endDate) = monthRange(year, month)
        return transactionDao.getTotalAmountByTypeAndDateRange(type, startDate, endDate)
    }

    fun getTotalAmountByTypeAndBookAndMonth(type: TransactionType, book: String, year: Int, month: Int): Flow<Double?> {
        val (startDate, endDate) = monthRange(year, month)
        return transactionDao.getTotalAmountByTypeAndBookAndDateRange(type, book, startDate, endDate)
    }

    fun getCategorySummaryByTypeAndMonth(type: TransactionType, year: Int, month: Int): Flow<List<CategorySummary>> {
        val (startDate, endDate) = monthRange(year, month)
        return transactionDao.getCategorySummaryByTypeAndDateRange(type, startDate, endDate)
    }

    fun getCategorySummaryByTypeAndBookAndMonth(
        type: TransactionType,
        book: String,
        year: Int,
        month: Int
    ): Flow<List<CategorySummary>> {
        val (startDate, endDate) = monthRange(year, month)
        return transactionDao.getCategorySummaryByTypeAndBookAndDateRange(type, book, startDate, endDate)
    }

    fun getTransactionsByYear(year: Int): Flow<List<Transaction>> {
        val (startDate, endDate) = yearRange(year)
        return transactionDao.getTransactionsByDateRange(startDate, endDate)
    }

    fun getTransactionsByBookAndYear(book: String, year: Int): Flow<List<Transaction>> {
        val (startDate, endDate) = yearRange(year)
        return transactionDao.getTransactionsByBookAndDateRange(book, startDate, endDate)
    }

    fun getTotalAmountByTypeAndYear(type: TransactionType, year: Int): Flow<Double?> {
        val (startDate, endDate) = yearRange(year)
        return transactionDao.getTotalAmountByTypeAndDateRange(type, startDate, endDate)
    }

    fun getTotalAmountByTypeAndBookAndYear(type: TransactionType, book: String, year: Int): Flow<Double?> {
        val (startDate, endDate) = yearRange(year)
        return transactionDao.getTotalAmountByTypeAndBookAndDateRange(type, book, startDate, endDate)
    }

    fun getCategorySummaryByTypeAndYear(type: TransactionType, year: Int): Flow<List<CategorySummary>> {
        val (startDate, endDate) = yearRange(year)
        return transactionDao.getCategorySummaryByTypeAndDateRange(type, startDate, endDate)
    }

    fun getCategorySummaryByTypeAndBookAndYear(
        type: TransactionType,
        book: String,
        year: Int
    ): Flow<List<CategorySummary>> {
        val (startDate, endDate) = yearRange(year)
        return transactionDao.getCategorySummaryByTypeAndBookAndDateRange(type, book, startDate, endDate)
    }

    suspend fun countByCategory(categoryId: Long): Int {
        return transactionDao.countByCategory(categoryId)
    }

    suspend fun reassignCategory(categoryId: Long, targetCategoryId: Long) {
        transactionDao.reassignCategory(categoryId, targetCategoryId)
    }

    private fun monthRange(year: Int, month: Int): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        calendar.set(year, month, 1, 0, 0, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startDate = calendar.timeInMillis

        calendar.set(year, month, calendar.getActualMaximum(Calendar.DAY_OF_MONTH), 23, 59, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        return startDate to calendar.timeInMillis
    }

    private fun yearRange(year: Int): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        calendar.set(year, 0, 1, 0, 0, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startDate = calendar.timeInMillis

        calendar.set(year, 11, 31, 23, 59, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        return startDate to calendar.timeInMillis
    }
}
