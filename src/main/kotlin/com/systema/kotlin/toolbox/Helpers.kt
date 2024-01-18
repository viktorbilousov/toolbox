@file:Suppress("unused")

package com.systema.kotlin.toolbox

import java.awt.Point
import java.awt.Rectangle
import java.io.File
import java.time.Instant
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

//#################### Wating ####################

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

    val clazz = this.javaPrimitivePrimitiveTypeToKotlin()

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
fun <T> Class<T>.javaPrimitivePrimitiveTypeToKotlin(): Class<T>{
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


// ################### String builders ###################

fun StringBuilder.newLine(cnt: Int = 1): StringBuilder {
    for ( i in 1..cnt){
        this.append("\n")
    }
    return this;
}
