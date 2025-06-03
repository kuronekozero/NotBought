package com.example.mysavings // Замени com.example.mysavings на имя твоего пакета

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "user_categories",
    indices = [Index(value = ["name"], unique = true)] // Имена категорий должны быть уникальными
)
data class UserCategory(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String
)