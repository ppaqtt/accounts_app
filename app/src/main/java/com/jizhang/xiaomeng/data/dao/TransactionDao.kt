package com.jizhang.xiaomeng.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.jizhang.xiaomeng.data.model.Transaction
import com.jizhang.xiaomeng.data.model.TransactionType
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY date DESC, createdAt DESC")
    fun getAllTransactions(): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE bookId = :book ORDER BY date DESC, createdAt DESC")
    fun getTransactionsByBook(book: String): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE bookId = :book AND date >= :startDate AND date <= :endDate ORDER BY date DESC, createdAt DESC")
    fun getTransactionsByBookAndDateRange(book: String, startDate: Long, endDate: Long): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE bookId = :book AND type = :type ORDER BY date DESC, createdAt DESC")
    fun getTransactionsByBookAndType(book: String, type: TransactionType): Flow<List<Transaction>>

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

    @Query("SELECT SUM(amount) FROM transactions WHERE type = :type AND bookId = :book AND date >= :startDate AND date <= :endDate")
    fun getTotalAmountByTypeAndBookAndDateRange(type: TransactionType, book: String, startDate: Long, endDate: Long): Flow<Double?>

    @Query("SELECT categoryId, SUM(amount) as total FROM transactions WHERE type = :type AND date >= :startDate AND date <= :endDate GROUP BY categoryId ORDER BY total DESC")
    fun getCategorySummaryByTypeAndDateRange(type: TransactionType, startDate: Long, endDate: Long): Flow<List<CategorySummary>>

    @Query("SELECT categoryId, SUM(amount) as total FROM transactions WHERE type = :type AND bookId = :book AND date >= :startDate AND date <= :endDate GROUP BY categoryId ORDER BY total DESC")
    fun getCategorySummaryByTypeAndBookAndDateRange(type: TransactionType, book: String, startDate: Long, endDate: Long): Flow<List<CategorySummary>>

    @Query("DELETE FROM transactions WHERE id IN (:ids)")
    suspend fun deleteTransactionsByIds(ids: List<Long>)

    @Query("DELETE FROM transactions WHERE bookId = :book")
    suspend fun deleteTransactionsByBook(book: String)

    @Query("DELETE FROM transactions")
    suspend fun deleteAllTransactions()

    @Query("SELECT * FROM transactions WHERE date >= :startDate AND date <= :endDate AND type = :type ORDER BY date DESC")
    fun getTransactionsByDateRangeAndType(startDate: Long, endDate: Long, type: TransactionType): Flow<List<Transaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactions(transactions: List<Transaction>)

    @Query("UPDATE transactions SET bookId = :newBook WHERE bookId = :oldBook")
    suspend fun renameBookTransactions(oldBook: String, newBook: String)

    @Query("SELECT COUNT(*) FROM transactions WHERE categoryId = :categoryId")
    suspend fun countByCategory(categoryId: Long): Int

    @Query("UPDATE transactions SET categoryId = :targetCategoryId WHERE categoryId = :categoryId")
    suspend fun reassignCategory(categoryId: Long, targetCategoryId: Long)
}

data class CategorySummary(
    val categoryId: Long,
    val total: Double
)
