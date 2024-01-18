package com.systema.kotlin.toolbox

import java.sql.Timestamp
import java.text.DateFormat
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

object TSUtil {
    val TIME_FORMAT_WITH_SPACE : DateTimeFormatter          = formatter("dd.MM.yyyy HH:mm:ss")
    val TIME_FORMAT_EQC_EQS_LOG_FILES : DateTimeFormatter   = formatter("yyyy/MM/dd HH:mm:ss.SSS")
    val DEFAULT_TIME_FORMAT : DateTimeFormatter             = TIME_FORMAT_WITH_SPACE
    val INPUT_GUI_TIME_FORMAT : DateTimeFormatter           = TIME_FORMAT_WITH_SPACE
    val ISO_TIME_FORMAT : DateTimeFormatter                 = formatter("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    const val ISO_TIME_FORMAT_STR : String                  = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
    const val TIME_FORMAT_WITH_SPACE_STR : String           = "dd.MM.yyyy HH:mm:ss"
    val TIME_WITHOUT_DOUBLE_POINTS : DateTimeFormatter      = formatter("dd.MM.yyyy HH-mm-ss")


    @Deprecated("use Instant")
    fun parseTs(string: String, dataFormat: DateFormat): Timestamp {
        dataFormat.timeZone = TimeZone.getTimeZone("UTC")
        return Timestamp.from(dataFormat.parse(string).toInstant())
    }


    fun parseTs(string: String, dataFormat: DateTimeFormatter = DateTimeFormatter.ISO_DATE_TIME): Instant {
        var formatter = dataFormat
        if(dataFormat.zone == null) {
            formatter = formatter.withZone(ZoneId.of("UTC"))
        }
        return Instant.from(formatter.parse(string))
    }

    fun formatTs(timestamp: Instant, dataFormat: DateTimeFormatter): String {
        if(dataFormat.zone == null){
            dataFormat.withZone(ZoneId.of("UTC"))
        }
        return dataFormat.format(timestamp)
    }


    fun zeroTime(datetime: DateTimeFormatter = DEFAULT_TIME_FORMAT): String = formatTs(Instant.ofEpochMilli(0), datetime)

    fun currentTime(datetime: DateTimeFormatter = DEFAULT_TIME_FORMAT): String = formatTs(currentTimeUTC(), datetime)

    fun currentTimeUTC(): Instant {
        return Instant.ofEpochMilli(Instant.now(Clock.systemUTC()).toEpochMilli()) // remove nanoseconds
    }

    fun zeroTimeUTC(): Instant = Instant.ofEpochMilli(0)


    fun formatter(pattern: String) = DateTimeFormatter.ofPattern(pattern).withZone(ZoneId.of("UTC"))
}