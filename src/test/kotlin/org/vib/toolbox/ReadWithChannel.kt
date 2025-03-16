package org.vib.toolbox

import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.IOException
import java.nio.ByteBuffer

object ReadWithChannel {
    @JvmStatic
    fun main(args: Array<String>) {
        try {
            val sb = StringBuilder()
            FileInputStream("./src/test/resources/eqc-9632.log").channel.use { channel ->  //w ww  .j  a  v a 2  s  .  co m
                val buf = ByteBuffer.allocateDirect(8)

                var bytesRead = 0
                while (bytesRead >= 0) {
                    bytesRead = channel.read(buf)
                    buf.flip()
                    for (i in 0 until bytesRead) {
                        val b = buf.get()
                        sb.append(Char(b.toUShort()))
                    }
                }
            }
        } catch (e: FileNotFoundException) { // TODO Auto-generated catch block
            e.printStackTrace()
        } catch (e1: IOException) { // TODO Auto-generated catch block
            e1.printStackTrace()
        }
    }
}