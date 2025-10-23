package org.vib.toolbox.reader

fun IHistoryReader.readToLineBreak(matchPosition: MatchPosition = MatchPosition.AFTER, trimmed: Boolean = true): String? {
    if(matchPosition == MatchPosition.AFTER && trimmed){
        val res =  readTo('\n', '\r', matchPosition = MatchPosition.BEFORE)
        if(peekNext() == '\r') {
            goForward()
        }
        if(peekNext() == '\n'){
            goForward()
        }
        return res
    }
    else {
        return readTo('\n', '\r', matchPosition = matchPosition)
    }
}

fun HistoryReader.skipSpaces() : HistoryReader{
    val space = ' '



    var next : Char?
    do {
        next = readChar() ?: return this
    }
    while (next == space)

    goBack()

    return this
}

fun IHistoryReader.goBackToLineBegin(): Boolean {
   val res = goBackTo('\n', matchPosition = MatchPosition.AFTER)
   return res || markPosition() == 0L // 0 -> start of a stream
}