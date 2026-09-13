package com.example.ui.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.*

object CurrencyHelper {
    val ratesToRub = mapOf(
        "RUB" to 1.0,
        "USD" to 92.5,
        "EUR" to 100.2,
        "KZT" to 0.20,
        "BYN" to 28.5,
        "CNY" to 12.8
    )

    val currencySymbols = mapOf(
        "RUB" to "₽",
        "USD" to "$",
        "EUR" to "€",
        "KZT" to "₸",
        "BYN" to "Br",
        "CNY" to "¥"
    )

    fun formatAmount(amount: Double, currency: String = "RUB", showPlus: Boolean = false): String {
        val symbols = DecimalFormatSymbols(Locale.getDefault()).apply {
            groupingSeparator = ' '
            decimalSeparator = ','
        }
        val isWhole = amount % 1.0 == 0.0
        val pattern = if (isWhole) "#,##0" else "#,##0.00"
        val formatter = DecimalFormat(pattern, symbols)
        val formatted = formatter.format(Math.abs(amount))
        val symbol = currencySymbols[currency] ?: currency

        val sign = when {
            amount < 0 -> "−"
            amount > 0 && showPlus -> "+"
            else -> ""
        }
        return if (sign.isNotEmpty()) "$sign$formatted $symbol" else "$formatted $symbol"
    }

    fun format(amount: Double, currency: String = "RUB"): String = formatAmount(amount, currency)

    fun convert(amount: Double, fromCurrency: String, toCurrency: String): Double {
        if (fromCurrency == toCurrency) return amount
        val rateFrom = ratesToRub[fromCurrency] ?: 1.0
        val rateTo = ratesToRub[toCurrency] ?: 1.0
        val amountInRub = amount * rateFrom
        return amountInRub / rateTo
    }
}

object DateHelper {
    fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("d MMMM, HH:mm", Locale("ru"))
        return sdf.format(Date(timestamp))
    }

    fun formatDateShort(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd.MM.yyyy", Locale("ru"))
        return sdf.format(Date(timestamp))
    }

    fun isToday(timestamp: Long): Boolean {
        val cal1 = Calendar.getInstance().apply { timeInMillis = timestamp }
        val cal2 = Calendar.getInstance()
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
               cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }
}

object IconHelper {
    fun getIconByName(iconName: String): ImageVector {
        return when (iconName) {
            "shopping_cart" -> Icons.Default.ShoppingCart
            "restaurant" -> Icons.Default.Restaurant
            "directions_car" -> Icons.Default.DirectionsCar
            "home" -> Icons.Default.Home
            "checkroom" -> Icons.Default.Checkroom
            "medical_services" -> Icons.Default.MedicalServices
            "movie" -> Icons.Default.Movie
            "subscriptions" -> Icons.Default.Subscriptions
            "wifi" -> Icons.Default.Wifi
            "school" -> Icons.Default.School
            "payments" -> Icons.Default.Payments
            "account_balance_wallet" -> Icons.Default.AccountBalanceWallet
            "credit_card" -> Icons.Default.CreditCard
            "savings" -> Icons.Default.Savings
            "trending_up" -> Icons.Default.TrendingUp
            "account_balance" -> Icons.Default.AccountBalance
            "laptop" -> Icons.Default.Laptop
            "redeem" -> Icons.Default.Redeem
            "sell" -> Icons.Default.Sell
            "flight" -> Icons.Default.Flight
            "shield" -> Icons.Default.Shield
            "flag" -> Icons.Default.Flag
            "compare_arrows" -> Icons.AutoMirrored.Filled.CompareArrows
            "swap_horiz" -> Icons.Default.SwapHoriz
            "attach_money" -> Icons.Default.AttachMoney
            "euro" -> Icons.Default.Euro
            "currency_ruble" -> Icons.Default.CurrencyRuble
            "storefront" -> Icons.Default.Storefront
            "loyalty" -> Icons.Default.Loyalty
            "call_split" -> Icons.Default.CallSplit
            else -> Icons.Default.Category
        }
    }

    fun parseColor(hex: String): Color {
        return try {
            val cleanHex = hex.removePrefix("#")
            val colorLong = cleanHex.toLong(16)
            if (cleanHex.length == 6) {
                Color(0xFF000000 or colorLong)
            } else {
                Color(colorLong)
            }
        } catch (e: Exception) {
            Color(0xFF10B981)
        }
    }
}
