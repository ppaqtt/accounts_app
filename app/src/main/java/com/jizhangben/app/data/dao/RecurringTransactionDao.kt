package com.jizhangben.app.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.jizhangben.app.data.model.RecurringTransaction
import kotlinx.coroutines.flow.Flow

@Dao
interface RecurringTransactionDao {
    @Query("SELECT * FROM recurring_transactions ORDER BY dayOfMonth ASC")
    fun getAllRecurringTransactions(): Flow<List<RecurringTransaction>>

    @Query("SELECT * FROM recurring_transactions WHERE isEnabled = 1")
    fun getEnabledRecurringTransactions(): Flow<List<RecurringTransaction>>

    @Query("SELECT * FROM recurring_transactions WHERE dayOfMonth = :day")
    fun getRecurringTransactionsByDay(day: Int): Flow<List<RecurringTransaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecurringTransaction(transaction: RecurringTransaction): Long

    @Update
    suspend fun updateRecurringTransaction(transaction: RecurringTransaction)

    @Delete
    suspend fun deleteRecurringTransaction(transaction: RecurringTransaction)
}
