package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.entity.AccountEntity
import com.example.service.UserBankHelper
import com.example.ui.theme.IncomeGreen
import com.example.ui.util.CurrencyHelper
import com.example.ui.util.IconHelper
import kotlin.math.roundToInt

@Composable
fun IncomeDistributionDialog(
    accounts: List<AccountEntity>,
    onDismiss: () -> Unit,
    onConfirm: (sourceAccountId: Long, allocations: List<Pair<Long, Double>>) -> Unit
) {
    val activeAccounts = remember(accounts) { accounts.filter { !it.isArchived } }

    // Source account (default: T-Bank for wife's cash salary or VTB Salary)
    val defaultSource = remember(activeAccounts) {
        activeAccounts.find { it.name.contains("Т-Банк", ignoreCase = true) || it.name.contains("Тинькофф", ignoreCase = true) }
            ?: activeAccounts.find { it.name.contains("Зарплат", ignoreCase = true) }
            ?: activeAccounts.firstOrNull()
    }
    var selectedSourceId by remember { mutableStateOf(defaultSource?.id ?: 0L) }
    var totalAmountText by remember { mutableStateOf(if (defaultSource != null && defaultSource.balance > 0) "%.0f".format(defaultSource.balance) else "65000") }

    val targetAccounts = remember(activeAccounts, selectedSourceId) {
        activeAccounts.filter { it.id != selectedSourceId }
    }

    // Map of accountId -> assigned amount
    val allocationsMap = remember { mutableStateMapOf<Long, Double>() }

    // Apply distribution strategy
    val applyStrategy: (String) -> Unit = { strategy ->
        val total = totalAmountText.toDoubleOrNull() ?: 0.0
        allocationsMap.clear()
        if (total > 0 && targetAccounts.isNotEmpty()) {
            when (strategy) {
                "FAMILY_BALANCE" -> {
                    // Look for specific user accounts
                    val vtbFood = targetAccounts.find { it.name.contains("Продукт", ignoreCase = true) }
                    val ozon = targetAccounts.find { it.name.contains("Озон", ignoreCase = true) }
                    val vtbSalary = targetAccounts.find { it.name.contains("Зарплат", ignoreCase = true) || it.name.contains("ЗП", ignoreCase = true) }
                    val alfa = targetAccounts.find { it.name.contains("Альфа", ignoreCase = true) || it.name.contains("Апельсин", ignoreCase = true) }
                    val yandex = targetAccounts.find { it.name.contains("Яндекс", ignoreCase = true) }

                    if (vtbFood != null && ozon != null) {
                        allocationsMap[vtbFood.id] = (total * 0.35).roundToInt().toDouble()
                        allocationsMap[ozon.id] = (total * 0.30).roundToInt().toDouble()
                        if (vtbSalary != null) allocationsMap[vtbSalary.id] = (total * 0.20).roundToInt().toDouble()
                        if (alfa != null) allocationsMap[alfa.id] = (total * 0.15).roundToInt().toDouble()
                    } else {
                        val portion = (total / targetAccounts.size).roundToInt().toDouble()
                        targetAccounts.forEach { allocationsMap[it.id] = portion }
                    }
                }
                "MAX_SAVINGS" -> {
                    val ozon = targetAccounts.find { it.name.contains("Озон", ignoreCase = true) }
                    val vtbFood = targetAccounts.find { it.name.contains("Продукт", ignoreCase = true) }
                    val others = targetAccounts.filter { it.id != ozon?.id && it.id != vtbFood?.id }

                    if (ozon != null) {
                        allocationsMap[ozon.id] = (total * 0.50).roundToInt().toDouble()
                        if (vtbFood != null) allocationsMap[vtbFood.id] = (total * 0.30).roundToInt().toDouble()
                        val remaining = total * 0.20
                        if (others.isNotEmpty()) {
                            val each = (remaining / others.size).roundToInt().toDouble()
                            others.forEach { allocationsMap[it.id] = each }
                        }
                    } else {
                        val portion = (total / targetAccounts.size).roundToInt().toDouble()
                        targetAccounts.forEach { allocationsMap[it.id] = portion }
                    }
                }
                "EQUAL" -> {
                    val portion = (total / targetAccounts.size).roundToInt().toDouble()
                    targetAccounts.forEach { allocationsMap[it.id] = portion }
                }
            }
        }
    }

    // Auto-init strategy once
    LaunchedEffect(selectedSourceId, totalAmountText) {
        if (allocationsMap.isEmpty()) {
            applyStrategy("FAMILY_BALANCE")
        }
    }

    val totalAllocated = allocationsMap.values.sum()
    val totalToDistribute = totalAmountText.toDoubleOrNull() ?: 0.0
    val remaining = totalToDistribute - totalAllocated

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f)
                .clip(RoundedCornerShape(28.dp))
                .testTag("income_distribution_dialog"),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(IncomeGreen.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CallSplit,
                                contentDescription = null,
                                tint = IncomeGreen
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Шлюз распределения средств",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Распределение ЗП жены и переводов по банкам",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Закрыть")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Source Account Selection
                Text(
                    text = "Откуда распределить (счёт-шлюз):",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(activeAccounts) { acc ->
                        val isSelected = acc.id == selectedSourceId
                        val accColor = IconHelper.parseColor(acc.colorHex)
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null,
                            modifier = Modifier.clickable {
                                selectedSourceId = acc.id
                                applyStrategy("FAMILY_BALANCE")
                            }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(accColor))
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = acc.name,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                    Text(
                                        text = CurrencyHelper.formatAmount(acc.balance, acc.currency),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Total Amount input
                OutlinedTextField(
                    value = totalAmountText,
                    onValueChange = { input ->
                        if (input.isEmpty() || input.matches(Regex("""^\d*([.,]\d{0,2})?$"""))) {
                            totalAmountText = input.replace(',', '.')
                        }
                    },
                    label = { Text("Общая сумма для распределения") },
                    trailingIcon = {
                        Text(
                            text = "₽",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = IncomeGreen,
                            modifier = Modifier.padding(end = 12.dp)
                        )
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    textStyle = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.fillMaxWidth().testTag("distribution_total_input"),
                    shape = RoundedCornerShape(14.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Strategy Chips
                Text(
                    text = "Готовые сценарии распределения:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    SuggestionChip(
                        onClick = { applyStrategy("FAMILY_BALANCE") },
                        label = { Text("🛒 Семейный баланс") }
                    )
                    SuggestionChip(
                        onClick = { applyStrategy("MAX_SAVINGS") },
                        label = { Text("📈 В копилку (Озон)") }
                    )
                    SuggestionChip(
                        onClick = { applyStrategy("EQUAL") },
                        label = { Text("⚖️ Поровну") }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Remaining status card
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = when {
                        remaining == 0.0 -> IncomeGreen.copy(alpha = 0.12f)
                        remaining > 0 -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                        else -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Распределено: ${CurrencyHelper.formatAmount(totalAllocated, "RUB")}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = when {
                                remaining == 0.0 -> "Всё распределено ✓"
                                remaining > 0 -> "Осталось: ${CurrencyHelper.formatAmount(remaining, "RUB")}"
                                else -> "Превышение: ${CurrencyHelper.formatAmount(-remaining, "RUB")}"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = when {
                                remaining == 0.0 -> IncomeGreen
                                remaining > 0 -> MaterialTheme.colorScheme.primary
                                else -> MaterialTheme.colorScheme.error
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Target Accounts Allocations List
                Text(
                    text = "Куда направить средства:",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                targetAccounts.forEach { targetAcc ->
                    val allocatedAmount = allocationsMap[targetAcc.id] ?: 0.0
                    val accColor = IconHelper.parseColor(targetAcc.colorHex)

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(accColor))
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = targetAcc.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "Баланс: ${CurrencyHelper.formatAmount(targetAcc.balance, targetAcc.currency)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            // Quick adjustment buttons and text
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = {
                                        val cur = allocationsMap[targetAcc.id] ?: 0.0
                                        allocationsMap[targetAcc.id] = (cur - 1000.0).coerceAtLeast(0.0)
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.Remove, contentDescription = null, modifier = Modifier.size(16.dp))
                                }

                                Text(
                                    text = "%.0f ₽".format(allocatedAmount),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 4.dp)
                                )

                                IconButton(
                                    onClick = {
                                        val cur = allocationsMap[targetAcc.id] ?: 0.0
                                        allocationsMap[targetAcc.id] = cur + 1000.0
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Отмена")
                    }
                    Button(
                        onClick = {
                            val validAllocations = allocationsMap.entries
                                .filter { it.value > 0 }
                                .map { it.key to it.value }
                            onConfirm(selectedSourceId, validAllocations)
                            onDismiss()
                        },
                        enabled = totalAllocated > 0,
                        modifier = Modifier
                            .weight(1.5f)
                            .testTag("confirm_distribution_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = IncomeGreen)
                    ) {
                        Icon(Icons.Default.CallSplit, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Распределить")
                    }
                }
            }
        }
    }
}
