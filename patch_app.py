import re

with open('app/src/main/java/com/example/ui/ManiManiApp.kt', 'r') as f:
    content = f.read()

# Add Transaction Dialog calls
# We need to change the arguments and the lambda.
add_old = r"""        AddTransactionDialog\(
            accounts = state\.accounts,
            categories = state\.categories,
            bankOfTheMonth = state\.bankOfTheMonth,
            onDismiss = \{ showAddTransactionDialog = false \},
            onManageCategories = \{ showManageCategoriesDialog = true \},
            onConfirm = \{ type, amount, accId, toAccId, catId, note, tag, exclude ->
                viewModel\.addTransaction\(
                    type = type,
                    amount = amount,
                    accountId = accId,
                    toAccountId = toAccId,
                    categoryId = catId,
                    note = note,
                    tag = tag,
                    excludeFromStats = exclude
                \)
            \}
        \)"""

add_new = """        AddTransactionDialog(
            accounts = state.accounts,
            categories = state.categories,
            goals = state.goals,
            debts = state.debts,
            bankOfTheMonth = state.bankOfTheMonth,
            onDismiss = { showAddTransactionDialog = false },
            onManageCategories = { showManageCategoriesDialog = true },
            onConfirm = { type, amount, accId, toAccId, catId, note, tag, exclude, goalId, debtId ->
                viewModel.addTransaction(
                    type = type,
                    amount = amount,
                    accountId = accId,
                    toAccountId = toAccId,
                    categoryId = catId,
                    note = note,
                    tag = tag,
                    excludeFromStats = exclude,
                    goalId = goalId,
                    debtId = debtId
                )
            }
        )"""
content = re.sub(add_old, add_new, content)

edit_old = r"""        AddTransactionDialog\(
            accounts = state\.accounts,
            categories = state\.categories,
            bankOfTheMonth = state\.bankOfTheMonth,
            transactionToEdit = txToEdit,
            onDismiss = \{ transactionToEdit = null \},
            onManageCategories = \{ showManageCategoriesDialog = true \},
            onConfirm = \{ type, amount, accId, toAccId, catId, note, tag, exclude ->
                val updated = txToEdit\.copy\(
                    type = type,
                    amount = amount,
                    accountId = accId,
                    toAccountId = toAccId,
                    categoryId = catId,
                    note = note,
                    tag = tag,
                    excludeFromStats = exclude
                \)
                viewModel\.updateTransaction\(txToEdit, updated\)
                transactionToEdit = null
            \}
        \)"""

edit_new = """        AddTransactionDialog(
            accounts = state.accounts,
            categories = state.categories,
            goals = state.goals,
            debts = state.debts,
            bankOfTheMonth = state.bankOfTheMonth,
            transactionToEdit = txToEdit,
            onDismiss = { transactionToEdit = null },
            onManageCategories = { showManageCategoriesDialog = true },
            onConfirm = { type, amount, accId, toAccId, catId, note, tag, exclude, goalId, debtId ->
                val updated = txToEdit.copy(
                    type = type,
                    amount = amount,
                    accountId = accId,
                    toAccountId = toAccId,
                    categoryId = catId,
                    note = note,
                    tag = tag,
                    excludeFromStats = exclude,
                    goalId = goalId,
                    debtId = debtId
                )
                viewModel.updateTransaction(txToEdit, updated)
                transactionToEdit = null
            }
        )"""
content = re.sub(edit_old, edit_new, content)

with open('app/src/main/java/com/example/ui/ManiManiApp.kt', 'w') as f:
    f.write(content)
