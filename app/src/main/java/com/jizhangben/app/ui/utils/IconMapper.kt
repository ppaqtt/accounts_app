package com.jizhangben.app.ui.utils

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Work
import androidx.compose.ui.graphics.vector.ImageVector

object IconMapper {
    private val iconMap = mapOf(
        "restaurant" to Icons.Filled.Restaurant,
        "directions_car" to Icons.Filled.DirectionsCar,
        "shopping_bag" to Icons.Filled.ShoppingBag,
        "movie" to Icons.Filled.Movie,
        "home" to Icons.Filled.Home,
        "medical_services" to Icons.Filled.LocalHospital,
        "school" to Icons.Filled.School,
        "phone_android" to Icons.Filled.PhoneAndroid,
        "checkroom" to Icons.Filled.Checkroom,
        "more_horiz" to Icons.Filled.MoreHoriz,
        "account_balance_wallet" to Icons.Filled.AccountBalanceWallet,
        "card_giftcard" to Icons.Filled.CardGiftcard,
        "trending_up" to Icons.AutoMirrored.Filled.TrendingUp,
        "work" to Icons.Filled.Work,
        "add" to Icons.Filled.Add
    )

    fun getIcon(iconName: String): ImageVector {
        return iconMap[iconName] ?: Icons.Filled.MoreHoriz
    }
}
