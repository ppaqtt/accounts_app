package com.jizhangben.app.data.repository

import com.jizhangben.app.data.dao.TransactionDao
import com.jizhangben.app.data.model.Transaction
import com.jizhangben.app.data.model.TransactionType
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

class TransactionRepository(private val transactionDao: TransactionDao) {
    val allTransactions: Flow<List<Transaction>> = transactionDao.getAllTransactions()

    fun getTransactionsByType(type: TransactionType): Flow<List<Transaction>> {
        return transactionDao.getTransactionsByType(type)
    }

    fun getTransactionsByMonth(year: Int, month: Int): Flow<List<Transaction>> {
        val calendar = Calendar.getInstance()
        calendar.set(year, month, 1, 0, 0, 0)
        val startDate = calendar.timeInMillis

        calendar.set(year, month, calendar.getActualMaximum(Calendar.DAY_OF_MONTH), 23, 59, 59)
        val endDate = calendar.timeInMillis

        return transactionDao.getTransactionsByDateRange(startDate, endDate)
    }

    suspend fun insertTransaction(transaction: Transaction): Long {
        return transactionDao.insertTransaction(transaction)
    }

    suspend fun updateTransaction(transaction: Transaction) {
        transactionDao.updateTransaction(transaction)
    }

    suspend fun deleteTransaction(transaction: Transaction) {
        transactionDao.deleteTransaction(transaction)
    }

    fun getTotalAmountByTypeAndMonth(type: TransactionType, year: Int, month: Int): Flow<Double?> {
        val calendar = Calendar.getInstance()
        calendar.set(year, month, 1, 0, 0, 0)
        val startDate = calendar.timeInMillis

        calendar.set(year, month, calendar.getActualMaximum(Calendar.DAY_OF_MONTH), 23, 59, 59)
        val endDate = calendar.timeInMillis

        return transactionDao.getTotalAmountByTypeAndDateRange(type, startDate, endDate)
    }

    fun getCategorySummaryByTypeAndMonth(type: TransactionType, year: Int, month: Int): Flow<List<com.jizhangben.app.data.dao.CategorySummary>> {
        val calendar = Calendar.getInstance()
        calendar.set(year, month, 1, 0, 0, 0)
        val startDate = calendar.timeInMillis

        calendar.set(year, month, calendar.getActualMaximum(Calendar.DAY_OF_MONTH), 23, 59, 59)
        val endDate = calendar.timeInMillis

        return transactionDao.getCategorySummaryByTypeAndDateRange(type, startDate, endDate)
    }
}
