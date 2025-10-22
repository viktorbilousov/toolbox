package org.vib.toolbox.reader.rc2

import io.kotest.assertions.assertSoftly
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import org.vib.toolbox.reader.DoubleBufferedReader
import org.vib.toolbox.reader.HistoryBufferedReader.MatchPosition
import org.vib.toolbox.reader.HistoryBufferedReader.ReadLimit
import org.vib.toolbox.reader.readChar
import java.io.IOException
import java.io.StringReader

@Timeout(1)
class DoubleBufferedReaderRC2Test {

    @Test
    fun `should read characters forward correctly`() {
        val text = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        val reader = DoubleBufferedReader(StringReader(text), bufferSize = 10)

        val result = buildString {
            var ch: Int
            while (reader.read().also { ch = it } != -1) {
                append(ch.toChar())
            }
        }

        assertEquals(text, result)
    }

    @Test
    fun `should read all characters forward correctly`() {
        val text = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        val reader = DoubleBufferedReader(StringReader(text), bufferSize = 10)
        val result = reader.readText()
        assertEquals(text, result)
    }

    @Test
    fun `read and read char work the same`() {
        val text = "1234|abcd"
        val reader1 = readerOf(text)
        val reader2 = readerOf(text)

        reader1.goForwardTo("|", matchPosition = MatchPosition.BEFORE)
        reader2.goForwardTo("|", matchPosition = MatchPosition.BEFORE)


        reader1.read().toChar() shouldBe '|'
        reader2.readChar() shouldBe '|'

        reader1.goBack()
        reader2.goBack()

        reader1.read().toChar() shouldBe '|'
        reader2.readChar() shouldBe '|'
    }

    @Test
    fun `should go back inside same buffer`() {
        val text = "ABCDE"
        val reader = DoubleBufferedReader(StringReader(text), bufferSize = 6)

        val first = reader.read().toChar() // 'A'
        val second = reader.read().toChar() // 'B'

        assertEquals('A', first)
        assertEquals('B', second)

        val back = reader.goBackAndGet()
        assertEquals('A', back)

        val again = reader.read().toChar()
        assertEquals('B', again)
    }

    @Test
    fun `should go back across buffer boundary`() {
        val text = "ABCDEFGHIJ" // 10 chars
        val reader = DoubleBufferedReader(StringReader(text), bufferSize = 6) // two halves = 3 chars each

        val readAll = buildString {
            repeat(6) { append(reader.read().toChar()) } // fills two buffers
        }
        assertEquals("ABCDEF", readAll)

        // Go back into previous buffer
        val back = reader.peekCurrent()!!
        assertEquals('F', back)
        val back2 = reader.goBackAndGet()!!
        assertEquals('E', back2)
        val back3 = reader.goBackAndGet()!!
        assertEquals('D', back3)
    }

    @Test
    fun `should return false when going back beyond available history`() {
        val text = "ABC"
        val reader = DoubleBufferedReader(StringReader(text), bufferSize = 6)

        reader.read()
        reader.read()
        reader.read()

        // Go back three times -> ok
        repeat(3) { assertNotEquals(null, reader.goBack()) }

        // One more -> should fail
        assertEquals(false, reader.goBack())
    }

    @Test
    fun `should handle empty input`() {
        val reader = DoubleBufferedReader(StringReader(""), bufferSize = 20)
        assertEquals(-1, reader.read())
        assertEquals(false, reader.goBack())
    }


    @Test
    fun `should go back to target and stop before match`() {
        val text = "abcdef123ghi"
        val reader = DoubleBufferedReader(StringReader(text), bufferSize = 16)

        // Move to the end
        while (reader.read() != -1);

        val found = reader.goBackTo("123", matchPosition = MatchPosition.BEFORE)
        assertTrue(found)

        // Next read should yield the first char of the match
        val next = reader.read().toChar()
        assertEquals('1', next)
    }

    @Test
    fun `should go back to target and position after match`() {
        val text = "abcdef123ghi"
        val reader = DoubleBufferedReader(StringReader(text), bufferSize = 16)

        while (reader.read() != -1);

        val found = reader.goBackTo("123", matchPosition = MatchPosition.AFTER)
        assertTrue(found)

        // We should be positioned after the match, so next char is 'g'
        val next = reader.read().toChar()
        assertEquals('g', next)
    }

    @Test
    fun `should not move when target not found and resetOnFail true`() {
        val text = "abcdef"
        val reader = DoubleBufferedReader(StringReader(text), bufferSize = 16)

        while (reader.read() != -1);

        val before = reader.goBack() // go back one char -> now at 'f'
        val found = reader.goBackTo("zzz", resetOnFail = true)
        assertFalse(found)

        // We should be back where we were (at 'f')
        val c = reader.read().toChar()
        assertEquals('f', c)
    }

