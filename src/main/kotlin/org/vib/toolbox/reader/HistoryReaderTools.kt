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

fun HistoryReader.skipSpaces(readFirstAfterSpace: Boolean = false) : Int{
    val space = ' '

    val currentIsSpace = peekCurrent() == space
    var cnt = 0
    var spaceDetected = false;
    var next : Char?
    while (true){
        next = readChar() ?: return cnt

        if(next == space){
            cnt++
            spaceDetected = true
        }
        else{
            if(cnt == 0){
                if(!currentIsSpace) {
                    goBack()
                }
                else if(!readFirstAfterSpace){
                    goBack()
                }
            }
            if(cnt > 0 && !readFirstAfterSpace) {
                goBack()
            }
            break
        }
    }

    return cnt
}

fun IHistoryReader.goBackToLineBegin(): Boolean {
   val res = goBackTo('\n', matchPosition = MatchPosition.AFTER)
   return res || markPosition() == 0L // 0 -> start of a stream
}