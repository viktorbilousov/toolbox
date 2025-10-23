package org.vib.toolbox.reader

import java.io.Reader

abstract class HistoryReader : Reader(), IHistoryReader{
    companion object{
        @JvmStatic
        fun ofBuffered(reader: Reader): HistoryBufferedReader{
            return HistoryBufferedReader(reader)
        }
    }
}