    @Test
    fun `should stop at first matching target among many`() {
        val text = "abcxxxyyyzzz"
        val reader = DoubleBufferedReader(StringReader(text), bufferSize = 16)

        while (reader.read() != -1);

        val found = reader.goBackTo("yyy", "xxx", matchPosition = MatchPosition.BEFORE)
        assertTrue(found)

        // Next read should yield the first char of the first match found backward (yyy)
        val next = reader.read().toChar()
        assertEquals('y', next)
    }

    @Test
    fun `should return false when nothing matches`() {
        val text = "abcdef"
        val reader = DoubleBufferedReader(StringReader(text), bufferSize = 12)
        while (reader.read() != -1);
        val found = reader.goBackTo("xyz")
        assertFalse(found)
    }

    @Test
    fun `goBackTo should not cross the limit`() {
        val text = "9876|543210"
        val reader = DoubleBufferedReader(StringReader(text), bufferSize = 20)
        reader.readText()
        val found5 = reader.goBackTo("|", limit = 5)
        val found6 = reader.goBackTo("|", limit = 6)
        assertFalse(found5)
        assertTrue(found6)
    }

    @Test
    fun `should find with limit`() {
        val text = "9876|543210"
        val reader = DoubleBufferedReader(StringReader(text), bufferSize = 20)
        assertEquals(reader.readText(), "9876|543210")
        val found = reader.goBackTo("|", limit = 20)
        assertTrue(found)
    }

    @Test
    fun `should peek current`() {
        val text = "9876|543210"
        val reader = DoubleBufferedReader(StringReader(text), bufferSize = 20)
        while (reader.readChar() != '|') {
        }
        println(reader.peekPrevious())
        println(reader.peekCurrent())
        println(reader.peekNext())
        assertEquals(reader.peekCurrent(), '|')
    }


    @Test
    fun `should peek previous`() {
        val text = "9876|543210"
        val reader = DoubleBufferedReader(StringReader(text), bufferSize = 20)
        while (reader.readChar() != '|') {
        }
        assertEquals(reader.peekPrevious(), '6')
    }

    @Test
    fun `should peek next`() {
        val text = "9876|543210"
        val reader = DoubleBufferedReader(StringReader(text), bufferSize = 20)
        while (reader.readChar() != '|') {
        }
        assertEquals(reader.peekNext(), '5')
    }

    @Test
    fun `should peek next equals to read`() {
        val text = "9876|543210"
        val reader = DoubleBufferedReader(StringReader(text), bufferSize = 20)
        while (reader.readChar() != '|') {
        }
        assertEquals(reader.peekNext(), '5')
        assertEquals(reader.readChar(), '5')
    }

    @Test
    fun `should be able to peek current on any position`() {
        val text = "9876|543210"
        val reader = DoubleBufferedReader(StringReader(text), bufferSize = 10)
        val sb = StringBuilder()
        for ((index, ch) in text.withIndex()) {
            val ch = reader.readChar() ?: break
            sb.append(ch)
            reader.peekCurrent() shouldBe ch
        }
        sb.toString() shouldBe text
    }


    @Test
    fun `should be able to peek previous on any position except first`() {
        val text = "9876|543210"
        val reader = DoubleBufferedReader(StringReader(text), bufferSize = 10)
        val sb = StringBuilder()
        for ((index, ch) in text.withIndex()) {
            val ch = reader.readChar() ?: break
            val prev = reader.peekPrevious()
            if (index == 0) {
                prev shouldBe null
                sb.append("[null]")
            } else {
                sb.append(prev)
                prev shouldBe text[index - 1]
            }
        }
        sb.toString() shouldBe "[null]9876|54321"
    }

    @Test
    fun `should be able to peek next on any position except last`() {
        val text = "9876|543210"
        val reader = DoubleBufferedReader(StringReader(text), bufferSize = 20)
        val sb = StringBuilder()
        for ((index, ch) in text.withIndex()) {
            val ch = reader.readChar() ?: break
            val next = reader.peekNext()
            if (index == text.lastIndex) {
                next shouldBe null
                sb.append("[null]")
            } else {
                sb.append(next)
                next shouldBe text[index + 1]
            }
        }
        sb.toString() shouldBe "876|543210[null]"
    }


    @Test
    fun `hasCurrent should be false before read`() {
        val text = "9876|543210"
        val reader = DoubleBufferedReader(StringReader(text), bufferSize = 10)
        reader.hasCurrent() shouldBe false
        reader.read()
        reader.hasCurrent() shouldBe true
        reader.readText()
        reader.hasCurrent() shouldBe true
    }


    @Test
    fun `hasPrev should be false before second read`() {
        val text = "9876|543210"
        val reader = DoubleBufferedReader(StringReader(text), bufferSize = 10)
        reader.hasPrevious() shouldBe false

        reader.read()

        reader.hasPrevious() shouldBe false

        reader.read()

        reader.hasPrevious() shouldBe true

        reader.readText()
        reader.hasPrevious() shouldBe true
    }

