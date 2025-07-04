package org.vib.toolbox

import java.util.Arrays

object CharArrayComparator {
    fun containsPartOf(
        where: CharArray,
        what: CharArray,
        whatFromIndex: Int = 0,
    ): Int {
        require(whatFromIndex in what.indices) { "fromIndex out of bounds" }

        if(what.isEmpty()) return 0;

        val char = what[whatFromIndex]
        var indexes = mutableListOf<Int>()

        var i = 0;
        for (ch in where) {
            if(ch == char) {
               val res = checkIndex(i, where, what, whatFromIndex)
               if(res >= 0) return res
            }
            i++
        }
        return -1;
    }

    private fun checkIndex(whereFirstIndex: Int,
                           where: CharArray,
                           what: CharArray,
                           whatFromIndex: Int = 0,
                          ): Int {
        val whatToIndex: Int = what.lastIndex
        // fully included
        // abc123 and bc123
        if(where.lastIndex - whereFirstIndex >= whatToIndex -  whatFromIndex) {
            val contains = Arrays.equals(
                where,
                whereFirstIndex,
                whereFirstIndex + (whatToIndex - whatFromIndex) +1,
                what,
                whatFromIndex,
                whatToIndex + 1
            );
            return if (contains) (whatToIndex - whatFromIndex + 1)
            else -1;
        }

        else{
            val contains =  Arrays.equals(
                where,
                whereFirstIndex,
                where.size,
                what,
                whatFromIndex,
                whatFromIndex + (where.lastIndex - whereFirstIndex) + 1
            )

            return if(contains)  (where.lastIndex -  whereFirstIndex + 1)
            else -1;
        }
    }



}