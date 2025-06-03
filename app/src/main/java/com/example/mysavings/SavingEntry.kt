package com.example.mysavings // Замени com.example.mysavings на имя твоего пакета

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate // Используем java.time для работы с датой

@Entity(tableName = "saving_entries")
data class SavingEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val itemName: String,
    val cost: Double,
    val category: String, // Пока просто строка, позже можно усложнить
    val date: LocalDate = LocalDate.now() // Дата добавления записи
)