    @Test
    fun `hasNext should be false at the end`() {
        val text = "9876|543210"
        val reader = DoubleBufferedReader(StringReader(text), bufferSize = 10)
        reader.hasNext() shouldBe true

        reader.read()

        reader.hasNext() shouldBe true

        reader.readText()
        reader.hasNext() shouldBe false
    }

    private fun readerOf(text: String, capacity: Int = 8192 * 2) = DoubleBufferedReader(text.reader(), capacity)

    @Test
    fun `should stop before target when matchPosition BEFORE`() {
        val reader = readerOf("abcXYZdef")

        val found = reader.goForwardTo("XYZ", matchPosition = MatchPosition.BEFORE)

        assertTrue(found)
        assertEquals('X', reader.read().toChar()) // first char
    }

    @Timeout(1)
    @Test
    fun `should stop after target when matchPosition AFTER`() {
        val reader = readerOf("abcXYZdef")

        val found = reader.goForwardTo("XYZ", matchPosition = MatchPosition.AFTER)

        assertTrue(found)
        // After match → next char should be 'd'
        assertEquals('d', reader.read().toChar())
    }

    @Test
    fun `should return false and reset position when resetOnFail true`() {
        val reader = readerOf("abcdef")

        val startChar = reader.readChar()
        val found = reader.goForwardTo("ZZZ", resetOnFail = true)

        assertFalse(found)
        // The position should be reset to original — peek should still show 'a'
        assertEquals(startChar, reader.peekCurrent())
    }

    @Test
    fun `should not reset position when resetOnFail false`() {
        val reader = readerOf("abcdef")

        val found = reader.goForwardTo("ZZZ", resetOnFail = false)

        assertFalse(found)
        // The reader should now be at EOF
        assertEquals(-1, reader.read())
    }

    @Test
    fun `should respect readLimit when searching`() {
        val reader = readerOf("abcdefghXYZ")

        val found = reader.goForwardTo("XYZ", readLimit = 5)

        assertFalse(found)
        // The reader should stop after 5 characters (f)
        assertEquals('f', reader.read().toChar())
    }

    @Test
    fun `should work with multiple targets`() {
        val reader = readerOf("abc123XYZ456")

        val found = reader.goForwardTo("XYZ", "123")

        assertTrue(found)
        // Should stop before the first target (123)
        assertEquals('c', reader.peekCurrent())
        assertEquals('1', reader.read().toChar())
    }

    @Test
    fun `should support both overloads`() {
        val reader = readerOf("abcXYZdef")

        val found = reader.goForwardTo("XYZ", matchPosition = MatchPosition.BEFORE, readLimit = ReadLimit.UNLIMITED)

        assertTrue(found)
        assertEquals('X', reader.read().toChar())
    }


    @Test
    fun `should calculate safe limit`() {
        val reader = readerOf("1234567890X", 10)
        val found = reader.goForwardTo("X", readLimit = ReadLimit.END_OF_BUFFER)
        assertFalse (found)
        assertEquals('X', reader.read().toChar())
    }



    @Test
    fun `should return to marked position after reset`() {
        val reader = readerOf("abcdef")

        // Read a few chars
        reader.read() // 'a'
        reader.read() // 'b'

        val mark = reader.markPosition()
        println(mark)
        val next = reader.read() // 'c'
        assertEquals('c'.code, next)

        reader.resetPosition(mark)
        val reread = reader.read()
        assertEquals('c'.code, reread, "Reset should restore the marked position")
    }

    @Test
    fun `should handle multiple marks and resets correctly`() {
        val reader = readerOf("abcdef")

        reader.read() // 'a'
        val mark1 = reader.markPosition()
        reader.read() // 'b'
        val mark2 = reader.markPosition()
        reader.read() // 'c'

        // Reset to mark2 — should read 'c' again
        reader.resetPosition(mark2)
        assertEquals('c'.code, reader.read())

        // Reset to mark1 — should read 'b' next
        reader.resetPosition(mark1)
        assertEquals('b'.code, reader.read())
    }

    @Test
    fun `mark and reset should work near end of stream`() {
        val reader = readerOf("xyz")
        while (reader.read() != -1) {} // move to EOF

        val mark = reader.markPosition()
        assertEquals(-1, reader.read()) // still EOF

        reader.resetPosition(mark)
        assertEquals(-1, reader.read(), "Reset after EOF should stay at EOF")
    }

    @Test
    fun `multiple resets to same mark should be idempotent`() {
        val reader = readerOf("hello")

        reader.read() // 'h'
        val mark = reader.markPosition()
        reader.read() // 'e'
        reader.resetPosition(mark)
        assertEquals('e'.code, reader.read())

        // Reset again to same mark — should still reread 'e'
        reader.resetPosition(mark)
        assertEquals('e'.code, reader.read())
    }

