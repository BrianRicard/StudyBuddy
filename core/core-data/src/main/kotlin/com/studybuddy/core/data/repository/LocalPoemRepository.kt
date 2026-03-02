package com.studybuddy.core.data.repository

import com.studybuddy.core.data.db.dao.PoemDao
import com.studybuddy.core.data.db.entity.CachedPoemEntity
import com.studybuddy.core.data.db.entity.FavouritePoemEntity
import com.studybuddy.core.data.mapper.toDomain
import com.studybuddy.core.data.mapper.toEntity
import com.studybuddy.core.data.mapper.toUserEntity
import com.studybuddy.core.data.network.BundledPoemLoader
import com.studybuddy.core.data.network.PoetryDbClient
import com.studybuddy.core.domain.model.Poem
import com.studybuddy.core.domain.model.PoemSource
import com.studybuddy.core.domain.model.ReadingSession
import com.studybuddy.core.domain.repository.PoemRepository
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private const val CACHE_DURATION_MS = 24 * 60 * 60 * 1000L // 24 hours

@Singleton
class LocalPoemRepository @Inject constructor(
    private val dao: PoemDao,
    private val apiClient: PoetryDbClient,
    private val bundledLoader: BundledPoemLoader,
) : PoemRepository {

    private val json = Json { ignoreUnknownKeys = true }

    override fun getPoemsByLanguage(language: String): Flow<List<Poem>> =
        dao.getCachedPoemsByLanguage(language).map { entities ->
            entities.map { it.toDomain() }
        }

    override fun getUserPoems(profileId: String): Flow<List<Poem>> = dao.getUserPoems(profileId).map { entities ->
        entities.map { it.toDomain() }
    }

    override fun getFavourites(profileId: String): Flow<List<Poem>> = combine(
        dao.getFavourites(profileId),
        dao.getCachedPoemsByLanguage("en"),
        dao.getCachedPoemsByLanguage("fr"),
        dao.getCachedPoemsByLanguage("de"),
    ) { favs, en, fr, de ->
        val allCached = (en + fr + de).associateBy { it.id }
        favs.mapNotNull { fav ->
            allCached[fav.poemId]?.toDomain(isFavourite = true)
        }
    }

    override fun isFavourite(
        poemId: String,
        profileId: String,
    ): Flow<Boolean> = dao.isFavourite(poemId, profileId)

    override fun getSessionsForPoem(
        poemId: String,
        profileId: String,
    ): Flow<List<ReadingSession>> = dao.getSessionsForPoem(poemId, profileId).map { entities ->
        entities.map { it.toDomain() }
    }

    override suspend fun getPoemById(id: String): Poem? = dao.getCachedPoemById(id)?.toDomain()
        ?: dao.getUserPoemById(id)?.toDomain()

    override suspend fun refreshPoems(language: String) {
        val now = System.currentTimeMillis()
        dao.deleteExpiredCache(now - CACHE_DURATION_MS)

        // Seed bundled poems if not yet cached
        val bundledCount = dao.getCachedBundledPoemCount(language)
        if (bundledCount == 0) {
            val bundled = when (language) {
                "fr" -> bundledLoader.loadFrenchPoems()
                "de" -> bundledLoader.loadGermanPoems()
                else -> emptyList()
            }
            if (bundled.isNotEmpty()) {
                val entities = bundled.map { poem ->
                    CachedPoemEntity(
                        id = UUID.randomUUID().toString(),
                        title = poem.title,
                        author = poem.author,
                        lines = json.encodeToString(poem.lines),
                        language = language,
                        source = PoemSource.BUNDLED.name,
                        tags = json.encodeToString(poem.tags),
                        cachedAt = now,
                    )
                }
                dao.insertCachedPoems(entities)
            }
        }

        // Fetch English poems from API if cache is stale
        if (language == "en") {
            val apiCount = dao.getCachedApiPoemCount(language, now - CACHE_DURATION_MS)
            if (apiCount == 0) {
                try {
                    val apiPoems = apiClient.getChildFriendlyPoems()
                    val entities = apiPoems.map { poem ->
                        CachedPoemEntity(
                            id = UUID.randomUUID().toString(),
                            title = poem.title,
                            author = poem.author,
                            lines = json.encodeToString(poem.lines),
                            language = "en",
                            source = PoemSource.API.name,
                            tags = json.encodeToString(emptyList<String>()),
                            cachedAt = now,
                        )
                    }
                    dao.insertCachedPoems(entities)
                } catch (_: Exception) {
                    // Offline — use whatever is cached
                }
            }
        }
    }

    override suspend fun toggleFavourite(
        poemId: String,
        poemSource: String,
        profileId: String,
    ) {
        val currentlyFav = dao.isFavourite(poemId, profileId).first()
        if (currentlyFav) {
            dao.removeFavourite(poemId, profileId)
        } else {
            dao.insertFavourite(
                FavouritePoemEntity(
                    id = UUID.randomUUID().toString(),
                    poemId = poemId,
                    poemSource = poemSource,
                    profileId = profileId,
                    favouritedAt = System.currentTimeMillis(),
                ),
            )
        }
    }

    override suspend fun createUserPoem(
        poem: Poem,
        profileId: String,
    ) {
        dao.insertUserPoem(poem.toUserEntity(profileId))
    }

    override suspend fun deleteUserPoem(id: String) {
        dao.deleteUserPoem(id)
    }

    override suspend fun saveReadingSession(session: ReadingSession) {
        dao.insertReadingSession(session.toEntity())
    }

    override suspend fun getBestSession(
        poemId: String,
        profileId: String,
    ): ReadingSession? = dao.getBestSession(poemId, profileId)?.toDomain()

    override suspend fun sync() {
        // Cloud migration hook — no-op for local-first
    }
}
