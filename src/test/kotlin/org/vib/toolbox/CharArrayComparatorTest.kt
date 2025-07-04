package org.vib.toolbox

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.assertThrows
import org.vib.toolbox.CharArrayComparator.containsPartOf
import kotlin.test.Test

class CharArrayComparatorTest {
    @Test
    fun `returns full match when fully present`() {
        containsPartOf("abcde".toCharArray(), "cd".toCharArray()) shouldBe 2
    }

    @Test
    fun `returns partial match when at the end of where`() {
        containsPartOf("1234".toCharArray(), "3456".toCharArray()) shouldBe 2
    }

    @Test
    fun `returns -1 when match breaks in the middle`() {
        containsPartOf("12341".toCharArray(), "3456".toCharArray()) shouldBe -1
    }

    @Test
    fun `returns -1 when there is no matching sequence`() {
        containsPartOf("xyz".toCharArray(), "ab".toCharArray()) shouldBe -1
    }

    @Test
    fun `matches partial encoding string at the end`() {
        containsPartOf(
            "<?xml version='1.0' encoding=".toCharArray(),
            "encoding='UTF-8'?>".toCharArray()
        ) shouldBe "encoding=".length
    }

    @Test
    fun `matches entire string when fully equal`() {
        containsPartOf(
            "encoding='UTF-8'?>".toCharArray(),
            "encoding='UTF-8'?>".toCharArray()
        ) shouldBe "encoding='UTF-8'?>".length
    }

    @Test
    fun `returns -1 for completely different arrays`() {
        containsPartOf(
            "123456789".toCharArray(),
            "abcdef".toCharArray()
        ) shouldBe -1
    }

//    @Test
//    fun `correctly uses fromIndex and toIndex to match a subset`() {
//        containsPartOf(
//            where = "encoding='UTF-8'?>".toCharArray(),
//            what = "hello_encoding_world".toCharArray(),
//            whatFromIndex = 6,
//        ) shouldBe 8
//    }

    @Test
    fun `throws when fromIndex is out of bounds`() {
        val exception = assertThrows<IllegalArgumentException>() {
            containsPartOf("abc".toCharArray(), "def".toCharArray(), whatFromIndex = 5)
        }
        exception.message shouldBe "fromIndex out of bounds"
    }

}