    @Test
    fun `should not affect other marks after reset`() {
        val reader = readerOf("abcdef")

        reader.read() // 'a'
        val m1 = reader.markPosition()
        reader.read() // 'b'
        val m2 = reader.markPosition()
        reader.read() // 'c'

        reader.resetPosition(m1)
        assertEquals('b'.code, reader.read())

        reader.resetPosition(m2)
        assertEquals('c'.code, reader.read())
    }

    @Test
    fun `markPosition should always increase while reading forward`() {
        val reader = readerOf("abcdef")
        var prev = reader.markPosition()
        repeat(5) {
            reader.read()
            val mark = reader.markPosition()
            assertTrue(mark >= prev, "Mark should not decrease as we read forward")
            prev = mark
        }
    }

    @Test
    fun `markPosition should throws exception if cannot reset`() {
        val reader = DoubleBufferedReader("1234567890".reader(), 4)
        val pos = reader.markPosition()
        repeat(5) {
            reader.read()
        }
        shouldThrow<IOException> { reader.resetPosition(pos) }
    }


    @Test
    fun `markPosition should not throws exception if reset from last char`() {
        val reader = DoubleBufferedReader("1234567890".reader(), 4)
        val pos = reader.markPosition()
        repeat(4) {
            reader.read()
        }
        reader.resetPosition(pos)
        reader.readChar() shouldBe '1'
    }

    @Test
    fun `hasNext don't move the current read pointer`() {
        val reader = readerOf("12345");
        reader.hasNext() shouldBe true
        reader.getFromFirstReadToCurrent() shouldBe ""
    }

    @Test
    fun `hasNext don't move the current read pointer 2`() {
        val reader = readerOf("1234|1234", 4*2);
        reader.readChar() shouldBe '1'
        reader.readChar() shouldBe '2'
        reader.readChar() shouldBe '3'
        reader.readChar() shouldBe '4'
        reader.getFromFirstReadToCurrent() shouldBe "1234"

        reader.hasNext() shouldBe true

        reader.getFromFirstReadToCurrent() shouldBe "1234"

        reader.readChar() shouldBe '|'
    }

    @Test
    fun `getFromFirstReadToCurrent`() {
        val text = "1234|1234|1234|1234"
        val reader = readerOf("1234|1234|1234|1234", 4*2);

        val expected = listOf(
            "",
            "1",
            "12",
            "123",
            "1234",
            "1234|",
            "1234|1",
            "1234|12",
            "1234|123",

            "|1234",
            "|1234|",
            "|1234|1",
            "|1234|12",

            "4|123",
            "4|1234",
            "4|1234|",
            "4|1234|1",

            "34|12",
            "34|123",
            "34|1234"
        );

        for ((index, ch) in text.withIndex()) {
            val t = reader.getFromFirstReadToCurrent()
            println(t)
            expected[index] shouldBe t
            reader.read()
        }
         reader.getFromFirstReadToCurrent() shouldBe expected.last()

    }


    @Test
    fun `should stop before first matching char when matchPosition BEFORE`() {
        val reader = readerOf("abcXyz")

        val found = reader.goForwardTo('X', matchPosition = MatchPosition.BEFORE)
        assertTrue(found, "Expected to find target 'X'")

        val next = reader.read().toChar()
        assertEquals('X', next, "Reader should now be positioned before the target character")
    }

    @Test
    fun `should stop after matching char when matchPosition AFTER`() {
        val reader = readerOf("abcXyz")

        val found = reader.goForwardTo('X', matchPosition = MatchPosition.AFTER)
        assertTrue(found, "Expected to find target 'X'")

        val next = reader.read().toChar()
        assertEquals('y', next, "Reader should now point after the matched character")
    }

    @Test
    fun `should find any of multiple target characters`() {
        val reader = readerOf("abcdef")

        val found = reader.goForwardTo('x', 'e', 'z')
        assertTrue(found, "Expected to find 'e' among targets")

        val next = reader.read().toChar()
        assertEquals('e', next, "Reader should point at found target when BEFORE")
    }

    @Test
    fun `should return false when no target found`() {
        val reader = readerOf("abcdef")

        val found = reader.goForwardTo('x', 'y')
        assertFalse(found, "Expected to not find any target characters")
    }

    @Test
    fun `should reset to initial position on fail when resetOnFail true`() {
        val reader = readerOf("abcdef")

        val mark = reader.markPosition()
        val found = reader.goForwardTo('x', resetOnFail = true)
        assertFalse(found)

        val after = reader.markPosition()
        assertEquals(mark, after, "Reader should be reset to starting position")
    }

    @Test
    fun `should not reset position on fail when resetOnFail false`() {
        val reader = readerOf("abcdef")

        val start = reader.markPosition()
        val found = reader.goForwardTo('x', resetOnFail = false)
        assertFalse(found)

        val after = reader.markPosition()
        assertTrue(after > start, "Reader should have advanced when not resetting on fail")
    }

