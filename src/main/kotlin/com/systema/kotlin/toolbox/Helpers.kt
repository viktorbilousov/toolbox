@file:Suppress("unused")

package com.systema.kotlin.toolbox

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.awt.Point
import java.awt.Rectangle
import java.io.File
import java.io.Reader
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.reflect.KClass
import kotlin.reflect.cast
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.seconds

//#################### Common ####################

fun <T,R> withNotNull(receiver: T? , block: (T) -> R) : R? {
    return if (receiver != null) block(receiver)
    else null
}

operator fun Point.plus(point: Point) : Point{
    return Point(this.x + point.x, this.y + point.y)
}

operator fun Point.minus(point: Point) : Point{
    return Point(this.x - point.x, this.y - point.y)
}


infix fun Int.x(y : Int) : Point = Point(this, y)

val Rectangle.point get() = Point(this.x, this.y)
val Rectangle.center get() = Point(this.x + this.width/2, this.y + this.height/2)

//#################### String ####################


fun String.splitAndTrim(vararg delimiters : String) : List<String> {
    return this.split(*delimiters).map { it.trim() }
}

fun String.inline(newLineSeparator: String = "\\n") = this.replace("\r", "").replace("\n", newLineSeparator)

fun String.lines(lineSeparator: String = "\n", trim: Boolean = false) = this.split(lineSeparator).map { if(trim) it.trim() else it  }

fun String.toStringWihtNumbersOfLines(): String{
    val lines = this.split("\n")
    val linesCnt = lines.size.toString().length

    val sb = StringBuilder()
    var i = 1
    for (line in lines) {
        sb
            .spaces(linesCnt - i.toString().length + 1)
            .append(i.toString())
            .append("|    ")
            .append(line)
            .newLine()
        i++
    }
    return sb.trimEnd().toString()
}


//#################### Number ####################

fun Int.iterate(from: Int = 1, block: (Int) -> Unit ){
    for (i in from .. this){
        block(i)
    }
}
fun Int.iterateReversed(to: Int = 1, block: (Int) -> Unit ){
    for (i in this downTo  to){
        block(i)
    }
}

fun Double.round(decimals: Int = 2): Double = "%.${decimals}f".format(this).toDouble()
fun Float.round(decimals: Int = 2): Double = "%.${decimals}f".format(this).toDouble()


//#################### Collections ####################


fun <T> Collection<T>.containsAllExactly(collection: Collection<T>): Boolean {
    return this.containsAll(collection) && this.size == collection.size
}


fun <T> T.wrapToList() : List<T> = listOf(this)

fun <T> List<T>.asMutable() : MutableList<T> = this as MutableList<T>
fun <K,V> Map<K,V>.asMutable() : MutableMap<K,V> = this as MutableMap<K,V>

inline fun <T> ignoreException(handle: (e: Exception) -> Unit = {}, block: () -> T?): T? {
    return try {
        block.invoke()
    } catch(e: Exception) {
        handle(e)
        null
    }
}

//#################### Waiting ####################

fun sleep(duration: Duration){
    Thread.sleep(duration.inWholeMilliseconds)
}

fun sleep(mils: Number){
    Thread.sleep(mils.toLong())
}


fun waitFor(limitSec: Duration = 10.seconds, sleepMils: Int = 100, condition: () -> Boolean) : Boolean {
    val start = Instant.now()
    while (true){
        if(limitSec.inWholeMilliseconds > 0 && Instant.now().toEpochMilli() - start.toEpochMilli() > limitSec.inWholeMilliseconds) return false
        if(condition()) return true
        sleep(sleepMils)
    }
}

fun waitUntil(sleepMils: Int = 100, condition: () -> Boolean): Boolean {
    return waitFor(1.hours, sleepMils , condition)
}

//#################### Time ####################


fun Instant.between(from: Instant, to: Instant): Boolean {
    return this.isAfter(from) && this.isBefore(to)
}

fun DateTimeFormatter.formatTime(dateTime: String, zoneId: ZoneId = ZoneId.systemDefault()): Instant {
    return TSUtil.parseTs(dateTime, this, zoneId)
}

