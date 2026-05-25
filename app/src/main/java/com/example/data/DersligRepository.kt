package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.lang.Math.max

class DersligRepository(private val dao: DersligDao) {

    val userStats: Flow<UserStats?> = dao.getUserStats()
    val completedQuizzes: Flow<List<CompletedQuiz>> = dao.getCompletedQuizzes()
    val shopItems: Flow<List<ShopItem>> = dao.getShopItems()

    suspend fun saveUserStats(userStats: UserStats) {
        dao.insertUserStats(userStats)
    }

    suspend fun changeGrade(grade: String) {
        val stats = dao.getUserStats().firstOrNull() ?: UserStats()
        dao.insertUserStats(stats.copy(selectedGrade = grade))
    }

    suspend fun updateUsername(newName: String) {
        val stats = dao.getUserStats().firstOrNull() ?: UserStats()
        dao.insertUserStats(stats.copy(username = newName))
    }

    suspend fun completeQuiz(
        quizId: String,
        correctCount: Int,
        wrongCount: Int,
        scorePercent: Int,
        xpEarned: Int,
        coinsEarned: Int
    ) {
        // Save the completed quiz record
        val quizRecord = CompletedQuiz(
            quizId = quizId,
            scorePercent = scorePercent,
            correctCount = correctCount,
            wrongCount = wrongCount,
            coinsEarned = coinsEarned,
            xpEarned = xpEarned
        )
        dao.insertCompletedQuiz(quizRecord)

        // Fetch current user stats and update
        val stats = dao.getUserStats().firstOrNull() ?: UserStats()
        val updatedStats = stats.copy(
            xp = stats.xp + xpEarned,
            coins = stats.coins + coinsEarned
        )
        dao.insertUserStats(updatedStats)
    }

    suspend fun purchaseShopItem(item: ShopItem, currentStats: UserStats): Boolean {
        if (currentStats.coins < item.cost) return false

        // Deduct coins from user
        val updatedStats = currentStats.copy(
            coins = max(0, currentStats.coins - item.cost),
            // Automatically set border if it's a border category item
            avatarBorder = if (item.category == "BORDER") item.title else currentStats.avatarBorder
        )
        dao.insertUserStats(updatedStats)

        // Mark item as purchased
        val updatedItem = item.copy(isPurchased = true)
        dao.updateShopItem(updatedItem)

        return true
    }

    suspend fun selectBorder(borderTitle: String) {
        val stats = dao.getUserStats().firstOrNull() ?: UserStats()
        dao.insertUserStats(stats.copy(avatarBorder = borderTitle))
    }

    suspend fun completeOnboarding(name: String, grade: String) {
        val stats = dao.getUserStats().firstOrNull() ?: UserStats()
        dao.insertUserStats(stats.copy(username = name, selectedGrade = grade, onboardingCompleted = true))
    }
}
