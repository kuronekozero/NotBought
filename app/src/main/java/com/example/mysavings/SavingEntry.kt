package com.example.mysavings // Замени com.example.mysavings на имя твоего пакета

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "saving_entries")
data class SavingEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val itemName: String,
    val cost: Double,
    val category: String,
    val date: LocalDateTime = LocalDateTime.now()
)