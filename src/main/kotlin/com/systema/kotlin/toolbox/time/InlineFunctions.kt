package com.systema.kotlin.toolbox.time

import java.time.Instant

fun HasTime.between(from: Instant, to: Instant, includeTimeNull: Boolean = false, ignoreTimeNull: Boolean = false): Boolean {
    if(this.time == null){
        if(!ignoreTimeNull && !includeTimeNull) error("cannot filter transaction: time == null!")
        return includeTimeNull
    }
    return this.time!!.isAfter(from) && this.time!!.isBefore(to)
}
