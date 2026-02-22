package com.studybuddy.core.data.backup

import com.studybuddy.core.data.db.StudyBuddyDatabase
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CsvExporter @Inject constructor(private val database: StudyBuddyDatabase) {
    suspend fun exportWordLists(profileId: String): String {
        val lists = database.dicteeDao().getAllLists().filter { it.profileId == profileId }
        val allWords = database.dicteeDao().getAllWords()

        val sb = StringBuilder()
        sb.appendLine("List,Language,Word,Mastered,Attempts,Correct Count")

        lists.forEach { list ->
            val wordsInList = allWords.filter { it.listId == list.id }
            wordsInList.forEach { word ->
                sb.appendLine(
                    "${escapeCsv(list.title)},${list.language},${escapeCsv(word.word)}," +
                        "${word.mastered},${word.attempts},${word.correctCount}",
                )
            }
        }

        return sb.toString()
    }

    private fun escapeCsv(value: String): String =
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            "\"${value.replace("\"", "\"\"")}\""
        } else {
            value
        }
}
