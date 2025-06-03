package com.example.mysavings // Замени com.example.mysavings на имя твоего пакета

enum class Category(val displayName: String) {
    FOOD_DRINKS("Еда и напитки"),
    ENTERTAINMENT("Развлечения"),
    SHOPPING("Покупки (вещи)"),
    TRANSPORT("Транспорт"),
    HOBBIES("Хобби"),
    OTHER("Другое");

    // Это позволит нам легко получить список названий для отображения в UI
    companion object {
        fun getDisplayNames(): List<String> = entries.map { it.displayName }
    }
}