package com.studybuddy.core.data.network

import android.content.Context
import com.studybuddy.core.data.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class BundledPoem(
    val title: String,
    val author: String,
    val lines: List<String>,
    val tags: List<String> = emptyList(),
)

@Singleton
class BundledPoemLoader @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val json = Json { ignoreUnknownKeys = true }

    fun loadFrenchPoems(): List<BundledPoem> = loadFromRaw(R.raw.poems_fr)

    fun loadGermanPoems(): List<BundledPoem> = loadFromRaw(R.raw.poems_de)

    private fun loadFromRaw(resId: Int): List<BundledPoem> {
        val text = context.resources.openRawResource(resId)
            .bufferedReader()
            .use { it.readText() }
        return json.decodeFromString(text)
    }
}
