package com.jizhang.xiaomeng

import android.app.Application
import com.jizhang.xiaomeng.data.db.AppDatabase
import com.jizhang.xiaomeng.data.repository.CategoryRepository
import com.jizhang.xiaomeng.data.repository.RecurringTransactionRepository
import com.jizhang.xiaomeng.data.repository.TransactionRepository

class JiZhangBenApplication : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }
    val transactionRepository by lazy { TransactionRepository(database.transactionDao()) }
    val categoryRepository by lazy { CategoryRepository(database.categoryDao()) }
    val recurringTransactionRepository by lazy { RecurringTransactionRepository(database.recurringTransactionDao()) }
}
