package com.jizhang.xiaomeng.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.jizhang.xiaomeng.data.dao.CategoryDao
import com.jizhang.xiaomeng.data.dao.RecurringTransactionDao
import com.jizhang.xiaomeng.data.dao.TransactionDao
import com.jizhang.xiaomeng.data.model.Category
import com.jizhang.xiaomeng.data.model.RecurringTransaction
import com.jizhang.xiaomeng.data.model.Transaction
import com.jizhang.xiaomeng.data.model.TransactionType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [Transaction::class, Category::class, RecurringTransaction::class],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao
    abstract fun recurringTransactionDao(): RecurringTransactionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "jizhangben_database"
                )
                    .addCallback(DatabaseCallback())
                    .addMigrations(MIGRATION_2_3)
                    .build()
                INSTANCE = instance
                instance
            }
        }

        fun resetInstance() {
            synchronized(this) {
                INSTANCE?.close()
                INSTANCE = null
            }
        }

        /** v2 -> v3：增加账本字段，并把分类外键改为 RESTRICT，避免删除分类时连带删除账单。 */
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS transactions_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        amount REAL NOT NULL,
                        type TEXT NOT NULL,
                        categoryId INTEGER NOT NULL,
                        note TEXT NOT NULL,
                        tags TEXT NOT NULL,
                        date INTEGER NOT NULL,
                        createdAt INTEGER NOT NULL,
                        bookId TEXT NOT NULL,
                        FOREIGN KEY(categoryId) REFERENCES categories(id) ON UPDATE NO ACTION ON DELETE RESTRICT
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    INSERT INTO transactions_new (id, amount, type, categoryId, note, tags, date, createdAt, bookId)
                    SELECT id, amount, type, categoryId, note, tags, date, createdAt, '日常账本' FROM transactions
                    """.trimIndent()
                )
                db.execSQL("DROP TABLE transactions")
                db.execSQL("ALTER TABLE transactions_new RENAME TO transactions")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_transactions_categoryId ON transactions(categoryId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_transactions_bookId ON transactions(bookId)")
            }
        }

        private class DatabaseCallback : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                CoroutineScope(Dispatchers.IO).launch {
                    INSTANCE?.let { database ->
                        prepopulateCategories(database.categoryDao())
                    }
                }
            }
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
