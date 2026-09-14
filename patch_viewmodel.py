import re

with open('app/src/main/java/com/example/ui/viewmodel/FinanceViewModel.kt', 'r') as f:
    content = f.read()

# Update addTransaction signature
add_old = r"""    fun addTransaction\(
        type: String,
        amount: Double,
        accountId: Long,
        toAccountId: Long\? = null,
        categoryId: Long\? = null,
        timestamp: Long = System\.currentTimeMillis\(\),
        note: String = "",
        tag: String = "",
        excludeFromStats: Boolean = false
    \) \{
        viewModelScope\.launch \{
            repository\.addTransaction\(
                TransactionEntity\(
                    type = type,
                    amount = amount,
                    accountId = accountId,
                    toAccountId = toAccountId,
                    categoryId = categoryId,
                    timestamp = timestamp,
                    note = note,
                    tag = tag,
                    excludeFromStats = excludeFromStats
                \)
            \)"""

add_new = """    fun addTransaction(
        type: String,
        amount: Double,
        accountId: Long,
        toAccountId: Long? = null,
        categoryId: Long? = null,
        timestamp: Long = System.currentTimeMillis(),
        note: String = "",
        tag: String = "",
        excludeFromStats: Boolean = false,
        goalId: Long? = null,
        debtId: Long? = null
    ) {
        viewModelScope.launch {
            repository.addTransaction(
                TransactionEntity(
                    type = type,
                    amount = amount,
                    accountId = accountId,
                    toAccountId = toAccountId,
                    categoryId = categoryId,
                    timestamp = timestamp,
                    note = note,
                    tag = tag,
                    excludeFromStats = excludeFromStats,
                    goalId = goalId,
                    debtId = debtId
                )
            )"""
content = re.sub(add_old, add_new, content)

# updateTransaction signature doesn't need to change because it takes TransactionEntity directly

with open('app/src/main/java/com/example/ui/viewmodel/FinanceViewModel.kt', 'w') as f:
    f.write(content)
