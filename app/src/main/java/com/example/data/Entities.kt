package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_stats")
data class UserStats(
    @PrimaryKey val id: Int = 1,
    val username: String = "Süper Öğrenci",
    val selectedGrade: String = "8. Sınıf (LGS)",
    val xp: Int = 0,
    val coins: Int = 100,
    val streak: Int = 1,
    val avatarBorder: String = "None", // "Gold", "Diamond", "Fire"
    val avatarRes: String = "student_avatar",
    val lastActiveDate: Long = System.currentTimeMillis(),
    val onboardingCompleted: Boolean = false
)

@Entity(tableName = "completed_quizzes")
data class CompletedQuiz(
    @PrimaryKey val quizId: String,
    val scorePercent: Int,
    val correctCount: Int,
    val wrongCount: Int,
    val coinsEarned: Int,
    val xpEarned: Int,
    val completedTime: Long = System.currentTimeMillis()
)

@Entity(tableName = "shop_items")
data class ShopItem(
    @PrimaryKey val itemId: String,
    val title: String,
    val category: String, // "BORDER", "REWARD", "BG"
    val cost: Int,
    val isPurchased: Boolean = false,
    val description: String = ""
)