    @Test
    fun `goForwardTo should respect readLimit and stop early`() {
        val reader = readerOf("abcdefghX")

        val start = reader.markPosition()
        val found = reader.goForwardTo('X', readLimit = ReadLimit(5))
        assertFalse(found, "Target is outside read limit")

        val after = reader.markPosition()
        assertTrue(after - start <= 5, "Reader should not advance more than readLimit")
    }

    @Test
    fun `should find character at end of stream`() {
        val reader = readerOf("abcdefX")

        val found = reader.goForwardTo('X')
        assertTrue(found)
        val ch = reader.read().toChar()
        assertEquals('X', ch)
    }

    @Test
    fun `should return false for empty target list`() {
        val reader = readerOf("abcdef")
        val found = reader.goForwardTo(targets = emptyArray<String>())
        assertFalse(found)
    }

    @Test
    fun `should work correctly when multiple matches appear`() {
        val reader = readerOf("abXcXd")

        val found = reader.goForwardTo('X')
        assertTrue(found)
        val ch = reader.read().toChar()
        assertEquals('X', ch)

        // Continue to next match
        val nextFound = reader.goForwardTo('X')
        assertTrue(nextFound)
        val nextCh = reader.read().toChar()
        assertEquals('X', nextCh)
    }

    @Test
    fun `given reader after target when going back BEFORE should stop before target`() {
        val reader = readerOf("abcXyz")
        repeat(5) { reader.read() } // now at 'y'

        val found = reader.goBackTo('X', matchPosition = MatchPosition.BEFORE)
        assertTrue(found)

        val ch = reader.read().toChar()
        assertEquals('X', ch)
    }

    @Test
    fun `given reader after target when going back AFTER should stop after target`() {
        val reader = readerOf("abcXyz")
        repeat(5) { reader.read() } // at 'y'

        val found = reader.goBackTo('X', matchPosition = MatchPosition.AFTER)
        assertTrue(found)

        val ch = reader.read().toChar()
        assertEquals('y', ch)
    }

    @Test
    fun `given multiple target chars should stop at first matching char`() {
        val reader = readerOf("abcdef")
        reader.readText() // at 'e'

        val found = reader.goBackTo('a', 'c', 'd')
        assertTrue(found)

        val ch = reader.read().toChar()
        assertEquals('d', ch)
    }

    @Test
    fun `should return false when no targets are found`() {
        val reader = readerOf("abcdef")
        repeat(4) { reader.read() }

        val found = reader.goBackTo('x', 'y')
        assertFalse(found)
    }

    @Test
    fun `when resetOnFail is true reader should restore position on fail`() {
        val reader = readerOf("abcdef")
        repeat(4) { reader.read() }
        val mark = reader.markPosition()

        val found = reader.goBackTo('x', resetOnFail = true)
        assertFalse(found)

        val ch = reader.read().toChar()
        assertEquals('e', ch)
    }

    @Test
    fun `when resetOnFail is false reader should stay advanced on fail`() {
        val reader = readerOf("abcdef")
        repeat(4) { reader.read() }
        val mark = reader.markPosition()

        val found = reader.goBackTo('x', resetOnFail = false)
        assertFalse(found)

        val after = reader.markPosition()
        assertTrue(after < mark)
    }

    @Test
    fun `should not find target if limit is exceeded`() {
        val reader = readerOf("abcdefX")
        repeat(6) { reader.read() } // move to end

        val found = reader.goBackTo('a', limit = 3)
        assertFalse(found)
    }

    @Test
    fun `should find target character at start of stream`() {
        val reader = readerOf("abcdef")
        repeat(5) { reader.read() } // at 'f'

        val found = reader.goBackTo('a', matchPosition = MatchPosition.BEFORE)
        assertTrue(found)

        val ch = reader.read().toChar()
        assertEquals('a', ch)
    }

    @Test
    fun `back to should return false for empty target list`() {
        val reader = readerOf("abcdef")
        repeat(3) { reader.read() }

        val found = reader.goBackTo(targets = arrayOf())
        assertFalse(found)
    }

    @Test
    fun `should find multiple matches sequentially when scanning backward`() {
        val reader = readerOf("abXcXd")
        reader.readText()

        val found1 = reader.goBackTo('X')
        assertTrue(found1)
        assertEquals('X', reader.read().toChar())

        val found2 = reader.goBackTo('X')
        assertTrue(found2)
        assertEquals('X', reader.read().toChar())
    }


    @Test
    fun fromCurrentToEnd(){
        val text = "1234567890"
        val reader = readerOf(text)
        reader.readText()
        for ((index, ch) in text.reversed().withIndex()) {
            val curr = reader.readFromCurrentToEnd()
            println(curr)
            reader.goBack()
            if(index == 0) curr shouldBe ""
            curr shouldBe text.takeLast(index)
        }
        val curr = reader.readFromCurrentToEnd()
        curr shouldBe text
        println(curr)
    }