val ZoneId.UTC : ZoneId get() = ZoneId.of("UTC")
val ZoneId.Default : ZoneId get() = ZoneId.systemDefault()

//#################### Classes ####################

infix fun Class<*>.instanceOf(clazz: Class<*>) = clazz.isAssignableFrom(this)
infix fun Class<*>.isNotInstanceOf(clazz: Class<*>) = !clazz.isAssignableFrom(this)

infix fun Any.instanceOf(clazz: Class<*>) = clazz.isAssignableFrom(this::class.java)
infix fun Any.isNotInstanceOf(clazz: Class<*>) = !clazz.isAssignableFrom(this::class.java)

@Suppress("UNCHECKED_CAST")
fun<T: Any> KClass<T>.castObj(obj: Any?): T? {
    if(obj == null) return null

    try {
        return when (this) {
            Long::class.java -> obj.toString().toLong() as T
            Int::class.java -> obj.toString().toInt() as T
            Byte::class.java -> obj.toString().toByte() as T
            Short::class.java -> obj.toString().toShort() as T
            Char::class.java -> obj.toString()[0] as T
            Float::class.java -> obj.toString().toFloat() as T
            Double::class.java -> obj.toString().toDouble() as T
            Boolean::class.java -> obj.toString().toBoolean() as T
            else -> this.cast(obj)
        }
    }catch (e: NumberFormatException){
        throw ClassCastException("Cannot cast '$obj' as $this")
    }
}

@Suppress("UNCHECKED_CAST", "DuplicatedCode")
fun<T> Class<T>.castObj(obj: Any?): T? {
    if(obj == null) return null

    val clazz = this.javaPrimitiveTypeToKotlin()

    try {
        return when (clazz) {
            Long::class.java -> obj.toString().toLong() as T
            Int::class.java -> obj.toString().toInt() as T
            Byte::class.java -> obj.toString().toByte() as T
            Short::class.java -> obj.toString().toShort() as T
            Char::class.java -> obj.toString()[0] as T
            Float::class.java -> obj.toString().toFloat() as T
            Double::class.java -> obj.toString().toDouble() as T
            Boolean::class.java -> obj.toString().toBoolean() as T
            String::class.java -> obj.toString() as T
            else -> this.cast(obj)
        }
    }catch (e: NumberFormatException){
        throw ClassCastException("Cannot cast '$obj' as $this")
    }
}


/**
 * Returns a Java [Class] instance representing the primitive type corresponding to the given [KClass] if it exists.
 */
fun <T> Class<T>.javaPrimitiveTypeToKotlin(): Class<T>{
    @Suppress("UNCHECKED_CAST")
    return when (this.name) {
        "java.lang.Boolean" -> Boolean::class.java
        "java.lang.Character" -> Char::class.java
        "java.lang.Byte" -> Byte::class.java
        "java.lang.Short" -> Short::class.java
        "java.lang.Integer" -> Int::class.java
        "java.lang.Float" -> Float::class.java
        "java.lang.Long" -> Long::class.java
        "java.lang.Double" -> Double::class.java
        "java.lang.Void" -> Void.TYPE
        else -> this
    } as Class<T>
}


//#################### Files ####################


fun File.notExists(): Boolean {
    return !this.exists()
}

fun File.child(vararg children: String): File {
    var current = this
    for (child in children) {
        current = File(current, child)
    }
    return current
}

fun File.createNewFileIfNotExists(): Boolean{
    val absoluteFile = this.absoluteFile
    if(absoluteFile.exists()) return false
    val parent = absoluteFile.parentFile
    if(!parent.exists()){
        parent.mkdirs()
    }
    absoluteFile.createNewFile()
    return true
}



fun File.setExtension(extension: String) : File{
    val ex = extension.trim()
    return if(ex.isEmpty())
    {
        File(this.parentFile, this.nameWithoutExtension)
    }
    else
    {
        File(this.parentFile, this.nameWithoutExtension + "." + ex)
    }
}

fun File.getPathParts() : List<String>{
    return this.absolutePath.split(File.separator)
}

fun File.getLastPathParts(partsCnt: Int, removeExtension: Boolean = false) : List<String>{
    val file = if(removeExtension) this.setExtension("") else this
    return file.getPathParts().let {
        it.takeLast(minOf(partsCnt, it.size))
    }
}

