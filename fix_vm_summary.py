with open('app/src/main/java/com/example/ui/viewmodel/FinanceViewModel.kt', 'r') as f:
    content = f.read()

import re

# replace isEveningSummaryEnabled / setEveningSummaryEnabled
new_methods = """    private val _isEveningSummaryEnabled = MutableStateFlow(userFinancePrefs.isEveningSummaryEnabled())
    val isEveningSummaryEnabledFlow: StateFlow<Boolean> = _isEveningSummaryEnabled.asStateFlow()

    private val _eveningSummaryTime = MutableStateFlow(userFinancePrefs.getEveningSummaryTime())
    val eveningSummaryTimeFlow: StateFlow<String> = _eveningSummaryTime.asStateFlow()

    fun isEveningSummaryEnabled(): Boolean {
        return _isEveningSummaryEnabled.value
    }

    fun setEveningSummaryEnabled(enabled: Boolean) {
        userFinancePrefs.setEveningSummaryEnabled(enabled)
        _isEveningSummaryEnabled.value = enabled
    }

    fun getEveningSummaryTime(): String {
        return _eveningSummaryTime.value
    }

    fun setEveningSummaryTime(time: String) {
        userFinancePrefs.setEveningSummaryTime(time)
        _eveningSummaryTime.value = time
    }"""

pattern = r'\s*fun isEveningSummaryEnabled\(\): Boolean \{.*?\n\s*\}\s*fun setEveningSummaryEnabled\(enabled: Boolean\) \{.*?\n\s*\}\s*fun getEveningSummaryTime\(\): String \{.*?\n\s*\}\s*fun setEveningSummaryTime\(time: String\) \{.*?\n\s*\}'
content = re.sub(pattern, "\n" + new_methods, content, flags=re.DOTALL)

with open('app/src/main/java/com/example/ui/viewmodel/FinanceViewModel.kt', 'w') as f:
    f.write(content)