    @Test
    fun `fromCurrentToEnd 2 buffers`(){
        val text = "1234567890"
        val reader = readerOf(text, 10)
        reader.readText()
        for ((index, ch) in text.reversed().withIndex()) {
            val curr = reader.readFromCurrentToEnd()
            println(curr)
            reader.goBack()
            if(index == 0) curr shouldBe ""
            curr shouldBe text.takeLast(index)
        }
        val curr = reader.readFromCurrentToEnd()
        println(curr)
        curr shouldBe text
    }



    @Test
    fun `should return text up to but not including target when matchPosition BEFORE`() {
        val reader = readerOf("abcXdef")

        val result = reader.readTo('X', matchPosition = MatchPosition.BEFORE, resetOnFail = false, readLimit = 0)

        assertEquals("abc", result)
        // Reader now positioned before 'X'
        val next = reader.read().toChar()
        assertEquals('X', next)
    }

    @Test
    fun `should return text including target when matchPosition AFTER`() {
        val reader = readerOf("abcXdef")

        val result = reader.readTo('X', matchPosition = MatchPosition.AFTER, resetOnFail = false, readLimit = 0)

        assertEquals("abcX", result)
        // Reader now after 'X'
        val next = reader.read().toChar()
        assertEquals('d', next)
    }

    @Test
    fun `should return text up to first of multiple targets`() {
        val reader = readerOf("abcYXd")

        val result = reader.readTo('X', 'Y', matchPosition = MatchPosition.BEFORE, resetOnFail = false, readLimit = 0)

        assertEquals("abc", result)
        val next = reader.read().toChar()
        assertEquals('Y', next)
    }

    @Test
    fun `should return null and reset position when no target found and resetOnFail true`() {
        val reader = readerOf("abcdef")
        val mark = reader.markPosition()

        val result = reader.readTo('X', matchPosition = MatchPosition.BEFORE, resetOnFail = true, nullIfNotFound = true, readLimit = 0)

        assertNull(result)

        // Position should be restored
        val after = reader.markPosition()
        assertEquals(mark, after)
    }

    @Test
    fun `should not reset position when no match found and resetOnFail false`() {
        val reader = readerOf("abcdef")
        val mark = reader.markPosition()

        val result = reader.readTo('X', matchPosition = MatchPosition.BEFORE, resetOnFail = false, nullIfNotFound = true, readLimit = 0)

        assertNull(result)
        val after = reader.markPosition()
        assertTrue(after > mark)
    }

    @Test
    fun `readTo should respect readLimit and stop early`() {
        val reader = readerOf("abcdefX")

        val result = reader.readTo('X', matchPosition = MatchPosition.BEFORE, resetOnFail = false, nullIfNotFound = true, readLimit = 3)

        assertNull(result)
        val posAfter = reader.markPosition()
        assertTrue(posAfter < "abcdefX".length)
    }

    @Test
    fun `should return text until last char if target found at end`() {
        val reader = readerOf("abcdeX")

        val result = reader.readTo('X', matchPosition = MatchPosition.AFTER, resetOnFail = false, readLimit = 0)

        assertEquals("abcdeX", result)
    }

    @Test
    fun `should handle multiple consecutive reads correctly`() {
        val reader = readerOf("abcXdefYgh")

        val part1 = reader.readTo('X', matchPosition = MatchPosition.AFTER, resetOnFail = false, readLimit = 0)
        assertEquals("abcX", part1)

        val part2 = reader.readTo('Y', matchPosition = MatchPosition.BEFORE, resetOnFail = false, readLimit = 0)
        assertEquals("def", part2)

        val next = reader.read().toChar()
        assertEquals('Y', next)
    }

    @Test
    fun `should return empty string if target found immediately`() {
        val reader = readerOf("Xabc")

        val result = reader.readTo('X', matchPosition = MatchPosition.BEFORE, resetOnFail = false, readLimit = 0)

        assertEquals("", result)
        val next = reader.read().toChar()
        assertEquals('X', next)
    }

    @Test
    fun `should return null for empty targets`() {
        val reader = readerOf("abcdef")
        val result = reader.readTo(targets = CharArray(0), matchPosition = MatchPosition.BEFORE, resetOnFail = false, readLimit = 0)
        assertNull(result)
    }

    @Test
    fun `readTo char test`() {
        val text = "1234567890"
        for ((index, ch) in text.withIndex()) {
            val reader = readerOf(text, 4)
            val t = reader.readTo(text[index])
            println(text[index] + " : "+ t)
            t shouldBe text.take(index)
        }
    }

    @Test
    fun `readTo char from center test`() {
        val text = "aaaaaaaaa|1234567890"
        val textToSearch = "1234567890"
        for ((index, ch) in textToSearch.withIndex()) {
            val reader = readerOf(text, 4)
            reader.goForwardTo('|', matchPosition = MatchPosition.AFTER)
            val t = reader.readTo(textToSearch[index])
            println(textToSearch[index] + " : "+ t)
            t shouldBe textToSearch.take(index)
        }
    }

