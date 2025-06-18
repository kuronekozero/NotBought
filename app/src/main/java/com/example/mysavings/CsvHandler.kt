package com.example.mysavings

import java.io.InputStream
import java.io.OutputStream
import java.time.LocalDate
import java.time.format.DateTimeParseException

object CsvHandler {

    private const val CSV_HEADER = "id,itemName,cost,category,date"

    fun writeCsv(outputStream: OutputStream, entries: List<SavingEntry>) {
        outputStream.bufferedWriter().use { writer ->
            writer.write(CSV_HEADER)
            writer.newLine()
            entries.forEach { entry ->
                val line = "${entry.id},\"${entry.itemName.replace("\"", "\"\"")}\",${entry.cost},\"${entry.category.replace("\"", "\"\"")}\",${entry.date}"
                writer.write(line)
                writer.newLine()
            }
        }
    }

    fun readCsv(inputStream: InputStream): List<SavingEntry> {
        val entries = mutableListOf<SavingEntry>()
        inputStream.bufferedReader().useLines { lines ->
            lines.drop(1) // Пропускаем заголовок
                .forEach { line ->
                    try {
                        // Простой парсер, который не поддерживает запятые внутри кавычек
                        val tokens = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)".toRegex())
                        if (tokens.size >= 5) {
                            val entry = SavingEntry(
                                id = 0, // Игнорируем id из файла, Room сгенерирует новый
                                itemName = tokens[1].trim('"'),
                                cost = tokens[2].toDouble(),
                                category = tokens[3].trim('"'),
                                date = LocalDate.parse(tokens[4])
                            )
                            entries.add(entry)
                        }
                    } catch (e: Exception) {
                        // Пропускаем строки с ошибками парсинга
                        e.printStackTrace()
                    }
                }
        }
        return entries
    }
}