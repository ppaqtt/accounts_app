package com.jizhang.xiaomeng

import android.app.Application
import com.jizhang.xiaomeng.data.db.AppDatabase
import com.jizhang.xiaomeng.data.repository.CategoryRepository
import com.jizhang.xiaomeng.data.repository.RecurringTransactionRepository
import com.jizhang.xiaomeng.data.repository.TransactionRepository
import com.jizhang.xiaomeng.ui.utils.AutoBackupManager
import com.jizhang.xiaomeng.ui.utils.QuickAddShortcut
import com.jizhang.xiaomeng.ui.utils.RecurringTransactionReceiver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class JiZhangBenApplication : Application() {
    // 使用 getter 而不是 lazy：从备份恢复后数据库实例会被重置，需要重新打开。
    val database: AppDatabase
        get() = AppDatabase.getDatabase(this)
    val transactionRepository: TransactionRepository
        get() = TransactionRepository(database.transactionDao())
    val categoryRepository: CategoryRepository
        get() = CategoryRepository(database.categoryDao())
    val recurringTransactionRepository: RecurringTransactionRepository
        get() = RecurringTransactionRepository(database.recurringTransactionDao())

    override fun onCreate() {
        super.onCreate()
        RecurringTransactionReceiver.schedule(this)
        QuickAddShortcut.createShortcut(this)
        val backupManager = AutoBackupManager(this)
        backupManager.applyPendingRestoreIfNeeded(getDatabasePath("jizhangben_database"))
        if (backupManager.shouldBackupToday()) {
            CoroutineScope(Dispatchers.IO).launch {
                // 先确保数据库已创建并落盘，再进行每日自动备份。
                database.openHelper.writableDatabase
                    .query("PRAGMA wal_checkpoint(FULL)")
                    .use { it.moveToFirst() }
                val dbFile = getDatabasePath("jizhangben_database")
                if (dbFile.exists()) {
                    backupManager.performBackup(dbFile)
                }
            }
        }
    }
}
