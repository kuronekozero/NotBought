package com.example.mysavings

import java.io.InputStream
import java.io.OutputStream
import java.time.LocalDateTime

object CsvHandler {

    private const val SECTION_ENTRIES = "#ENTRIES"
    private const val SECTION_GOALS = "#GOALS"
    private const val SECTION_CATEGORIES = "#CATEGORIES"

    // region ---------- EXPORT ----------

    fun writeBackup(
        outputStream: OutputStream,
        entries: List<SavingEntry>,
        goals: List<Goal>,
        categories: List<UserCategory>
    ) {
        outputStream.bufferedWriter().use { writer ->
            // Saving entries
            writer.write(SECTION_ENTRIES)
            writer.newLine()
            writer.write("itemName,cost,category,date")
            writer.newLine()
            entries.forEach { e ->
                writer.write(
                    "\"${escape(e.itemName)}\",${e.cost},\"${escape(e.category)}\",${e.date}"
                )
                writer.newLine()
            }

            // Goals
            writer.write(SECTION_GOALS)
            writer.newLine()
            writer.write("name,description,targetAmount,creationDate,savingsStartDate")
            writer.newLine()
            goals.forEach { g ->
                writer.write(
                    "\"${escape(g.name)}\",\"${escape(g.description ?: "")}\",${g.targetAmount},${g.creationDate},${g.savingsStartDate}"
                )
                writer.newLine()
            }

            // Categories
            writer.write(SECTION_CATEGORIES)
            writer.newLine()
            writer.write("name")
            writer.newLine()
            categories.forEach { c ->
                writer.write("\"${escape(c.name)}\"")
                writer.newLine()
            }
        }
    }

    private fun escape(value: String): String = value.replace("\"", "\"\"")

    // endregion

    // region ---------- IMPORT ----------

    data class BackupData(
        val entries: List<SavingEntry>,
        val goals: List<Goal>,
        val categories: List<UserCategory>
    )

    fun readBackup(inputStream: InputStream): BackupData {
        val entries = mutableListOf<SavingEntry>()
        val goals = mutableListOf<Goal>()
        val categories = mutableListOf<UserCategory>()

        var currentSection: String? = null
        inputStream.bufferedReader().useLines { lines ->
            lines.forEach { rawLine ->
                val line = rawLine.trim()
                if (line.isEmpty()) return@forEach
                when {
                    line == SECTION_ENTRIES || line == SECTION_GOALS || line == SECTION_CATEGORIES -> {
                        currentSection = line
                    }
                    line.startsWith("#") -> {
                        // Unknown section – skip
                        currentSection = null
                    }
                    line.startsWith("itemName") || line.startsWith("name") -> {
                        // header rows – skip
                    }
                    else -> {
                        when (currentSection) {
                            SECTION_ENTRIES -> parseEntry(line)?.let(entries::add)
                            SECTION_GOALS -> parseGoal(line)?.let(goals::add)
                            SECTION_CATEGORIES -> parseCategory(line)?.let(categories::add)
                        }
                    }
                }
            }
        }
        return BackupData(entries, goals, categories)
    }

    private fun splitCsvLine(line: String): List<String> =
        line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)".toRegex())
            .map { it.trim().trim('"') }

    private fun parseEntry(line: String): SavingEntry? {
        val tokens = splitCsvLine(line)
        return if (tokens.size >= 4) try {
            SavingEntry(
                itemName = tokens[0],
                cost = tokens[1].toDouble(),
                category = tokens[2],
                date = LocalDateTime.parse(tokens[3])
            )
        } catch (e: Exception) {
            null
        } else null
    }

    private fun parseGoal(line: String): Goal? {
        val tokens = splitCsvLine(line)
        return if (tokens.size >= 5) try {
            Goal(
                name = tokens[0],
                description = tokens[1].ifEmpty { null },
                targetAmount = tokens[2].toDouble(),
                creationDate = LocalDateTime.parse(tokens[3]),
                savingsStartDate = LocalDateTime.parse(tokens[4])
            )
        } catch (e: Exception) {
            null
        } else null
    }

    private fun parseCategory(line: String): UserCategory? {
        val tokens = splitCsvLine(line)
        return if (tokens.isNotEmpty()) UserCategory(name = tokens[0]) else null
    }

    // endregion
}