    @Test
    fun `readToStr from center test`() {
        val text = "aaaaaaaaa|1234567890"
        val textToSearch = "1234567890"
        for ((index, ch) in textToSearch.withIndex()) {
            if(index == textToSearch.lastIndex) break

            val reader = readerOf(text, 4)
            reader.goForwardTo('|', matchPosition = MatchPosition.AFTER)
            val str = "${textToSearch[index]}" + textToSearch[index+1]
            val t = reader.readTo(str, matchPosition = MatchPosition.BEFORE)
            println(str + " : "+ `t`)
            t shouldBe textToSearch.take(index)
        }
    }



    @Test
    fun `readTo multi line test`() {
        val text = "123456789\nAabcdacbd\nA987654321"
        val reader = readerOf(text)

        val expectedResult1 = text.split("\nA")[0]
        val expectedResult2 = text.split("\nA")[1]
        val expectedResult3 = text.split("\nA")[2]

        val result1 = reader.readTo("\nA", matchPosition = MatchPosition.BEFORE)
        println("Result 1: '$result1'")
        reader.goForward(2)

        val result2 = reader.readTo("\nA", matchPosition = MatchPosition.BEFORE)
        println("Result 2: '$result2'")
        reader.goForward(2)

        val result3 = reader.readTo("\nA", matchPosition = MatchPosition.BEFORE)
        println("Result 3: '$result3'")
        assertSoftly{
            result1 shouldBe expectedResult1
            result2 shouldBe expectedResult2
            result3 shouldBe expectedResult3
            }
    }


    @Test
    fun `readToStr to new lines if line is longer that buffer size`() {
        val text = "11111111\nA222222222\nA333333333"
        val reader = readerOf(text)
        val expected1 = text.split("\nA")[0]
        val expected2 = text.split("\nA")[1]
        val expected3 = text.split("\nA")[2]


        reader.readTo("\nA") shouldBe expected1
        reader.goForward(2)
        reader.readTo("\nA") shouldBe expected2
        reader.goForward(2)
        reader.readTo("\nA") shouldBe expected3
    }

    @Test
    fun `readToStr read to end`() {
        val text = "12345678"
        val reader = readerOf(text)
        val expected1 = text.split("\n")[0]
        reader.readTo("\n") shouldBe expected1
    }


    @Test
    fun `readToStr to new lines if line is shorted then buffer size`() {
        val text = "11111111\n222222222\n333333333"
        val reader = readerOf(text)
        val expected1 = text.split("\n")[0]
        val expected2 = text.split("\n")[1]
        val expected3 = text.split("\n")[2]


        reader.readTo("\n") shouldBe expected1
        reader.goForward()
        reader.readTo("\n") shouldBe expected2
        reader.goForward()
        reader.readTo("\n") shouldBe expected3
    }





    @Test
    fun `go forward to first`() {
        val text = "1234567890"
        val reader = readerOf(text)
        reader.goForwardTo('1', matchPosition = MatchPosition.BEFORE).shouldBe(true)
        reader.readChar().shouldBe('1')
    }

    @Test
    fun `go forward to second`() {
        val text = "1234567890"
        val reader = readerOf(text)
        reader.goForwardTo('2', matchPosition = MatchPosition.BEFORE).shouldBe(true)
        reader.readChar().shouldBe('2')
    }

    @Test
    fun `go forward to string between buffers`() {
        val text = "aaaaaaaaa|1234567890"
        val textToSearch = "1234567890"
        for ((index, ch) in textToSearch.withIndex()) {
            val reader = readerOf(text, 4)
            reader.goForwardTo("|1") shouldBe true
            while (reader.goBack()){}
            val res = reader.goForwardTo("|1")
            res shouldBe true
        }
    }

    @Test
    fun `readTo simple single char target BEFORE`() {
        val text = "abcdefg"
        val reader = readerOf(text, 4)
        val result = reader.readTo("d", matchPosition = MatchPosition.BEFORE)
        result shouldBe "abc"
    }

    @Test
    fun `readTo char simple single char target AFTER`() {
        val text = "abcdefg"
        val reader = readerOf(text, 4)
        val result = reader.readTo("d", matchPosition = MatchPosition.AFTER)
        result shouldBe "abcd"
    }

    @Test
    fun `readTo string target BEFORE`() {
        val text = "hello world"
        val reader = readerOf(text, 4)
        val result = reader.readTo("lo", matchPosition = MatchPosition.BEFORE)
        result shouldBe "hel"
    }

    @Test
    fun `readTo string target AFTER`() {
        val text = "hello world"
        val reader = readerOf(text, 4)
        val result = reader.readTo("lo", matchPosition = MatchPosition.AFTER)
        result shouldBe "hello"
    }

