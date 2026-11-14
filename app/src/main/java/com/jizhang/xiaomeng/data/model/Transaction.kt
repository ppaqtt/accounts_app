package com.jizhang.xiaomeng.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = Category::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("categoryId")]
)
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amount: Double,
    val type: TransactionType,
    val categoryId: Long,
    val accountId: Long = 0, // 资金转出账户，0 表示未指定
    val toAccountId: Long = 0, // 转账时的转入账户
    val note: String = "",
    val tags: String = "", // 逗号分隔的标签，如 "工作,餐饮"
    val date: Date,
    val createdAt: Date = Date()
)
