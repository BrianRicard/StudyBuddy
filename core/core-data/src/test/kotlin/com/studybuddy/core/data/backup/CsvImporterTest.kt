package com.studybuddy.core.data.backup

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class CsvImporterTest {

    // region parseCsvLine

    @Nested
    inner class ParseCsvLine {

        @Test
        fun `splits simple fields`() {
            val result = parseCsvLine("Les Animaux,fr,chat,false,3,1")
            assertEquals(listOf("Les Animaux", "fr", "chat", "false", "3", "1"), result)
        }

        @Test
        fun `handles quoted fields with commas`() {
            val result = parseCsvLine("\"Words, Advanced\",en,hello,true,5,5")
            assertEquals(listOf("Words, Advanced", "en", "hello", "true", "5", "5"), result)
        }

        @Test
        fun `handles quoted fields with escaped quotes`() {
            val result = parseCsvLine("\"She said \"\"hello\"\"\",en,test,false,0,0")
            assertEquals(listOf("She said \"hello\"", "en", "test", "false", "0", "0"), result)
        }

        @Test
        fun `handles empty fields`() {
            val result = parseCsvLine(",,word,,0,")
            assertEquals(listOf("", "", "word", "", "0", ""), result)
        }

        @Test
        fun `handles minimal 3-column line`() {
            val result = parseCsvLine("Animals,en,cat")
            assertEquals(listOf("Animals", "en", "cat"), result)
        }

        @ParameterizedTest
        @CsvSource(
            "maison, maison",
            "école, école",
            "'hello world', 'hello world'",
        )
        fun `preserves accents and spaces`(
            input: String,
            expected: String,
        ) {
            val result = parseCsvLine("List,fr,$input")
            assertEquals(expected.trim(), result[2].trim())
        }

        @Test
        fun `handles single field`() {
            val result = parseCsvLine("hello")
            assertEquals(listOf("hello"), result)
        }

        @Test
        fun `handles empty string`() {
            val result = parseCsvLine("")
            assertEquals(listOf(""), result)
        }

        @Test
        fun `handles quoted field at end of line`() {
            val result = parseCsvLine("a,b,\"quoted end\"")
            assertEquals(listOf("a", "b", "quoted end"), result)
        }

        @Test
        fun `handles fully quoted line`() {
            val result = parseCsvLine("\"a\",\"b\",\"c\"")
            assertEquals(listOf("a", "b", "c"), result)
        }

        @Test
        fun `handles newline inside quoted field`() {
            val result = parseCsvLine("\"line1\nline2\",en,word")
            assertEquals(listOf("line1\nline2", "en", "word"), result)
        }
    }

    // endregion

    // region CSV content validation helpers

    @Nested
    inner class CsvContentValidation {

        @Test
        fun `full 6-column row parses all fields correctly`() {
            val row = parseCsvLine("Fruits,fr,pomme,true,10,8")
            assertEquals("Fruits", row[0])
            assertEquals("fr", row[1])
            assertEquals("pomme", row[2])
            assertEquals("true", row[3])
            assertEquals("10", row[4])
            assertEquals("8", row[5])
        }

        @Test
        fun `3-column row omits optional statistics`() {
            val row = parseCsvLine("Animals,en,dog")
            assertEquals(3, row.size)
            assertEquals("Animals", row[0])
            assertEquals("en", row[1])
            assertEquals("dog", row[2])
        }

        @Test
        fun `row with fewer than 3 columns is too short`() {
            val row = parseCsvLine("only,two")
            assert(row.size < 3) { "Expected fewer than 3 fields" }
        }

        @Test
        fun `boolean field parses correctly`() {
            val trueRow = parseCsvLine("List,en,word,true,1,1")
            assertEquals("true", trueRow[3])
            assertEquals(true, trueRow[3].toBooleanStrictOrNull())

            val falseRow = parseCsvLine("List,en,word,false,0,0")
            assertEquals("false", falseRow[3])
            assertEquals(false, falseRow[3].toBooleanStrictOrNull())
        }

        @Test
        fun `invalid boolean defaults gracefully`() {
            val row = parseCsvLine("List,en,word,maybe,1,1")
            assertEquals(null, row[3].toBooleanStrictOrNull())
        }

        @Test
        fun `integer fields parse correctly`() {
            val row = parseCsvLine("List,en,word,false,42,37")
            assertEquals(42, row[4].toIntOrNull())
            assertEquals(37, row[5].toIntOrNull())
        }

        @Test
        fun `non-numeric attempts field returns null`() {
            val row = parseCsvLine("List,en,word,false,abc,1")
            assertEquals(null, row[4].toIntOrNull())
        }

        @Test
        fun `multiple rows group by list and language`() {
            val csv = """
                List,Language,Word
                Animals,en,cat
                Animals,en,dog
                Animaux,fr,chat
                Animals,en,bird
            """.trimIndent()

            val rows = csv.lines().drop(1).map { parseCsvLine(it) }
            val grouped = rows.groupBy { Pair(it[0], it[1]) }

            assertEquals(2, grouped.size)
            assertEquals(3, grouped[Pair("Animals", "en")]?.size)
            assertEquals(1, grouped[Pair("Animaux", "fr")]?.size)
        }

        @Test
        fun `blank lines are filtered out`() {
            val csv = """
                List,Language,Word
                Animals,en,cat

                Animals,en,dog
            """.trimIndent()

            val lines = csv.lines().filter { it.isNotBlank() }
            assertEquals(3, lines.size)
        }

        @Test
        fun `header-only CSV has no data rows`() {
            val csv = "List,Language,Word"
            val lines = csv.lines().filter { it.isNotBlank() }
            val dataRows = lines.drop(1)
            assertEquals(0, dataRows.size)
        }
    }

    // endregion
}
