package com.jizhang.xiaomeng.ui.utils

import com.jizhang.xiaomeng.BuildConfig

object AppChangelog {
    val currentVersion: String = BuildConfig.VERSION_NAME
    const val currentDate = "2026.11.05"

    val currentEntries = listOf(
        "账单支持编辑金额、分类、日期、备注和标签",
        "点击账单可进入详情页，编辑入口不再跳空白页",
        "多账本数据真正隔离，切换账本后统计同步切换",
        "新增分类预算，可按月查看各类支出进度",
        "新增周报/月报，支持支出排行、每日趋势与分享",
        "新增连续记账天数提示",
        "新增自动备份与从备份恢复",
        "CSV 导入支持 GBK 编码、重复账单跳过、智能分类",
        "应用锁改为哈希存储，支持指纹/面容解锁",
        "离开应用超过 30 秒自动重新锁定",
        "通知权限申请与重启后提醒恢复",
        "删除分类时自动迁移账单，避免数据丢失",
        "数据库升级改为无损迁移",
        "修复桌面小组件与快速记账入口",
        "深色模式、动态取色、搜索筛选、年度统计、周期账单"
    )

    data class HistoryEntry(val version: String, val date: String, val entries: List<String>)

    val history = listOf(
        HistoryEntry(
            version = "v1.0.0",
            date = "2025.06.01",
            entries = listOf(
                "🎉 首次发布",
                "收支记账与分类管理",
                "月度统计与数据可视化",
                "账单编辑与删除",
                "CSV账单导入导出",
                "支持微信、QQ、支付宝账单导入",
                "数据本地存储，保护隐私"
            )
        )
    )
}
