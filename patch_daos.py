import re

with open('app/src/main/java/com/example/data/dao/FinanceDaos.kt', 'r') as f:
    content = f.read()

# Add getGoalById
goal_old = r"""    @Query\("SELECT \* FROM goals ORDER BY id ASC"\)
    fun getAllGoals\(\): Flow<List<GoalEntity>>"""
goal_new = """    @Query("SELECT * FROM goals ORDER BY id ASC")
    fun getAllGoals(): Flow<List<GoalEntity>>

    @Query("SELECT * FROM goals WHERE id = :id")
    suspend fun getGoalById(id: Long): GoalEntity?"""
content = re.sub(goal_old, goal_new, content)

# Add getDebtById
debt_old = r"""    @Query\("SELECT \* FROM debts ORDER BY isSettled ASC, id DESC"\)
    fun getAllDebts\(\): Flow<List<DebtEntity>>"""
debt_new = """    @Query("SELECT * FROM debts ORDER BY isSettled ASC, id DESC")
    fun getAllDebts(): Flow<List<DebtEntity>>

    @Query("SELECT * FROM debts WHERE id = :id")
    suspend fun getDebtById(id: Long): DebtEntity?"""
content = re.sub(debt_old, debt_new, content)

with open('app/src/main/java/com/example/data/dao/FinanceDaos.kt', 'w') as f:
    f.write(content)
