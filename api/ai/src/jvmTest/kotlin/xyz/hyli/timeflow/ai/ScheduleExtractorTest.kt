/*
 * Copyright (c) 2026 Lyxot and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证。
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/Lyxot/TimeFlow/blob/master/LICENSE
 */

package xyz.hyli.timeflow.ai

import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import xyz.hyli.timeflow.api.models.ExtractedCourse
import java.io.File
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.test.*

class ScheduleExtractorTest {

    @Test
    fun testParseJsonLines() {
        val extractor = ScheduleExtractor(
            provider = "openai",
            apiKey = "dummy",
            model = "gpt-4.1-mini"
        )

        val jsonl = """
            {"_schedule":true,"name":"测试课程表","totalWeeks":16,"displayWeekends":false,"morningLessons":4,"afternoonLessons":4}
            {"name":"数学","teacher":"张三","classroom":"A101","time":[1,2],"weekday":0,"week":[1,2,3,4,5]}
            {"name":"英语","teacher":"李四","classroom":"B202","time":[3,4],"weekday":1,"week":[1,2,3]}
            some garbage line
            {"name":"物理","teacher":null,"classroom":null,"time":[5,6],"weekday":2,"week":[1,3,5,7]}
        """.trimIndent()

        val result = extractor.parseResult(jsonl)

        // Schedule info
        val info = result.scheduleInfo
        assertNotNull(info, "Expected schedule info")
        assertEquals("测试课程表", info.name)
        assertEquals(16, info.totalWeeks)
        assertEquals(false, info.displayWeekends)
        assertEquals(4, info.morningLessons)
        assertEquals(4, info.afternoonLessons)
        assertNull(info.eveningLessons)

        // Courses
        val courses = result.courses
        assertEquals(3, courses.size)
        assertEquals("数学", courses[0].name)
        assertEquals(0, courses[0].weekday)
        assertEquals(listOf(1, 2), courses[0].time)
        assertEquals("英语", courses[1].name)
        assertNull(courses[2].teacher)

        // toSchedule
        val schedule = result.toSchedule()
        assertEquals("测试课程表", schedule.name)
        assertEquals(3, schedule.courses.size)
        assertEquals(false, schedule.displayWeekends)
        assertEquals(4, schedule.lessonTimePeriodInfo.morning.size)
        assertEquals(4, schedule.lessonTimePeriodInfo.afternoon.size)

        extractor.close()
    }

    @Test
    fun testToCourse() {
        val extracted = ExtractedCourse(
            name = "高等数学",
            teacher = "王教授",
            classroom = "A-301",
            time = listOf(1, 3),
            weekday = 0,
            week = listOf(1, 2, 3, 4, 5, 6, 7, 8)
        )

        val course = extracted.toCourse(color = 0xFF0000)
        assertTrue(course.name == "高等数学")
        assertTrue(course.teacher == "王教授")
        assertTrue(course.classroom == "A-301")
        assertTrue(course.time.start == 1)
        assertTrue(course.time.end == 3)
        assertTrue(course.weekday == xyz.hyli.timeflow.data.Weekday.MONDAY)
        assertTrue(course.week.weeks == listOf(1, 2, 3, 4, 5, 6, 7, 8))
        assertTrue(course.color == 0xFF0000)
    }

    /**
     * Integration test: calls OpenRouter with a real image.
     * Only runs if TEST_IMAGE_PATH env var is set.
     * Usage: TEST_IMAGE_PATH=/path/to/schedule.png ./gradlew :api:ai:jvmTest --tests "*testStreamingExtraction*"
     */
    @OptIn(ExperimentalEncodingApi::class)
    @Test
    fun testStreamingExtraction() {
        val imagePath = System.getenv("TEST_IMAGE_PATH") ?: run {
            println("Skipping integration test: TEST_IMAGE_PATH not set")
            return
        }
        val apiKey = System.getenv("TEST_API_KEY") ?: run {
            println("Skipping integration test: TEST_API_KEY not set")
            return
        }
        val provider = System.getenv("TEST_PROVIDER") ?: "openrouter"
        val model = System.getenv("TEST_MODEL") ?: "google/gemini-2.0-flash-exp:free"
        val endpoint = System.getenv("TEST_ENDPOINT")

        val imageBytes = File(imagePath).readBytes()
        val imageBase64 = Base64.encode(imageBytes)

        val extractor = ScheduleExtractor(
            provider = provider,
            apiKey = apiKey,
            model = model,
            endpoint = endpoint
        )

        runBlocking {
            // Try non-streaming first
            println("--- Non-streaming extraction ---")
            val courses = extractor.extractFull(imageBase64).courses
            println("Extracted ${courses.size} courses:")
            courses.forEach { course ->
                println("  ${course.name} | ${course.teacher} | ${course.classroom} | time=${course.time} | weekday=${course.weekday} | weeks=${course.week}")
            }
            assertTrue(courses.isNotEmpty(), "Expected at least one course to be extracted")

            // Then try streaming
            println("\n--- Streaming extraction ---")
            val chunks = mutableListOf<String>()
            extractor.extractStreaming(imageBase64).toList().also { deltas ->
                deltas.forEach { delta ->
                    print(delta)
                    chunks.add(delta)
                }
            }
            println("\n--- End streaming ---")

            val fullText = chunks.joinToString("")
            val streamedCourses = extractor.parseResult(fullText).courses
            println("Streamed ${streamedCourses.size} courses")
            assertTrue(streamedCourses.isNotEmpty(), "Expected at least one course from streaming")
        }

        extractor.close()
    }
}
