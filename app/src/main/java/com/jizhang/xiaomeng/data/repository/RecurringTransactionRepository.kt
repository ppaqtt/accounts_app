package com.jizhang.xiaomeng.data.repository

import com.jizhang.xiaomeng.data.dao.RecurringTransactionDao
import com.jizhang.xiaomeng.data.model.RecurringTransaction
import kotlinx.coroutines.flow.Flow

class RecurringTransactionRepository(private val dao: RecurringTransactionDao) {
    val allRecurringTransactions: Flow<List<RecurringTransaction>> = dao.getAllRecurringTransactions()
    val enabledRecurringTransactions: Flow<List<RecurringTransaction>> = dao.getEnabledRecurringTransactions()

    fun getRecurringTransactionsByDay(day: Int): Flow<List<RecurringTransaction>> = dao.getRecurringTransactionsByDay(day)

    suspend fun insert(transaction: RecurringTransaction): Long = dao.insertRecurringTransaction(transaction)
    suspend fun update(transaction: RecurringTransaction) = dao.updateRecurringTransaction(transaction)
    suspend fun delete(transaction: RecurringTransaction) = dao.deleteRecurringTransaction(transaction)
}
