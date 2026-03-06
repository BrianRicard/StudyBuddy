package com.studybuddy.core.data.network

import android.content.Context
import com.studybuddy.core.data.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class BundledDicteeList(
    val id: String,
    val unit: Int,
    val title: String,
    val language: String,
    val words: List<String>,
)

@Singleton
class BundledDicteeListLoader @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val json = Json { ignoreUnknownKeys = true }

    fun loadFrenchLists(): List<BundledDicteeList> {
        val text = context.resources.openRawResource(R.raw.dictee_fr_default)
            .bufferedReader()
            .use { it.readText() }
        return json.decodeFromString(text)
    }
}
