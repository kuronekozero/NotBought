package com.example.mysavings // Замени com.example.mysavings на имя твоего пакета

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import java.time.LocalDate

// Конвертер для LocalDate, так как Room не умеет хранить его напрямую
class LocalDateConverter {
    @androidx.room.TypeConverter
    fun fromTimestamp(value: Long?): LocalDate? {
        return value?.let { LocalDate.ofEpochDay(it) }
    }

    @androidx.room.TypeConverter
    fun dateToTimestamp(date: LocalDate?): Long? {
        return date?.toEpochDay()
    }
}

@Database(entities = [SavingEntry::class], version = 1, exportSchema = false)
@TypeConverters(LocalDateConverter::class) // Регистрируем конвертер
abstract class AppDatabase : RoomDatabase() {

    abstract fun savingEntryDao(): SavingEntryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "my_savings_database" // Имя файла БД
                )
                    // .fallbackToDestructiveMigration() // Если будешь менять схему, для простоты на этапе разработки
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}