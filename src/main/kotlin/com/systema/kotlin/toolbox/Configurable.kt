package com.systema.kotlin.toolbox

abstract class Configurable<out E>(private val configuration: E.() -> Unit) {
    abstract fun createConfigurator(): E
    protected fun installConfiguration() {
        createConfigurator().configuration()
    }
}