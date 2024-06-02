import java.io.BufferedReader
import java.io.IOException
import java.io.Reader
import java.io.StringReader
import java.nio.file.Files

object ReaderDemo {
    @JvmStatic
    fun main(args: Array<String>) {
        try {
            val s = "Hello World"

//            BufferedReader()
//            Files.newBufferedReader()
            // create a new StringReader
            val reader: Reader = StringReader(s)

            // read the first five chars
            for (i in 0..4) {
                val c = reader.read().toChar()
                print("" + c)
            }

            println("\nHi")
            // mark current position for maximum of 10 characters
            reader.mark(10)

            // read five more chars
            for (i in 0..5) {
                val c = reader.read().toChar()
                print("" + c)
            }

            // reset back to the marked position
            reader.reset()

            // change line
            println()

            // read six more chars
            for (i in 0..5) {
                val c = reader.read().toChar()
                print("" + c)
            }

            // close the stream
            reader.close()
        } catch (ex: IOException) {
            ex.printStackTrace()
        }
    }
}