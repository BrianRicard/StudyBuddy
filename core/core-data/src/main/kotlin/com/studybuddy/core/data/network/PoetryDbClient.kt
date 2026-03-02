package com.studybuddy.core.data.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.json.Json

private const val BASE_URL = "https://poetrydb.org"
private const val TIMEOUT_MS = 10_000L
private const val MAX_CHILD_LINES = 20
private const val RANDOM_FETCH_COUNT = 20

@Singleton
class PoetryDbClient @Inject constructor() {

    private val client = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                },
            )
        }
        install(HttpTimeout) {
            requestTimeoutMillis = TIMEOUT_MS
            connectTimeoutMillis = TIMEOUT_MS
        }
    }

    suspend fun getRandomPoems(count: Int = RANDOM_FETCH_COUNT): List<PoetryDbPoem> =
        client.get("$BASE_URL/random/$count").body()

    suspend fun searchByTitle(keyword: String): List<PoetryDbPoem> = client.get("$BASE_URL/title/$keyword").body()

    suspend fun getByAuthor(author: String): List<PoetryDbPoem> = client.get("$BASE_URL/author/$author").body()

    suspend fun getByLineCount(lineCount: Int): List<PoetryDbPoem> = client.get("$BASE_URL/linecount/$lineCount").body()

    suspend fun getChildFriendlyPoems(): List<PoetryDbPoem> {
        val poems = getRandomPoems()
        return poems.filter { it.lines.size <= MAX_CHILD_LINES }
    }

    fun close() {
        client.close()
    }
}
