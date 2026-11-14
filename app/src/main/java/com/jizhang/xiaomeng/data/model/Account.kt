package com.jizhang.xiaomeng.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class AccountType(val displayName: String) {
    CASH("现金"),
    BANK_CARD("银行卡"),
    ALIPAY("支付宝"),
    WECHAT("微信"),
    OTHER("其他")
}

@Entity(tableName = "accounts")
data class Account(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: AccountType,
    val initialBalance: Double = 0.0,
    val isDefault: Boolean = false
)
