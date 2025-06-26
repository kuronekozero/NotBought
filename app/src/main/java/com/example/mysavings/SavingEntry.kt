package com.example.mysavings

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime
import java.time.LocalDate

@Entity(tableName = "saving_entries")
data class SavingEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val itemName: String,
    val cost: Double,
    val category: String,
    val date: LocalDateTime = LocalDateTime.now()
)
fun LocalDateTime.toLocalDate(): LocalDate = this.toLocalDate()