package com.studybuddy.core.data.backup

import android.content.Context
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import com.studybuddy.core.data.db.StudyBuddyDatabase
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.ByteArrayOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PdfReportGenerator @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: StudyBuddyDatabase,
) {
    suspend fun generateReport(profileId: String): ByteArray {
        val profile = database.profileDao().getAllProfiles().firstOrNull { it.id == profileId }
        val dicteeLists = database.dicteeDao().getAllLists().filter { it.profileId == profileId }
        val allWords = database.dicteeDao().getAllWords()
        val mathSessions = database.mathDao().getAllSessions().filter { it.profileId == profileId }

        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val page = document.startPage(pageInfo)

        drawReport(
            canvas = page.canvas,
            profileName = profile?.name ?: "StudyBuddy",
            dicteeLists = dicteeLists.size,
            totalWords = allWords.count { word -> dicteeLists.any { it.id == word.listId } },
            masteredWords = allWords.count { word ->
                word.mastered && dicteeLists.any { it.id == word.listId }
            },
            mathSessions = mathSessions.size,
            avgAccuracy = if (mathSessions.isNotEmpty()) {
                mathSessions.map { it.correctCount.toFloat() / it.totalProblems.coerceAtLeast(1) }
                    .average()
                    .toFloat()
            } else {
                0f
            },
            totalPoints = profile?.totalPoints ?: 0L,
        )

        document.finishPage(page)

        val outputStream = ByteArrayOutputStream()
        document.writeTo(outputStream)
        document.close()

        return outputStream.toByteArray()
    }

    @Suppress("LongParameterList")
    private fun drawReport(
        canvas: android.graphics.Canvas,
        profileName: String,
        dicteeLists: Int,
        totalWords: Int,
        masteredWords: Int,
        mathSessions: Int,
        avgAccuracy: Float,
        totalPoints: Long,
    ) {
        val titlePaint = Paint().apply {
            textSize = TITLE_TEXT_SIZE
            isFakeBoldText = true
            isAntiAlias = true
        }
        val headerPaint = Paint().apply {
            textSize = HEADER_TEXT_SIZE
            isFakeBoldText = true
            isAntiAlias = true
        }
        val bodyPaint = Paint().apply {
            textSize = BODY_TEXT_SIZE
            isAntiAlias = true
        }

        var y = MARGIN_TOP

        canvas.drawText("StudyBuddy Progress Report", MARGIN_LEFT, y, titlePaint)
        y += LINE_SPACING_TITLE

        canvas.drawText("Student: $profileName", MARGIN_LEFT, y, bodyPaint)
        y += LINE_SPACING_LARGE
        canvas.drawText("Total Stars: $totalPoints", MARGIN_LEFT, y, bodyPaint)
        y += LINE_SPACING_LARGE

        canvas.drawText("Dictee", MARGIN_LEFT, y, headerPaint)
        y += LINE_SPACING_NORMAL
        canvas.drawText("Word Lists: $dicteeLists", MARGIN_LEFT, y, bodyPaint)
        y += LINE_SPACING_NORMAL
        canvas.drawText("Total Words: $totalWords", MARGIN_LEFT, y, bodyPaint)
        y += LINE_SPACING_NORMAL
        canvas.drawText("Mastered Words: $masteredWords", MARGIN_LEFT, y, bodyPaint)
        y += LINE_SPACING_NORMAL
        val wordAccuracy = if (totalWords > 0) {
            "%.0f%%".format(masteredWords.toFloat() / totalWords * PERCENT_MULTIPLIER)
        } else {
            "N/A"
        }
        canvas.drawText("Accuracy: $wordAccuracy", MARGIN_LEFT, y, bodyPaint)
        y += LINE_SPACING_LARGE

        canvas.drawText("Speed Math", MARGIN_LEFT, y, headerPaint)
        y += LINE_SPACING_NORMAL
        canvas.drawText("Sessions Completed: $mathSessions", MARGIN_LEFT, y, bodyPaint)
        y += LINE_SPACING_NORMAL
        val mathAccuracyStr = if (mathSessions > 0) {
            "%.0f%%".format(avgAccuracy * PERCENT_MULTIPLIER)
        } else {
            "N/A"
        }
        canvas.drawText("Average Accuracy: $mathAccuracyStr", MARGIN_LEFT, y, bodyPaint)
    }

    companion object {
        private const val PAGE_WIDTH = 595
        private const val PAGE_HEIGHT = 842
        private const val MARGIN_LEFT = 40f
        private const val MARGIN_TOP = 60f
        private const val TITLE_TEXT_SIZE = 24f
        private const val HEADER_TEXT_SIZE = 18f
        private const val BODY_TEXT_SIZE = 14f
        private const val LINE_SPACING_TITLE = 50f
        private const val LINE_SPACING_LARGE = 40f
        private const val LINE_SPACING_NORMAL = 25f
        private const val PERCENT_MULTIPLIER = 100f
    }
}
