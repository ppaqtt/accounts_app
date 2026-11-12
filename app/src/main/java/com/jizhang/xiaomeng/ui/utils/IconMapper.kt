package com.jizhang.xiaomeng.ui.utils

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Chair
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.LocalGroceryStore
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Nature
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PhoneIphone
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Weekend
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
        "add" to Icons.Filled.Add,
        "flight" to Icons.Filled.Flight,
        "hotel" to Icons.Filled.Hotel,
        "confirmation_number" to Icons.Filled.ConfirmationNumber,
        "chair" to Icons.Filled.Chair,
        "weekend" to Icons.Filled.Weekend,
        "build" to Icons.Filled.Build,
        "payments" to Icons.Filled.Payments,
        "favorite" to Icons.Filled.Favorite,
        "shopping_cart" to Icons.Filled.ShoppingCart,
        "pets" to Icons.Filled.Pets,
        "celebration" to Icons.Filled.Celebration,
        "cake" to Icons.Filled.Cake,
        "coffee" to Icons.Filled.Coffee,
        "local_grocery_store" to Icons.Filled.LocalGroceryStore,
        "fitness_center" to Icons.Filled.FitnessCenter,
        "savings" to Icons.Filled.Savings,
        "account_balance" to Icons.Filled.AccountBalance,
        "nature" to Icons.Filled.Nature,
        "phone_iphone" to Icons.Filled.PhoneIphone
    )

    fun getIcon(iconName: String): ImageVector {
        return iconMap[iconName] ?: Icons.Filled.MoreHoriz
    }
}