fun File.getLastPathPartsStr(partsCnt: Int, removeExtension: Boolean = false, splitter: String= File.separator) : String {
    return getLastPathParts(partsCnt, removeExtension).joinToString(splitter)
}

private fun getFilesRec(folder: File, files: MutableList<File>){
    if(!folder.exists() || folder.isFile) return
    val filesAndFolders =  folder!!.listFiles()!!.map { it!! }
    filesAndFolders.filter { it.isFile }.forEach {
        files.add(it)
    }
    filesAndFolders.filter { it.isDirectory }.forEach {
        getFilesRec(it, files)
    }
}

fun getFiles(fileOrFolderPath: String): List<File>{
    if(fileOrFolderPath.endsWith("\\**")){
        val folder = fileOrFolderPath.replaceAfterLast("\\", "") + "\\"
        val folderFile = File(folder).absoluteFile
        if(!folderFile.exists()) return listOf();
        val files = mutableListOf<File>()
        getFilesRec(folderFile, files)
        return files
    }
    else if(fileOrFolderPath.endsWith("\\*")){
        val folder = fileOrFolderPath.replaceAfterLast("\\", "") + "\\"
        val folderFile = File(folder).absoluteFile
        if(!folderFile.exists()) return listOf();
        return folderFile.listFiles()!!.filter { it.isFile }
    }
    else{
        return File(fileOrFolderPath).wrapToList()
    }
}

// ################### String builders ###################

fun StringBuilder.newLine(cnt: Int = 1): StringBuilder {
    return repeat('\n', cnt)
}

fun StringBuilder.spaces(cnt: Int): StringBuilder {
    return repeat(' ', cnt)

}

fun StringBuilder.repeat(char: Char, cnt: Int): StringBuilder {
    for ( i in 1..cnt){
        this.append(char)
    }
    return this
}

fun StringBuilder.repeat(text: String, cnt: Int): StringBuilder {
    for ( i in 1..cnt){
        this.append(text)
    }
    return this
}


// ################### Loggers ###################

private fun loggerAnyToName(any: Any): String{
    return when (any) {
        is String -> any
        is Class<*> -> any.simpleName
        is KClass<*> -> any.simpleName ?: any.java.simpleName
        else -> any.toString()
    }
}


fun CharArray.asText() = String(this)

fun logger(any: Any): Logger = LoggerFactory.getLogger(loggerAnyToName(any))
fun logger(clazz: Class<*>): Logger = LoggerFactory.getLogger(clazz.simpleName)
inline fun <reified T> logger() : Logger = LoggerFactory.getLogger(T::class.java.simpleName)
fun loggerWithId(any: Any, id: String): Logger = LoggerFactory.getLogger(loggerAnyToName(any) + "#" + id)
inline fun <reified T> loggerWithId(id: String): Logger = LoggerFactory.getLogger(T::class.java.simpleName + "#" + id)




//// ########################### StreamReader ###################################

fun Reader.readChar(): Char? {
    val code = this.read()
    if(code == -1) return null
    return code.toChar()
}

//
//fun ReaderWithMemory.goBackToLineBegin() :Boolean{
//    return goBackTo('\n')
//}
//fun ReaderWithMemory.goBackFromLineEndToLineBegin() :Boolean {
//    return hasPrev() &&  goBackTo('\n')
//}
//
//fun ReaderWithMemory.readToNextTrimmed(vararg char: Char): String? {
//    return readToNext(*char).asText().trim()
//}
//
//fun ReaderWithMemory.readToNextSpace() = readToNext(' ')
//fun ReaderWithMemory.readToLineBreak(trimEnd: Boolean = true) = readTextToNext( '\n')?.let { if(trimEnd) it.trimEnd() else it }
//fun ReaderWithMemory.readToLineBreakTrimmed() = readTextToNext( '\n')?.trim()
//
//fun skipSpaces() : TextReaderWithMemory{
//    var next : Char? = ' '
//    while (next == ' ')  next = readNext()
//    if(next != null) {
//        buffer.goBack()
//    }
//    return this
//}

