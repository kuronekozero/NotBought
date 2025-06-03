package com.example.mysavings // Замени com.example.mysavings на имя твоего пакета

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserCategoryDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE) // Игнорировать, если категория с таким именем уже есть (из-за unique index)
    suspend fun insert(category: UserCategory)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(categories: List<UserCategory>) // Для добавления дефолтных категорий

    @Delete
    suspend fun delete(category: UserCategory)

    @Query("SELECT * FROM user_categories ORDER BY name ASC")
    fun getAllCategories(): Flow<List<UserCategory>>

    @Query("SELECT COUNT(*) FROM user_categories")
    suspend fun getCategoryCount(): Int // Для проверки, есть ли категории (для добавления дефолтных)
}