    @Test
    fun `readTo multiple targets`() {
        val text = "abcdefg"
        val reader = readerOf(text, 4)
        val result = reader.readTo("d", "f", matchPosition = MatchPosition.BEFORE)
        result shouldBe "abc" // stops at first match 'd'
    }

    @Test
    fun `readTo multiple string targets`() {
        val text = "hello world"
        val reader = readerOf(text, 4)
        val result = reader.readTo("lo", "wor", matchPosition = MatchPosition.BEFORE)
        result shouldBe "hel" // stops at first match "lo"
    }

    @Test
    fun `readTo case1`() {
        val text = "hehello world"
        val reader = readerOf(text)
        val result = reader.readTo("hello", matchPosition = MatchPosition.BEFORE)
        result shouldBe "he" // stops at first match "lo"
    }


    @Test
    fun `readTo target not found with resetOnFail`() {
        val text = "abcdefg"
        val reader = readerOf(text, 8)
        val mark = reader.markPosition()
        val result = reader.readTo("x", matchPosition = MatchPosition.BEFORE, resetOnFail = true, nullIfNotFound = true)
        result shouldBe null
        reader.markPosition() shouldBe mark // position restored
    }

    @Test
    fun `readTo char target not found without reset`() {
        val text = "abcdefg"
        val reader = readerOf(text, 8)
        val result = reader.readTo('x', matchPosition = MatchPosition.BEFORE, resetOnFail = false, nullIfNotFound = false)
        result shouldBe "abcdefg" // returns all read chars
    }

    @Test
    fun `readTo target not found without reset`() {
        val text = "abcdefg"
        val reader = readerOf(text, 8)
        val result = reader.readTo("xx", matchPosition = MatchPosition.BEFORE, resetOnFail = false, nullIfNotFound = false)
        result shouldBe "abcdefg" // returns all read chars
    }

    @Test
    fun `readTo char with readLimit stops early`() {
        val text = "abcdefg"
        val reader = readerOf(text, 4)
        val result = reader.readTo('f', matchPosition = MatchPosition.BEFORE, readLimit = 3)
        result shouldBe "abc" // reads only 3 characters
    }
    @Test
    fun `readTo string with readLimit stops early`() {
        val text = "abcdefg"
        val reader = readerOf(text, 4)
        val result = reader.readTo("fg", matchPosition = MatchPosition.BEFORE, readLimit = 3)
        result shouldBe "abc" // reads only 3 characters
    }


    @Test
    fun `readTo complex multi-line scenario`() {
        val text = "1111\n2222\n3333\n4444"
        val reader = readerOf(text)

        assertSoftly {
            // read up to "\n2222" BEFORE → returns first line
            val t1 =   reader.readTo("\n2222", matchPosition = MatchPosition.BEFORE)

            // read up to "\n3333" AFTER → includes matched "\n2222"
            val t2 =   reader.readTo("\n3333", matchPosition = MatchPosition.AFTER)
            // read up to "\n4444" BEFORE with limit → stops early
            val t3 =   reader.readTo("\n4444", matchPosition = MatchPosition.BEFORE, readLimit = 2)

            println("t1=`$t1`")
            println("t2=`$t2`")
            println("t3=`$t3`")


            assertSoftly {
                t1 shouldBe "1111"
                t2 shouldBe  "\n2222\n3333"
                t3 shouldBe  "\n4"
            }
        }
    }


    @Test
    fun `readTo char complex scenario`() {
        val text = "1111\n2222\n3333\n4444"
        val reader = readerOf(text)

        assertSoftly {
            // read up to "\n2222" BEFORE → returns first line

             reader.readTo('\n', matchPosition = MatchPosition.BEFORE) shouldBe "1111"
             reader.readTo('\n', matchPosition = MatchPosition.BEFORE) shouldBe ""
             reader.readTo('\n', matchPosition = MatchPosition.BEFORE) shouldBe ""
             reader.goForward() shouldBe true

            reader.readTo('\n', matchPosition = MatchPosition.BEFORE) shouldBe "2222"
            reader.readTo('\n', matchPosition = MatchPosition.BEFORE) shouldBe ""
            reader.readTo('\n', matchPosition = MatchPosition.BEFORE) shouldBe ""
            reader.goForward() shouldBe true

            reader.readTo('\n', matchPosition = MatchPosition.BEFORE) shouldBe "3333"
            reader.readTo('\n', matchPosition = MatchPosition.BEFORE) shouldBe ""
            reader.readTo('\n', matchPosition = MatchPosition.BEFORE) shouldBe ""
            reader.goForward() shouldBe true

            reader.readTo('\n', matchPosition = MatchPosition.BEFORE) shouldBe "4444"
            reader.readTo('\n', matchPosition = MatchPosition.BEFORE) shouldBe ""
            reader.readTo('\n', matchPosition = MatchPosition.BEFORE) shouldBe ""
            reader.goForward() shouldBe false

        }
    }





}
