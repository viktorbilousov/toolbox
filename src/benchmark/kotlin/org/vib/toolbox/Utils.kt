package org.vib.toolbox


import java.io.File
import kotlin.time.Duration

object Utils {

    val sourceFile = File("src/benchmark/resources/eqc.log")
    val targetFolder = File("src/benchmark/resources/target/")
    val files get() = getFiles("src/benchmark/resources/target/*")
    var filesSizeKb : Long = 0
    val fileSizeMb: Double by lazy { filesSizeKb.toDouble() / 1024 }

    @JvmStatic
    fun createFiles(){
        targetFolder.mkdirs()
        if(targetFolder.listFiles()!!.isNotEmpty()){
            for (file in targetFolder.listFiles()) {
                val bytes = sourceFile.length()
                filesSizeKb += bytes / 1024  // divide by 1024 for KB
            }
            return
        }
        for ( i in 1 .. 10){
            sourceFile.copyTo(targetFolder.child("eqc-$i.log"))
            val bytes = sourceFile.length()
            filesSizeKb += bytes / 1024  // divide by 1024 for KB
        }

    }

    public fun printInfo(name: String, time: Duration, files : Int = 10, comparing: Double? = null){
        val size = fileSizeMb/(10 - files + 1)

        var avg = ""
        if(comparing != null && comparing > 0 ){
            val perc = ((time.inWholeMilliseconds / comparing) * 100).toInt()
            avg = " [$perc%]"
        }

        println("[$name]  Read $files files $size Mb in $time (${(size *1000)/time.inWholeMilliseconds} mb/s) $avg")
    }

}