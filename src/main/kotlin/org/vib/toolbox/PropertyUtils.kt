@file:Suppress("unused")

package org.vib.toolbox


fun env(key: String) : String? = System.getenv(key)?.trim()?.ifBlank { null }
fun env(key: String, default: String) : String = env(key) ?: default

fun prop(key: String) : String? = System.getProperty(key, null)?.trim()
fun prop(key: String, default: String) : String = System.getProperty(key, default)

fun envOrProp(key: String) = env(key) ?: prop(key)
fun envOrProp(key: String, default: String) = env(key) ?: prop(key) ?: default