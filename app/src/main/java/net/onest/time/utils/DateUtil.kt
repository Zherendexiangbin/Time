package net.onest.time.utils

import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.TimeZone
import java.util.concurrent.TimeUnit
import kotlin.properties.Delegates

class DateUtil {
    private val currentDateTime: LocalDateTime = LocalDateTime.now()

    // 从今天起 往上推一天
    private val yesterdays: LocalDateTime = currentDateTime.minusDays(1)

    // 从今天起 往上推一周
    private val weeks: LocalDateTime = currentDateTime.minusWeeks(1).plusDays(1)

    // 从今天起 往上推一个月
    private val months: LocalDateTime = currentDateTime.minusMonths(1).plusDays(1)

    var todayEpochMill: Long =
        currentDateTime.toInstant(ZoneOffset.of("+8")).toEpochMilli() + 8 * 60 * 60 * 1000
    var yesterdayEpochMilli: Long =
        yesterdays.toInstant(ZoneOffset.of("+8")).toEpochMilli() + 8 * 60 * 60 * 1000
    var weekEpochMilli: Long =
        weeks.toInstant(ZoneOffset.of("+8")).toEpochMilli() + 8 * 60 * 60 * 1000
    var monthEpochMilli: Long =
        months.toInstant(ZoneOffset.of("+8")).toEpochMilli() + 8 * 60 * 60 * 1000


    companion object {
        val LOCAL_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val LOCAL_DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

        val curDay: String
            //获取当前日期
            get() {
                val calendar = Calendar.getInstance()
                val dateFormat = SimpleDateFormat("yyyy-MM-dd")
                val currentDate = dateFormat.format(calendar.time)
                return currentDate
            }

        val curWeek: String
            //获取当前一周的日期
            get() {
                val calendarWeek = Calendar.getInstance()
                val dateFormatWeek = SimpleDateFormat("yyyy-MM-dd")

                // 设置一周的第一天为周一
                calendarWeek[Calendar.DAY_OF_WEEK] = Calendar.MONDAY
                val startDate = dateFormatWeek.format(calendarWeek.time)

                // 获取本周的最后一天（星期日）
                calendarWeek.add(Calendar.DATE, 6)

                // 判断是否已经是月末，如果是，则将结束日期设置为当月最后一天
                if (calendarWeek[Calendar.DAY_OF_MONTH] == calendarWeek.getActualMaximum(Calendar.DAY_OF_MONTH)) {
                    calendarWeek[Calendar.DAY_OF_MONTH] =
                        calendarWeek.getActualMaximum(Calendar.DAY_OF_MONTH)
                } else {
                    // 如果不是月末，则继续获取本周的结束日期
                    calendarWeek[Calendar.DAY_OF_WEEK] = Calendar.SUNDAY
                }

                val endDate = dateFormatWeek.format(calendarWeek.time)
                return "$startDate ~ $endDate"
            }

        val curMonth: String
            //获取当前一月的日期
            get() {
                val calendarMonth = Calendar.getInstance()
                val dateFormatMonth = SimpleDateFormat("yyyy-MM-dd")
                // 获取当月的第一天
                calendarMonth[Calendar.DAY_OF_MONTH] = 1
                val startDateMonth = dateFormatMonth.format(calendarMonth.time)
                // 获取当月的最后一天
                calendarMonth[Calendar.DAY_OF_MONTH] =
                    calendarMonth.getActualMaximum(Calendar.DAY_OF_MONTH)
                val endDateMonth = dateFormatMonth.format(calendarMonth.time)

                return "$startDateMonth ~ $endDateMonth"
            }

        /**
         * 获得指定日期的 EpochMillisecond
         */
        @JvmStatic
        fun epochMillisecond(date: Date = Date()): Long {
            val calendar = Calendar.getInstance()
            calendar.time = date
            calendar[Calendar.HOUR_OF_DAY] = 0
            calendar[Calendar.MINUTE] = 0
            calendar[Calendar.SECOND] = 0
            calendar[Calendar.MILLISECOND] = 0
            return calendar.toInstant().epochSecond * 1000
        }

        /**
         * 获得指定日期的 EpochMillisecond
         */
        @JvmStatic
        fun epochMillisecond(calendar: Calendar): Long {
            calendar[Calendar.HOUR_OF_DAY] = 0
            calendar[Calendar.MINUTE] = 0
            calendar[Calendar.SECOND] = 0
            calendar[Calendar.MILLISECOND] = 0
            return calendar.toInstant().epochSecond * 1000
        }

        /**
         * 获得指定日期的 EpochMillisecond
         */
        @JvmStatic
        fun epochMillisecond(millisecond: Long) = epochMillisecond(Date(millisecond))

        /**
         * year年month月第一天的时间戳
         */
        @JvmStatic
        fun epochMillisecond(year: Int, month: Int, day: Int = 1): Long {
            val calendar = Calendar.getInstance()
            calendar[Calendar.YEAR] = year
            calendar[Calendar.MONTH] = month - 1
            calendar[Calendar.DAY_OF_MONTH] = day

            calendar[Calendar.HOUR_OF_DAY] = 0
            calendar[Calendar.MINUTE] = 0
            calendar[Calendar.SECOND] = 0
            calendar[Calendar.MILLISECOND] = 0
            return calendar.toInstant().epochSecond * 1000
        }
    }
}

