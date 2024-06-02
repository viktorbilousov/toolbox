package com.systema.kotlin.toolbox

import com.systema.kotlin.toolbox.builders.Builder

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