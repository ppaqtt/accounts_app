package com.jizhangben.app.ui.utils

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.jizhangben.app.MainActivity
import com.jizhangben.app.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TodayWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
    }

    companion object {
        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val views = RemoteViews(context.packageName, R.layout.widget_today)

            // 获取今日数据
            val prefs = context.getSharedPreferences("widget_data", Context.MODE_PRIVATE)
            val todayKey = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
            val income = prefs.getFloat("income_$todayKey", 0f)
            val expense = prefs.getFloat("expense_$todayKey", 0f)

            views.setTextViewText(R.id.widget_income, "¥${String.format("%.2f", income)}")
            views.setTextViewText(R.id.widget_expense, "¥${String.format("%.2f", expense)}")

            // 点击快速记账按钮
            val addIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("open_add", true)
            }
            val addPendingIntent = PendingIntent.getActivity(
                context, 0, addIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_quick_add, addPendingIntent)

            // 点击整个小组件打开APP
            val mainIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val mainPendingIntent = PendingIntent.getActivity(
                context, 1, mainIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_title, mainPendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        fun saveTodayData(context: Context, income: Double, expense: Double) {
            val prefs = context.getSharedPreferences("widget_data", Context.MODE_PRIVATE)
            val todayKey = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
            prefs.edit()
                .putFloat("income_$todayKey", income.toFloat())
                .putFloat("expense_$todayKey", expense.toFloat())
                .apply()
        }
    }
}
