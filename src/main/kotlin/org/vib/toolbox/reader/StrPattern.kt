package org.vib.toolbox.reader

class StrPattern(val string: String) {
    val lps : Array<Int>

    init {
        val m = string.length
        lps  = Array(m){0}
        var j = 0
        var i = 1

        while (i < m){
            if(string[i] == string[j]){
                lps[i] = ++j
                i++
            }
            else{
                if(j != 0){
                    j = lps[j-1]
                }
                else {
                    lps[i] = 0
                    i++
                }
            }
        }
    }

    override fun toString(): String {
        return "StrPattern(string='$string', lps=${lps.contentToString()})"
    }

}

