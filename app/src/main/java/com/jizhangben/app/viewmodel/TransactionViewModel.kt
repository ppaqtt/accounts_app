package com.jizhangben.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.jizhangben.app.JiZhangBenApplication
import com.jizhangben.app.data.model.Category
import com.jizhangben.app.data.model.Transaction
import com.jizhangben.app.data.model.TransactionType
import com.jizhangben.app.data.repository.CategoryRepository
import com.jizhangben.app.data.repository.TransactionRepository
import kotlinx.coroutines.launch
import java.util.Calendar

class TransactionViewModel(application: Application) : AndroidViewModel(application) {
    private val transactionRepository: TransactionRepository
    private val categoryRepository: CategoryRepository

    val allTransactions: LiveData<List<Transaction>>
    val allCategories: LiveData<List<Category>>

    private val calendar = Calendar.getInstance()
    val currentYear = calendar.get(Calendar.YEAR)
    val currentMonth = calendar.get(Calendar.MONTH)

    init {
        val database = (application as JiZhangBenApplication).database
        transactionRepository = (application as JiZhangBenApplication).transactionRepository
        categoryRepository = (application as JiZhangBenApplication).categoryRepository
        allTransactions = transactionRepository.allTransactions.asLiveData()
        allCategories = categoryRepository.allCategories.asLiveData()
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

    fun insertTransaction(transaction: Transaction) = viewModelScope.launch {
        transactionRepository.insertTransaction(transaction)
    }

    fun updateTransaction(transaction: Transaction) = viewModelScope.launch {
        transactionRepository.updateTransaction(transaction)
    }

    fun deleteTransaction(transaction: Transaction) = viewModelScope.launch {
        transactionRepository.deleteTransaction(transaction)
    }

    suspend fun getCategoryById(id: Long): Category? {
        return categoryRepository.getCategoryById(id)
    }
}
