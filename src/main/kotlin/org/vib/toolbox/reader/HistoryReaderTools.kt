package org.vib.toolbox.reader

fun IHistoryReader.readToLineBreak(matchPosition: MatchPosition = MatchPosition.AFTER, trimmed: Boolean = true, readLimit: IHistoryReader.ReadLimit = IHistoryReader.ReadLimit.UNLIMITED): String? {
    if(matchPosition == MatchPosition.AFTER && trimmed){
        val res =  readTo('\n', '\r', matchPosition = MatchPosition.BEFORE, readLimit = readLimit)
        if(peekNext() == '\r') {
            goForward()
        }
        if(peekNext() == '\n'){
            goForward()
        }
        return res
    }
    else {
        return readTo('\n', '\r', matchPosition = matchPosition, readLimit = readLimit)
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

/**
 * @param goToPreviousLine - if current position is begin of a line (\n) -> go to previous line
 */
fun IHistoryReader.goBackToLineBegin(goToPreviousLine: Boolean = false): Boolean {
    if((peekCurrent() == '\n') && !goToPreviousLine ) return true
   val res = goBackTo('\n', matchPosition = MatchPosition.AFTER)
   return res || markPosition() == 0L // 0 -> start of a stream
}