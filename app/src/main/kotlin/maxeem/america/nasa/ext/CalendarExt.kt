package maxeem.america.nasa.ext

import maxeem.america.nasa.Conf
import java.util.*

fun Calendar.toApiDate() = Conf.dateFormatter.format(time)

fun Calendar.asYearMonthDay() = apply {
    val (year, month, day) = Triple(get(Calendar.YEAR), get(Calendar.MONTH), get(Calendar.DAY_OF_MONTH))
    clear()
    set(year, month, day)
}
