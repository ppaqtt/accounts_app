package com.jizhangben.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.jizhangben.app.JiZhangBenApplication
import com.jizhangben.app.data.model.Category
import com.jizhangben.app.data.model.RecurringTransaction
import com.jizhangben.app.data.model.Transaction
import com.jizhangben.app.data.model.TransactionType
import com.jizhangben.app.data.repository.CategoryRepository
import com.jizhangben.app.data.repository.RecurringTransactionRepository
import com.jizhangben.app.data.repository.TransactionRepository
import com.jizhangben.app.ui.utils.CsvUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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

    fun getTransactionsByMonth(year: Int, month: Int): LiveData<List<Transaction>> {
        return transactionRepository.getTransactionsByMonth(year, month).asLiveData()
    }

    fun getTotalIncomeByMonth(year: Int, month: Int): LiveData<Double?> {
        return transactionRepository.getTotalAmountByTypeAndMonth(TransactionType.INCOME, year, month).asLiveData()
    }

    fun getTotalExpenseByMonth(year: Int, month: Int): LiveData<Double?> {
        return transactionRepository.getTotalAmountByTypeAndMonth(TransactionType.EXPENSE, year, month).asLiveData()
    }

    fun getExpenseCategorySummary(year: Int, month: Int): LiveData<List<com.jizhangben.app.data.dao.CategorySummary>> {
        return transactionRepository.getCategorySummaryByTypeAndMonth(TransactionType.EXPENSE, year, month).asLiveData()
    }

    fun getIncomeCategorySummary(year: Int, month: Int): LiveData<List<com.jizhangben.app.data.dao.CategorySummary>> {
        return transactionRepository.getCategorySummaryByTypeAndMonth(TransactionType.INCOME, year, month).asLiveData()
    }

    fun getTotalIncomeByYear(year: Int): LiveData<Double?> {
        return transactionRepository.getTotalAmountByTypeAndYear(TransactionType.INCOME, year).asLiveData()
    }

    fun getTotalExpenseByYear(year: Int): LiveData<Double?> {
        return transactionRepository.getTotalAmountByTypeAndYear(TransactionType.EXPENSE, year).asLiveData()
    }

    fun getExpenseCategorySummaryByYear(year: Int): LiveData<List<com.jizhangben.app.data.dao.CategorySummary>> {
        return transactionRepository.getCategorySummaryByTypeAndYear(TransactionType.EXPENSE, year).asLiveData()
    }

    fun getIncomeCategorySummaryByYear(year: Int): LiveData<List<com.jizhangben.app.data.dao.CategorySummary>> {
        return transactionRepository.getCategorySummaryByTypeAndYear(TransactionType.INCOME, year).asLiveData()
    }

    fun insertTransaction(transaction: Transaction) = viewModelScope.launch {
        transactionRepository.insertTransaction(transaction)
    }

    fun updateTransaction(transaction: Transaction) = viewModelScope.launch {
        transactionRepository.updateTransaction(transaction)
    }

    fun deleteTransaction(transaction: Transaction) = viewModelScope.launch {
        transactionRepository.deleteTransaction(transaction)
    }

    fun deleteTransactionsByIds(ids: List<Long>) = viewModelScope.launch {
        transactionRepository.deleteTransactionsByIds(ids)
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

    suspend fun exportToCsv(outputStream: OutputStream): Int = withContext(Dispatchers.IO) {
        val transactions = transactionRepository.allTransactions.first()
        val categories = categoryRepository.allCategories.first()
        CsvUtils.exportToCsv(transactions, categories, outputStream)
    }

    suspend fun importFromCsv(inputStream: InputStream): CsvUtils.ImportResult = withContext(Dispatchers.IO) {
        val categories = categoryRepository.allCategories.first()
        val result = CsvUtils.importFromCsv(inputStream, categories)
        for (transaction in result.transactions) {
            transactionRepository.insertTransaction(transaction)
        }
        result
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
