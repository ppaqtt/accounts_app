package com.jizhangben.app.ui.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.jizhangben.app.JiZhangBenApplication
import com.jizhangben.app.data.model.Transaction
import com.jizhangben.app.data.model.TransactionType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date

class RecurringTransactionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "com.jizhangben.app.ACTION_CHECK_RECURRING") {
            val app = context.applicationContext as JiZhangBenApplication
            val repository = app.recurringTransactionRepository
            val transactionRepository = app.transactionRepository

            val today = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)

            CoroutineScope(Dispatchers.IO).launch {
                // 获取今天需要执行的周期性账单
                repository.getRecurringTransactionsByDay(today).collect { list ->
                    list.filter { it.isEnabled }.forEach { recurring ->
                        // 创建一笔新的交易记录
                        val transaction = Transaction(
                            amount = recurring.amount,
                            type = recurring.type,
                            categoryId = recurring.categoryId,
                            note = recurring.note.ifEmpty { "周期性账单" },
                            date = Date()
                        )
                        transactionRepository.insertTransaction(transaction)
                    }
                }
            }
        }
    }
}
