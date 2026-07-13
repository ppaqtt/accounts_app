package com.jizhangben.app

import android.app.Application
import com.jizhangben.app.data.db.AppDatabase
import com.jizhangben.app.data.repository.CategoryRepository
import com.jizhangben.app.data.repository.RecurringTransactionRepository
import com.jizhangben.app.data.repository.TransactionRepository

class JiZhangBenApplication : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }
    val transactionRepository by lazy { TransactionRepository(database.transactionDao()) }
    val categoryRepository by lazy { CategoryRepository(database.categoryDao()) }
    val recurringTransactionRepository by lazy { RecurringTransactionRepository(database.recurringTransactionDao()) }
}