fun LocalDateTime.dayString(): String {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = this.toEpochSecond(ZoneOffset.of("+8")) * 1000
    val dateFormat = SimpleDateFormat("yyyy-MM-dd")
    val currentDate = dateFormat.format(calendar.time)
    return currentDate
}

fun LocalDateTime.weekString(): String {
    val calendarWeek = Calendar.getInstance()
    calendarWeek.timeInMillis = this.toEpochSecond(ZoneOffset.of("+8")) * 1000
    val dateFormatWeek = SimpleDateFormat("yyyy-MM-dd")

    // 设置一周的第一天为周一
    calendarWeek[Calendar.DAY_OF_WEEK] = Calendar.MONDAY
    val startDate = dateFormatWeek.format(calendarWeek.time)

    // 获取本周的最后一天（星期日）
    calendarWeek.add(Calendar.DATE, 6)

    // 判断是否已经是月末，如果是，则将结束日期设置为当月最后一天
    if (calendarWeek[Calendar.DAY_OF_MONTH] == calendarWeek.getActualMaximum(Calendar.DAY_OF_MONTH)) {
        calendarWeek[Calendar.DAY_OF_MONTH] =
            calendarWeek.getActualMaximum(Calendar.DAY_OF_MONTH)
    } else {
        // 如果不是月末，则继续获取本周的结束日期
        calendarWeek[Calendar.DAY_OF_WEEK] = Calendar.SUNDAY
    }

    val endDate = dateFormatWeek.format(calendarWeek.time)
    return "$startDate ~ $endDate"
}

fun LocalDateTime.monthString(): String {
    val calendarMonth = Calendar.getInstance()
    calendarMonth.timeInMillis = this.toEpochSecond(ZoneOffset.of("+8")) * 1000
    val dateFormatMonth = SimpleDateFormat("yyyy-MM-dd")
    // 获取当月的第一天
    calendarMonth[Calendar.DAY_OF_MONTH] = 1
    val startDateMonth = dateFormatMonth.format(calendarMonth.time)
    // 获取当月的最后一天
    calendarMonth[Calendar.DAY_OF_MONTH] =
        calendarMonth.getActualMaximum(Calendar.DAY_OF_MONTH)
    val endDateMonth = dateFormatMonth.format(calendarMonth.time)

    return "$startDateMonth ~ $endDateMonth"
}

fun defaultZoneOffset(): ZoneOffset = ZoneOffset.of("+8")
fun defaultZoneId(): ZoneId = ZoneId.of("UTC+8")

fun LocalDateTime.toDate(): Date {
    return Date(this.toInstant(defaultZoneOffset()).toEpochMilli())
}

fun LocalDate.toDate(): Date {
    return this.atStartOfDay().toDate()
}

fun Date.toLocalDateTime(): LocalDateTime {
    return LocalDateTime.ofInstant(this.toInstant(), defaultZoneId())
}

fun Date.toLocalDate(): LocalDate {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = time
    return LocalDate.of(
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH) + 1,
        calendar.get(Calendar.DAY_OF_MONTH))
}
enum class DateTimeUnit {
    YEAR, MONTH, WEEK, DAY, HOUR, MINUTE, SECOND;
    var value by Delegates.notNull<Long>()
}

fun LocalDateTime.localFormat() = this.format(DateUtil.LOCAL_DATE_TIME_FORMAT)
fun LocalDate.localFormat() = this.format(DateUtil.LOCAL_DATE_FORMAT)

fun LocalDateTime.plus(value: Long, timeUnit: DateTimeUnit): LocalDateTime? {
    return when(timeUnit) {
        DateTimeUnit.YEAR -> this.plusYears(value)
        DateTimeUnit.MONTH -> this.plusMonths(value)
        DateTimeUnit.WEEK -> this.plusWeeks(value)
        DateTimeUnit.DAY -> this.plusDays(value)
        else -> null
    }
}

fun LocalDateTime.minus(value: Long, timeUnit: DateTimeUnit): LocalDateTime? {
    return when(timeUnit) {
        DateTimeUnit.YEAR -> this.minusYears(value)
        DateTimeUnit.MONTH -> this.minusMonths(value)
        DateTimeUnit.WEEK -> this.minusWeeks(value)
        DateTimeUnit.DAY -> this.minusDays(value)
        else -> null
    }
}
