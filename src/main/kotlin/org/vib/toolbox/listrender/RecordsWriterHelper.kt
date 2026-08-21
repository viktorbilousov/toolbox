@file:Suppress("unused")

package org.vib.toolbox.listrender

import org.vib.toolbox.getLastPathPartsStr
import org.vib.toolbox.getPathParts
import java.io.File

object RecordsWriterHelper{
    fun createGroupsFromFiles(files: Collection<File>): Map<String, File> {
        val fileName2Content = files.filter { it.exists() }.associateBy { it.absoluteFile }
        val keys = fileName2Content.keys.map { it.getPathParts() }.toMutableList()
        val maxLen = keys.maxOf { it.size }
        var instant = 0
        for (i in 0 until maxLen) {
            val firstKey = keys.first()[i]
            if (keys.map { it[i] }.all { firstKey == it }) {
                instant++
            } else {
                break
            }
        }
        return fileName2Content.mapKeys { it.key.getLastPathPartsStr(it.key.getPathParts().size - instant) }
    }


    fun<V> replaceFilesToGroups(map: Map<File, V>): Map<String, V>{
        val group2Files = createGroupsFromFiles(map.keys)
        return map.mapKeys { key -> group2Files.entries.first { it.value == key.key  }.key }
    }

}
