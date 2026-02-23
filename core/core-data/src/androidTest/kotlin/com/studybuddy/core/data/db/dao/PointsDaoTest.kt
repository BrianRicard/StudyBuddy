package com.studybuddy.core.data.db.dao

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.studybuddy.core.data.db.StudyBuddyDatabase
import com.studybuddy.core.data.db.entity.PointEventEntity
import com.studybuddy.core.data.db.entity.ProfileEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID

/**
 * Instrumented tests for [PointsDao].
 *
 * Regression test: verifies that [PointsDao.getSessionsToday] returns the
 * COUNT of DICTEE/MATH events, not the SUM of their points.
 */
@RunWith(AndroidJUnit4::class)
class PointsDaoTest {

    private lateinit var database: StudyBuddyDatabase
    private lateinit var pointsDao: PointsDao
    private lateinit var profileDao: ProfileDao

    private val profileId = "test-profile-id"
    private val startOfDayMs = 1_700_000_000_000L // arbitrary start of day

    @Before
    fun setup() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, StudyBuddyDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        pointsDao = database.pointsDao()
        profileDao = database.profileDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    private suspend fun insertProfile() {
        profileDao.insert(
            ProfileEntity(
                id = profileId,
                name = "Test User",
                locale = "en",
                totalPoints = 0,
                createdAt = startOfDayMs,
                updatedAt = startOfDayMs,
            ),
        )
    }

    private fun createPointEvent(
        source: String,
        points: Int,
        timestamp: Long = startOfDayMs + 1000,
    ) = PointEventEntity(
        id = UUID.randomUUID().toString(),
        profileId = profileId,
        source = source,
        points = points,
        reason = "test",
        timestamp = timestamp,
    )

    @Test
    fun getSessionsToday_returnsCountNotSumOfPoints() = runTest {
        insertProfile()

        // Insert 3 MATH events with varying point values
        pointsDao.insert(createPointEvent(source = "MATH", points = 50))
        pointsDao.insert(createPointEvent(source = "MATH", points = 75))
        pointsDao.insert(createPointEvent(source = "DICTEE", points = 130))

        val sessionsToday = pointsDao.getSessionsToday(
            profileId = profileId,
            startOfDayMs = startOfDayMs,
        ).first()

        // Should be 3 (count of events), NOT 255 (sum of points)
        assertEquals(
            "getSessionsToday must return COUNT of events, not SUM of points",
            3,
            sessionsToday,
        )
    }

    @Test
    fun getSessionsToday_excludesNonGameSources() = runTest {
        insertProfile()

        // Insert events with different sources
        pointsDao.insert(createPointEvent(source = "MATH", points = 50))
        pointsDao.insert(createPointEvent(source = "DICTEE", points = 100))
        pointsDao.insert(createPointEvent(source = "DAILY_LOGIN", points = 10))
        pointsDao.insert(createPointEvent(source = "FIRST_SESSION", points = 20))

        val sessionsToday = pointsDao.getSessionsToday(
            profileId = profileId,
            startOfDayMs = startOfDayMs,
        ).first()

        // Only MATH and DICTEE should be counted
        assertEquals(
            "getSessionsToday must only count DICTEE and MATH events",
            2,
            sessionsToday,
        )
    }

    @Test
    fun getSessionsToday_excludesEventsBeforeStartOfDay() = runTest {
        insertProfile()

        // Insert event before start of day
        pointsDao.insert(
            createPointEvent(
                source = "MATH",
                points = 50,
                timestamp = startOfDayMs - 1000,
            ),
        )
        // Insert event after start of day
        pointsDao.insert(
            createPointEvent(
                source = "MATH",
                points = 50,
                timestamp = startOfDayMs + 1000,
            ),
        )

        val sessionsToday = pointsDao.getSessionsToday(
            profileId = profileId,
            startOfDayMs = startOfDayMs,
        ).first()

        assertEquals(
            "getSessionsToday must only count events from today",
            1,
            sessionsToday,
        )
    }

    @Test
    fun getSessionsToday_returnsZeroWhenNoEvents() = runTest {
        insertProfile()

        val sessionsToday = pointsDao.getSessionsToday(
            profileId = profileId,
            startOfDayMs = startOfDayMs,
        ).first()

        assertEquals(0, sessionsToday)
    }
}
