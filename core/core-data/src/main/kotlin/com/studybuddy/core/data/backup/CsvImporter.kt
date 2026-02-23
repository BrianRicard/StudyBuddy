package com.studybuddy.core.data.backup

import com.studybuddy.core.common.constants.AppConstants
import com.studybuddy.core.data.db.StudyBuddyDatabase
import com.studybuddy.core.data.db.entity.DicteeListEntity
import com.studybuddy.core.data.db.entity.DicteeWordEntity
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.datetime.Clock

/**
 * Imports dictée word lists from CSV format.
 *
 * Expected CSV format (with header):
 * ```
 * List,Language,Word,Mastered,Attempts,Correct Count
 * Les Animaux,fr,chat,false,3,1
 * Les Animaux,fr,maison,true,8,8
 * Week 2,en,red,false,0,0
 * ```
 *
 * If `Mastered`, `Attempts`, and `Correct Count` columns are missing,
 * words are imported with default values (not mastered, 0 attempts).
 * Minimal format (just list + words):
 * ```
 * List,Language,Word
 * Les Animaux,fr,chat
 * ```
 */
@Singleton
class CsvImporter @Inject constructor(private val database: StudyBuddyDatabase) {

    /**
     * Parses CSV content and inserts lists and words into the database.
     *
     * @return the number of words imported
     */
    suspend fun importWordLists(
        csvContent: String,
        profileId: String,
    ): Int {
        val lines = csvContent.lines()
            .map { it.trim() }
            .filter { it.isNotBlank() }

        if (lines.size < 2) return 0

        val rows = lines.drop(1).map { parseCsvLine(it) }
        val now = Clock.System.now().toEpochMilliseconds()

        // Group words by (list title, language)
        val listsMap = mutableMapOf<Pair<String, String>, MutableList<ParsedWord>>()
        for (row in rows) {
            if (row.size < 3) continue
            val listTitle = row[0]
            val language = row[1]
            val word = row[2]
            if (listTitle.isBlank() || word.isBlank()) continue
            if (word.length > AppConstants.MAX_WORD_LENGTH) continue

            val mastered = row.getOrNull(3)?.toBooleanStrictOrNull() ?: false
            val attempts = row.getOrNull(4)?.toIntOrNull() ?: 0
            val correctCount = row.getOrNull(5)?.toIntOrNull() ?: 0

            val key = Pair(listTitle, language)
            listsMap.getOrPut(key) { mutableListOf() }
                .add(ParsedWord(word, mastered, attempts, correctCount))
        }

        var importedCount = 0

        for ((key, words) in listsMap) {
            val (title, language) = key
            val listId = UUID.randomUUID().toString()

            database.dicteeDao().insertList(
                DicteeListEntity(
                    id = listId,
                    profileId = profileId,
                    title = title,
                    language = language,
                    createdAt = now,
                    updatedAt = now,
                ),
            )

            for (parsedWord in words) {
                database.dicteeDao().insertWord(
                    DicteeWordEntity(
                        id = UUID.randomUUID().toString(),
                        listId = listId,
                        word = parsedWord.word,
                        mastered = parsedWord.mastered,
                        attempts = parsedWord.attempts,
                        correctCount = parsedWord.correctCount,
                    ),
                )
                importedCount++
            }
        }

        return importedCount
    }
}

private data class ParsedWord(
    val word: String,
    val mastered: Boolean,
    val attempts: Int,
    val correctCount: Int,
)

/**
 * Parses a single CSV line respecting quoted fields.
 */
internal fun parseCsvLine(line: String): List<String> {
    val fields = mutableListOf<String>()
    val current = StringBuilder()
    var inQuotes = false
    var i = 0

    while (i < line.length) {
        val c = line[i]
        when {
            c == '"' && inQuotes && i + 1 < line.length && line[i + 1] == '"' -> {
                current.append('"')
                i += 2
            }
            c == '"' -> {
                inQuotes = !inQuotes
                i++
            }
            c == ',' && !inQuotes -> {
                fields.add(current.toString())
                current.clear()
                i++
            }
            else -> {
                current.append(c)
                i++
            }
        }
    }
    fields.add(current.toString())

    return fields
}
