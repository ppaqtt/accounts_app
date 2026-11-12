package com.jizhang.xiaomeng.ui.utils

import com.jizhang.xiaomeng.data.model.Category
import com.jizhang.xiaomeng.data.model.TransactionType

object CategoryTemplates {

    data class SceneTemplate(
        val key: String,
        val emoji: String,
        val title: String,
        val description: String,
        val categories: List<Category>
    )

    private fun expense(name: String, icon: String) =
        Category(name = name, icon = icon, type = TransactionType.EXPENSE, isDefault = false)

    private fun income(name: String, icon: String) =
        Category(name = name, icon = icon, type = TransactionType.INCOME, isDefault = false)

    val all = listOf(
        SceneTemplate(
            key = "travel",
            emoji = "✈️",
            title = "旅行账本",
            description = "机票、住宿、门票、当地交通与伴手礼",
            categories = listOf(
                expense("机票", "flight"),
                expense("住宿", "hotel"),
                expense("门票", "confirmation_number"),
                expense("旅行餐饮", "restaurant"),
                expense("当地交通", "directions_car"),
                expense("旅行购物", "shopping_bag"),
                expense("伴手礼", "card_giftcard"),
                expense("旅行其他", "more_horiz")
            )
        ),
        SceneTemplate(
            key = "renovation",
            emoji = "🛠️",
            title = "装修账本",
            description = "主材、家具家电、人工与软装预算",
            categories = listOf(
                expense("主材", "home"),
                expense("家具", "chair"),
                expense("家电", "weekend"),
                expense("装修人工", "build"),
                expense("设计费", "payments"),
                expense("软装", "shopping_bag"),
                expense("装修其他", "more_horiz")
            )
        ),
        SceneTemplate(
            key = "wedding",
            emoji = "💍",
            title = "婚礼账本",
            description = "婚宴、婚纱、摄影、婚戒与婚车",
            categories = listOf(
                expense("婚宴", "restaurant"),
                expense("婚纱礼服", "checkroom"),
                expense("婚纱摄影", "movie"),
                expense("婚戒首饰", "favorite"),
                expense("婚车", "directions_car"),
                expense("请柬喜糖", "card_giftcard"),
                expense("婚礼其他", "more_horiz"),
                income("礼金", "card_giftcard")
            )
        ),
        SceneTemplate(
            key = "family",
            emoji = "👨‍👩‍👧",
            title = "家庭共用",
            description = "日用、水电、教育、医疗与宠物",
            categories = listOf(
                expense("家庭餐饮", "restaurant"),
                expense("日用百货", "shopping_cart"),
                expense("水电燃气", "home"),
                expense("家庭交通", "directions_car"),
                expense("教育支出", "school"),
                expense("医疗支出", "medical_services"),
                expense("家庭娱乐", "movie"),
                expense("宠物", "pets"),
                income("家庭收入", "account_balance_wallet")
            )
        )
    )
}
