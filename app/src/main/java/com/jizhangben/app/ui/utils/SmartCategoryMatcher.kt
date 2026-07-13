package com.jizhangben.app.ui.utils

import com.jizhangben.app.data.model.Category
import com.jizhangben.app.data.model.TransactionType

object SmartCategoryMatcher {
    // 关键词到分类名的映射
    private val expenseKeywords = mapOf(
        "餐饮" to listOf("早餐", "午餐", "晚餐", "外卖", "吃饭", "食堂", "奶茶", "咖啡", "小吃", "水果", "零食", "饮料", "火锅", "烧烤", "快餐", "面", "饭", "菜"),
        "交通" to listOf("地铁", "公交", "打车", "出租", "滴滴", "加油", "停车", "高速", "机票", "火车", "高铁", "单车"),
        "购物" to listOf("淘宝", "京东", "拼多多", "超市", "买菜", "日用品", "衣服", "鞋", "化妆品"),
        "娱乐" to listOf("电影", "游戏", "KTV", "唱歌", "旅游", "门票", "会员", "视频", "音乐"),
        "居住" to listOf("房租", "水电", "物业", "燃气", "网费", "维修"),
        "医疗" to listOf("药", "医院", "体检", "挂号", "看病", "牙", "手术"),
        "教育" to listOf("学费", "书", "课程", "培训", "考试"),
        "通讯" to listOf("话费", "流量", "充值", "手机"),
        "服饰" to listOf("衣服", "裤子", "裙子", "鞋", "包", "帽子", "围巾")
    )

    private val incomeKeywords = mapOf(
        "工资" to listOf("工资", "月薪", "薪资", "报酬"),
        "奖金" to listOf("奖金", "年终", "提成", "绩效", "红包"),
        "投资" to listOf("利息", "股息", "基金", "理财", "收益"),
        "兼职" to listOf("兼职", "副业", "外包", "私活")
    )

    fun recommendCategory(note: String, type: TransactionType, categories: List<Category>): Category? {
        val keywords = if (type == TransactionType.EXPENSE) expenseKeywords else incomeKeywords

        for ((categoryName, words) in keywords) {
            if (words.any { note.contains(it) }) {
                return categories.find { it.name == categoryName && it.type == type }
            }
        }
        return categories.find { it.type == type && it.name == "其他" }
    }
}
