package org.vib.toolbox

abstract class Configurable<out E> {

    protected var config: @UnsafeVariance E? = null

    constructor(config: E) {
        this.config = config;
    }

    protected fun installConfiguration() {
        configure(config!!)
    }

    protected open fun configure(config: @UnsafeVariance E){}
}