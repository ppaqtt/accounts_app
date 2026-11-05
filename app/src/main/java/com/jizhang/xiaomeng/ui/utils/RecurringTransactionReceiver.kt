package com.jizhang.xiaomeng.ui.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.jizhang.xiaomeng.JiZhangBenApplication
import com.jizhang.xiaomeng.data.model.Transaction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date

class RecurringTransactionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_CHECK_RECURRING) return

        val app = context.applicationContext as JiZhangBenApplication
        val repository = app.recurringTransactionRepository
        val transactionRepository = app.transactionRepository
        val book = BookManager(context).currentBook

        val today = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)

        CoroutineScope(Dispatchers.IO).launch {
            val (start, end) = todayRange()
            val existingToday = transactionRepository.getTransactionsByBook(book).first()
                .filter { it.date.time in start..end }
            val recurringList = repository.getRecurringTransactionsByDay(today).first()

            recurringList.filter { it.isEnabled }.forEach { recurring ->
                val note = recurring.note.ifEmpty { "周期性账单" }
                val alreadyCreated = existingToday.any {
                    it.amount == recurring.amount &&
                        it.type == recurring.type &&
                        it.categoryId == recurring.categoryId &&
                        it.note == note
                }
                if (!alreadyCreated) {
                    transactionRepository.insertTransaction(
                        Transaction(
                            amount = recurring.amount,
                            type = recurring.type,
                            categoryId = recurring.categoryId,
                            note = note,
                            date = Date(),
                            bookId = book
                        )
                    )
                }
            }
        }
    }

    private fun todayRange(): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val start = calendar.timeInMillis
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        return start to calendar.timeInMillis
    }

    companion object {
        const val ACTION_CHECK_RECURRING = "com.jizhang.xiaomeng.ACTION_CHECK_RECURRING"

        fun schedule(context: Context) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, RecurringTransactionReceiver::class.java).apply {
                action = ACTION_CHECK_RECURRING
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                2,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 5)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                if (timeInMillis <= System.currentTimeMillis()) {
                    add(Calendar.DAY_OF_MONTH, 1)
                }
            }
            alarmManager.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                AlarmManager.INTERVAL_DAY,
                pendingIntent
            )
        }
    }
}
