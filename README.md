<div align="center">
  <img src="gemini-svg.svg" alt="ManiMani Logo" width="120" height="120" />
  <h1>ManiMani</h1>
  <p><b>Smart Personal Finance Tracker with AI Assistant & Bank Parsing</b></p>
</div>

## 🌟 Overview
**ManiMani** is a modern, privacy-focused personal finance tracking application built natively for Android. It automates your expense tracking by intelligently parsing bank notifications and provides deep insights into your financial health using Gemini AI.

## ✨ Key Features
*   **🤖 Gemini AI Assistant:** Chat with your smart financial assistant to analyze your spending, get budgeting advice, and summarize your monthly expenses.
*   **📲 Automatic Bank Sync (via Notifications):** Listens to incoming bank push notifications and automatically categorizes and logs your transactions.
*   **📊 Payday Cycles & Budget Planning:** Plan your budget intelligently based on your actual payday cycles rather than strict calendar months.
*   **📈 Rich Analytics:** Visualize your income and expense distributions with detailed charts and breakdown sheets.
*   **🎨 Modern UI:** Built entirely with Jetpack Compose using Material Design 3 guidelines for a buttery smooth, adaptive, and beautiful user experience.
*   **🔒 Local-First Privacy:** All your sensitive financial data is stored securely on-device using Room Database.

## 🛠 Tech Stack
*   **Language:** [Kotlin](https://kotlinlang.org/) (100%)
*   **UI Toolkit:** [Jetpack Compose](https://developer.android.com/jetpack/compose)
*   **Architecture:** MVVM (Model-View-ViewModel) + Clean Architecture principles
*   **Database:** [Room](https://developer.android.com/training/data-storage/room) (SQLite)
*   **Asynchronous Programming:** Kotlin Coroutines & Flow
*   **AI Integration:** Google Gemini API SDK

## 🚀 Getting Started

### Prerequisites
*   Android Studio (Latest stable version recommended)
*   Android SDK 34+
*   Gemini API Key (for the AI Assistant features)

### Installation
1.  **Clone the repository:**
    ```bash
    git clone https://github.com/yourusername/ManiMani.git
    cd ManiMani
    ```
2.  **Set up Gemini API Key:**
    Add your Gemini API key to the application settings directly within the app's Gemini Assistant screen, or enter it when prompted by the app.
3.  **Build and Run:**
    Open the project in Android Studio, sync Gradle, and run the `app` configuration on your emulator or physical device.

## 📂 Project Structure
*   `app/src/main/java/com/example/ui/` - Jetpack Compose UI screens and components
*   `app/src/main/java/com/example/data/` - Room database, DAOs, and Repositories
*   `app/src/main/java/com/example/service/` - Notification listening, Background Workers, and Gemini integration
*   `scripts/` - Utility scripts and historical migration patches

## 🛡 License
This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
