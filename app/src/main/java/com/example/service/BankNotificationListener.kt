package com.example.service

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.example.data.database.AppDatabase
import com.example.data.entity.PendingNotificationEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class BankNotificationListener : NotificationListenerService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        if (sbn == null) return

        val packageName = sbn.packageName ?: return
        
        if (packageName == applicationContext.packageName) {
            return
        }

        // CRITICAL: Ignore other personal finance apps to avoid double counting (they also intercept bank pushes)
        val ignoredPackages = listOf(
            "ru.zenmoney.android",
            "com.coinkeeper.android",
            "com.monefy.app.lite",
            "com.monefy.app.pro",
            "com.innofinapps.1money",
            "com.orion.cashew"
        )
        if (ignoredPackages.any { packageName.contains(it, ignoreCase = true) }) {
            return
        }

        val extras = sbn.notification.extras ?: return

        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()
            ?: extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString()

        if (text.isNullOrBlank()) return

        // Check if package belongs to known banks or notification text mentions financial transaction keywords
        val isKnownBank = BankNotificationParser.KNOWN_BANK_PACKAGES.containsKey(packageName)
        val fullText = "${title ?: ""} $text".lowercase()
        val hasFinancialKeywords = fullText.contains("покупка") || fullText.contains("списание") ||
                fullText.contains("зачисление") || fullText.contains("перевод") ||
                fullText.contains("оплата") || fullText.contains("баланс")

        if (!isKnownBank && !hasFinancialKeywords) {
            return
        }

        val parsed = BankNotificationParser.parse(text, title, packageName)
        if (parsed != null && parsed.amount > 0) {
            Log.d("BankNotificationListener", "Intercepted bank notification: $parsed from $packageName")

            serviceScope.launch {
                try {
                    val db = AppDatabase.getDatabase(applicationContext, serviceScope)
                    // Check if matching account exists by currency or card
                    val accounts = db.accountDao().getActiveAccountsSync()
                    val matchedAccount = accounts.find { acc ->
                        (parsed.cardLast4 != null && acc.name.contains(parsed.cardLast4)) ||
                                acc.name.contains(parsed.bankName, ignoreCase = true) ||
                                (acc.currency == parsed.currency && !acc.isArchived)
                    } ?: accounts.firstOrNull()

                    val categories = db.categoryDao().getAllCategoriesSync()
                    val suggestedCatId = BankNotificationParser.matchCategoryId(categories, parsed.matchedCategoryKeyword)

                    val rawTextVal = "${title?.let { "$it: " } ?: ""}$text"
                    val timestampVal = sbn.postTime.takeIf { it > 0 } ?: System.currentTimeMillis()
                    
                    // Duplicate check: same text within the last 10 minutes
                    val duplicate = db.pendingNotificationDao().findRecentDuplicateByText(
                        rawText = rawTextVal,
                        sinceTime = timestampVal - 10 * 60 * 1000L
                    )
                    
                    if (duplicate != null) {
                        Log.d("BankNotificationListener", "Skipping duplicate notification: $rawTextVal")
                        return@launch
                    }

                    val entity = PendingNotificationEntity(
                        packageName = packageName,
                        bankName = parsed.bankName,
                        rawText = rawTextVal,
                        type = parsed.type,
                        amount = parsed.amount,
                        currency = parsed.currency,
                        merchantOrSender = parsed.merchant,
                        cardLast4 = parsed.cardLast4,
                        suggestedCategoryId = suggestedCatId,
                        suggestedAccountId = matchedAccount?.id,
                        timestamp = timestampVal
                    )
                    db.pendingNotificationDao().insertNotification(entity)

                    // Send push reminder if enabled so user gets reminded in background
                    PushNotificationHelper.sendBankTransactionReminder(
                        context = applicationContext,
                        bankName = parsed.bankName,
                        amount = parsed.amount,
                        currency = parsed.currency,
                        merchant = parsed.merchant,
                        type = parsed.type
                    )
                } catch (e: Exception) {
                    Log.e("BankNotificationListener", "Failed to process bank notification", e)
                }
            }
        }
    }
}
