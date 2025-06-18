package com.example.mysavings // Замени com.example.mysavings на имя твоего пакета

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import java.time.LocalDate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.room.TypeConverter
import java.time.LocalDateTime

class LocalDateTimeConverter {
    @TypeConverter
    fun fromTimestamp(value: Long?): LocalDateTime? {
        return value?.let { LocalDateTime.ofEpochSecond(it, 0, java.time.ZoneOffset.UTC) }
    }

    @TypeConverter
    fun dateToTimestamp(date: LocalDateTime?): Long? {
        return date?.toEpochSecond(java.time.ZoneOffset.UTC)
    }
}

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

@Database(
    entities = [SavingEntry::class, UserCategory::class, Goal::class],
    version = 4,
    exportSchema = false
)
@TypeConverters(LocalDateConverter::class, LocalDateTimeConverter::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun savingEntryDao(): SavingEntryDao
    abstract fun userCategoryDao(): UserCategoryDao
    abstract fun goalDao(): GoalDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "my_savings_database"
                )
                    .fallbackToDestructiveMigration() // <<<--- ВАЖНО: для простоты при изменении версии
                    // Если база категорий пуста, добавляем дефолтные
                    // Этот коллбэк выполнится после создания или открытия БД
                    .addCallback(object : Callback() {
                        override fun onCreate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Заполняем дефолтными категориями при создании БД
                            // Делаем это в корутине, чтобы не блокировать основной поток
                            INSTANCE?.let { database ->
                                CoroutineScope(Dispatchers.IO).launch {
                                    val categoryDao = database.userCategoryDao()
                                    if (categoryDao.getCategoryCount() == 0) { // Проверяем, только если категории пусты
                                        categoryDao.insertAll(DefaultCategories.getList())
                                    }
                                }
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

// Создадим объект для дефолтных категорий
object DefaultCategories {
    fun getList(): List<UserCategory> {
        return listOf(
            UserCategory(name = "Еда и напитки"),
            UserCategory(name = "Развлечения"),
            UserCategory(name = "Покупки (вещи)"),
            UserCategory(name = "Транспорт"),
            UserCategory(name = "Хобби"),
            UserCategory(name = "Здоровье"),
            UserCategory(name = "Дом"),
            UserCategory(name = "Образование"),
            UserCategory(name = "Другое")
        )
    }
}