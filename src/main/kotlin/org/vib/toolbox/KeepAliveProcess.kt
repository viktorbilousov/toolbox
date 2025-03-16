@file:Suppress("unused")

package org.vib.toolbox

import kotlin.concurrent.thread

object KeepAliveProcess{

    private var stop = true
    private var thread : Thread? = null
    val isRunning get() = thread != null

    fun start(){
        if(thread != null) return
        thread = thread {
            println("Start a keep alive process")
            runCatching {
                while (!stop) {
                    Thread.sleep(100L)
                }
            }
            println("Exit a keep alive process")
        }
        stop = false
    }

    fun stop(){
        if(thread == null) return
        stop = true
        if(thread!!.isAlive){
            thread!!.interrupt()
            thread = null
        }
    }
}


