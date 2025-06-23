package com.example.mysavings // Замени com.example.mysavings на имя твоего пакета

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import androidx.room.Delete
import androidx.room.Update

@Dao
interface SavingEntryDao {
    @Insert
    suspend fun insert(entry: SavingEntry)

    @Query("SELECT * FROM saving_entries ORDER BY date DESC")
    fun getAllEntries(): Flow<List<SavingEntry>>

    @Query("SELECT SUM(cost) FROM saving_entries")
    fun getTotalSaved(): Flow<Double?> // Этот у нас уже есть

    // Новый метод для получения суммы за конкретный день
    @Query("SELECT SUM(cost) FROM saving_entries WHERE date = :specificDate")
    fun getTotalSavedForDate(specificDate: LocalDate): Flow<Double?>

    // Новый метод для получения суммы за диапазон дат (понадобится для недели, месяца, года)
    @Query("SELECT SUM(cost) FROM saving_entries WHERE date BETWEEN :startDate AND :endDate")
    fun getTotalSavedBetweenDates(startDate: LocalDate, endDate: LocalDate): Flow<Double?>

    @Query("SELECT category AS categoryName, SUM(cost) AS totalAmount FROM saving_entries GROUP BY category ORDER BY totalAmount DESC")
    fun getSavingsPerCategory(): Flow<List<CategorySavings>>

    @Query("SELECT * FROM saving_entries")
    suspend fun getAllEntriesList(): List<SavingEntry>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entries: List<SavingEntry>)

    @Update
    suspend fun update(entry: SavingEntry)

    @Delete
    suspend fun delete(entry: SavingEntry)

    @Query("SELECT * FROM saving_entries WHERE id = :id")
    suspend fun getEntryById(id: Int): SavingEntry?
}