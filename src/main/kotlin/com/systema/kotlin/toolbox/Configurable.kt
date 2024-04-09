package com.systema.kotlin.toolbox

abstract class Configurable<out E>(private val configuration: E.() -> Unit) {
    protected var config: @UnsafeVariance E? = null
    abstract fun createConfigurator(): E
    protected fun installConfiguration() {
        val config = createConfigurator()
        config.configuration()
        configure(config)
    }

    protected open fun configure(config: @UnsafeVariance E){}
}