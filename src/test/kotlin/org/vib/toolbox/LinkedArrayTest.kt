package org.vib.toolbox

import org.vib.toolbox.collections.LinkedArray
import io.kotest.assertions.assertSoftly
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class LinkedArrayTest {

    @Test
    fun shouldCreateCollection() {
        val collection = LinkedArray<String>()
    }

    @Test
    fun shouldAddElementsToEnd() {
        val collection = LinkedArray<Int>(10)
        collection.size shouldBe 0
        for (i in 1..10) {
            collection.add(i)
            collection.size shouldBe i
        }
    }

    @Test
    fun shouldTransformElementsToList() {
        val collection = LinkedArray<Int>(10)
        collection.size shouldBe 0
        for (i in 1..10) {
            collection.add(i)
            collection.size shouldBe i
        }

        collection.toList() shouldContainExactly listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)

    }

    @Test
    fun shouldTransformElementsToListAndIgnoreEmptyElements() {
        val collection = LinkedArray<Int>(10)
        collection.size shouldBe 0
        for (i in 1..5) {
            collection.add(i)
            collection.size shouldBe i
        }

        collection.toList() shouldContainExactly listOf(1, 2, 3, 4, 5)

    }


    @Test
    fun ifOversizeLastElementsShouldBeReplaced() {
        val collection = LinkedArray<Int>(10)
        collection.size shouldBe 0
        collection.addAll(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
        collection.toList() shouldContainExactly listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)

        for (i in 11..15) {
            collection.add(i)
        }

        collection.toList() shouldContainExactly listOf(6, 7, 8, 9, 10, 11, 12, 13, 14, 15)

    }

    @Test
    fun cabBeOversizedTwice() {
        val collection = LinkedArray<Int>(10)
        collection.size shouldBe 0
        collection.addAll(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
        collection.toList() shouldContainExactly listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)

        for (i in 11..30) {
            collection.add(i)
        }

        collection.toList() shouldContainExactly listOf(21, 22, 23, 24, 25, 26, 27, 28, 29, 30)
    }

    @Test
    fun canGetCurrentElement() {
        val collection = LinkedArray<Int>(10)
        for (i in 1..30) {
            collection.add(i)
            collection.goNext() shouldBe false
            collection.hasCurrent() shouldBe true
            collection.getCurrent() shouldBe i
        }
    }

    @Test
    fun canPrevElement() {
        val collection = LinkedArray<Int>(10)
        collection.addAll(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
        for (i in 0..9) {
            collection.getCurrent() shouldBe 10 - i
            collection.goBack() shouldBe  true
        }
        collection.goBack() shouldBe false
        collection.getCurrent() shouldBe null
    }

    @Test
    fun cannotMoveFromLastToFistUsingGoNext() {
        val collection = LinkedArray<Int>(10)
        collection.addAll(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
        collection.getCurrent() shouldBe 10
        collection.goNext() shouldBe false
        collection.getCurrent() shouldBe 10
    }

    @Test
    fun cannotGoBackIfNotExists() {
        val collection = LinkedArray<Int>(10)
        collection.addAll(1)
        collection.getCurrent() shouldBe 1
        collection.goBack() shouldBe true
        collection.getCurrent() shouldBe null
        collection.goBack() shouldBe false
    }

    @Test
    fun cannotGoNextIfNotExists() {
        val collection = LinkedArray<Int>(10)
        collection.addAll(1)
        collection.getCurrent() shouldBe 1
        collection.goNext() shouldBe false
        collection.getCurrent() shouldBe 1
    }


    @Test
    fun cannotMoveFromFirstToLastUsingGoNext() {
        val collection = LinkedArray<Int>(10)
        collection.addAll(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
        collection.getCurrent() shouldBe 10
        collection.goToFirstRead() shouldBe true
        collection.getCurrent() shouldBe 1
        collection.goBack() shouldBe true
        collection.getCurrent() shouldBe null
        collection.goBack() shouldBe false
    }

    @Test
    fun returnNullIfEmpty() {
        val collection = LinkedArray<Int>(10)
        collection.getCurrent() shouldBe null
        collection.goBack() shouldBe false
        collection.getCurrent() shouldBe null
        collection.goNext() shouldBe false
        collection.getCurrent() shouldBe null
    }

    @Test
    fun shouldGetCurrentReadPositionWhenBufferIsFull() {
        assertSoftly {
            val reader = LinkedArray<Int>(5)
            reader.addAll(1, 2, 3, 4, 5, 6, 7, 8, 9)
            reader.currentPositionFromFirstRead shouldBe 4
            reader.currentPositionFromLastRead shouldBe 0
            reader.getCurrent() shouldBe 9
            reader.size shouldBe 5

            reader.goBack()
            reader.currentPositionFromFirstRead shouldBe 3
            reader.currentPositionFromLastRead shouldBe 1
            reader.getCurrent() shouldBe 8
            reader.size shouldBe 5


            reader.goBack()
            reader.currentPositionFromFirstRead shouldBe 2
            reader.currentPositionFromLastRead shouldBe 2
            reader.getCurrent() shouldBe 7
            reader.size shouldBe 5


            reader.goToFirstRead()
            reader.currentPositionFromFirstRead shouldBe 0
            reader.currentPositionFromLastRead shouldBe 4
            reader.getCurrent() shouldBe 5
            reader.size shouldBe 5


            reader.goToLastRead()
            reader.currentPositionFromFirstRead shouldBe 4
            reader.currentPositionFromLastRead shouldBe 0
            reader.size shouldBe 5
        }
    }
}