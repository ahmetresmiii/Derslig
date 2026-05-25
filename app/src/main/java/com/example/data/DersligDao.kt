package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface DersligDao {
    @Query("SELECT * FROM user_stats WHERE id = 1 LIMIT 1")
    fun getUserStats(): Flow<UserStats?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserStats(userStats: UserStats)

    @Update
    suspend fun updateUserStats(userStats: UserStats)

    @Query("SELECT * FROM completed_quizzes")
    fun getCompletedQuizzes(): Flow<List<CompletedQuiz>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCompletedQuiz(quiz: CompletedQuiz)

    @Query("SELECT * FROM shop_items")
    fun getShopItems(): Flow<List<ShopItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShopItems(items: List<ShopItem>)

    @Update
    suspend fun updateShopItem(item: ShopItem)
}
