package com.jizhangben.app.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.jizhangben.app.data.dao.CategoryDao
import com.jizhangben.app.data.dao.TransactionDao
import com.jizhangben.app.data.model.Category
import com.jizhangben.app.data.model.Transaction
import com.jizhangben.app.data.model.TransactionType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [Transaction::class, Category::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao

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
