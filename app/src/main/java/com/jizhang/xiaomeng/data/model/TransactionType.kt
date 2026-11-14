package com.jizhang.xiaomeng.data.model

enum class TransactionType {
    EXPENSE,
    INCOME,
    TRANSFER
}

val TransactionType.displayName: String
    get() = when (this) {
        TransactionType.EXPENSE -> "支出"
        TransactionType.INCOME -> "收入"
        TransactionType.TRANSFER -> "转账"
    }
