package com.jizhang.xiaomeng.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.jizhang.xiaomeng.data.dao.AccountDao
import com.jizhang.xiaomeng.data.dao.CategoryDao
import com.jizhang.xiaomeng.data.dao.RecurringTransactionDao
import com.jizhang.xiaomeng.data.dao.TransactionDao
import com.jizhang.xiaomeng.data.model.Account
import com.jizhang.xiaomeng.data.model.AccountType
import com.jizhang.xiaomeng.data.model.Category
import com.jizhang.xiaomeng.data.model.RecurringTransaction
import com.jizhang.xiaomeng.data.model.Transaction
import com.jizhang.xiaomeng.data.model.TransactionType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [Transaction::class, Category::class, RecurringTransaction::class, Account::class],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao
    abstract fun recurringTransactionDao(): RecurringTransactionDao
    abstract fun accountDao(): AccountDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE transactions ADD COLUMN accountId INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE transactions ADD COLUMN toAccountId INTEGER NOT NULL DEFAULT 0")
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS accounts (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                            "name TEXT NOT NULL, " +
                            "type TEXT NOT NULL, " +
                            "initialBalance REAL NOT NULL, " +
                            "isDefault INTEGER NOT NULL)"
                )
                insertDefaultAccounts(db)
            }
        }

        private fun insertDefaultAccounts(db: SupportSQLiteDatabase) {
            val defaults = listOf(
                Triple("现金", "CASH", 1),
                Triple("支付宝", "ALIPAY", 1),
                Triple("微信", "WECHAT", 1),
                Triple("银行卡", "BANK_CARD", 1)
            )
            defaults.forEach { (name, type, isDefault) ->
                db.execSQL(
                    "INSERT INTO accounts (name, type, initialBalance, isDefault) VALUES (?, ?, 0, ?)",
                    arrayOf(name, type, isDefault)
                )
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "jizhangben_database"
                )
                    .addCallback(DatabaseCallback())
                    .addMigrations(MIGRATION_2_3)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                CoroutineScope(Dispatchers.IO).launch {
                    INSTANCE?.let { database ->
                        prepopulateCategories(database.categoryDao())
                        prepopulateAccounts(database.accountDao())
                    }
                }
            }
        }

        private suspend fun prepopulateAccounts(dao: AccountDao) {
            if (dao.countAccounts() > 0) return
            listOf(
                Account(name = "现金", type = AccountType.CASH, isDefault = true),
                Account(name = "支付宝", type = AccountType.ALIPAY, isDefault = true),
                Account(name = "微信", type = AccountType.WECHAT, isDefault = true),
                Account(name = "银行卡", type = AccountType.BANK_CARD, isDefault = true)
            ).forEach { dao.insertAccount(it) }
        }

        private suspend fun prepopulateCategories(dao: CategoryDao) {
            val expenseCategories = listOf(
                Category(name = "餐饮", icon = "restaurant", type = TransactionType.EXPENSE, isDefault = true),
                Category(name = "交通", icon = "directions_car", type = TransactionType.EXPENSE, isDefault = true),
                Category(name = "购物", icon = "shopping_bag", type = TransactionType.EXPENSE, isDefault = true),
                Category(name = "娱乐", icon = "movie", type = TransactionType.EXPENSE, isDefault = true),
                Category(name = "居住", icon = "home", type = TransactionType.EXPENSE, isDefault = true),
                Category(name = "医疗", icon = "medical_services", type = TransactionType.EXPENSE, isDefault = true),
                Category(name = "教育", icon = "school", type = TransactionType.EXPENSE, isDefault = true),
                Category(name = "通讯", icon = "phone_android", type = TransactionType.EXPENSE, isDefault = true),
                Category(name = "服饰", icon = "checkroom", type = TransactionType.EXPENSE, isDefault = true),
                Category(name = "其他", icon = "more_horiz", type = TransactionType.EXPENSE, isDefault = true)
            )

            val incomeCategories = listOf(
                Category(name = "工资", icon = "account_balance_wallet", type = TransactionType.INCOME, isDefault = true),
                Category(name = "奖金", icon = "card_giftcard", type = TransactionType.INCOME, isDefault = true),
                Category(name = "投资", icon = "trending_up", type = TransactionType.INCOME, isDefault = true),
                Category(name = "兼职", icon = "work", type = TransactionType.INCOME, isDefault = true),
                Category(name = "其他", icon = "more_horiz", type = TransactionType.INCOME, isDefault = true)
            )

            (expenseCategories + incomeCategories).forEach {
                dao.insertCategory(it)
            }
        }
    }
}
