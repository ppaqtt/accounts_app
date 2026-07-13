package com.jizhangben.app.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.jizhangben.app.data.model.Transaction
import com.jizhangben.app.data.model.TransactionType
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY date DESC, createdAt DESC")
    fun getAllTransactions(): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE type = :type ORDER BY date DESC, createdAt DESC")
    fun getTransactionsByType(type: TransactionType): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE date >= :startDate AND date <= :endDate ORDER BY date DESC, createdAt DESC")
    fun getTransactionsByDateRange(startDate: Long, endDate: Long): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getTransactionById(id: Long): Transaction?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: Transaction): Long

    @Update
    suspend fun updateTransaction(transaction: Transaction)

    @Delete
    suspend fun deleteTransaction(transaction: Transaction)

    @Query("SELECT SUM(amount) FROM transactions WHERE type = :type AND date >= :startDate AND date <= :endDate")
    fun getTotalAmountByTypeAndDateRange(type: TransactionType, startDate: Long, endDate: Long): Flow<Double?>

    @Query("SELECT categoryId, SUM(amount) as total FROM transactions WHERE type = :type AND date >= :startDate AND date <= :endDate GROUP BY categoryId ORDER BY total DESC")
    fun getCategorySummaryByTypeAndDateRange(type: TransactionType, startDate: Long, endDate: Long): Flow<List<CategorySummary>>

    @Query("DELETE FROM transactions WHERE id IN (:ids)")
    suspend fun deleteTransactionsByIds(ids: List<Long>)

    @Query("SELECT * FROM transactions WHERE date >= :startDate AND date <= :endDate AND type = :type ORDER BY date DESC")
    fun getTransactionsByDateRangeAndType(startDate: Long, endDate: Long, type: TransactionType): Flow<List<Transaction>>
}

data class CategorySummary(
    val categoryId: Long,
    val total: Double
)
