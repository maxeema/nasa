package maxeem.america.nasa.ext

import maxeem.america.nasa.Conf
import maxeem.america.common.Str
import java.util.*

private
val tikDots = arrayOf("...", "   ", ".  ", ".. ")
fun Str.toTikString(tik: Int) = this + tikDots[tik % tikDots.size]

fun Str.toApiCalendar() : Calendar? =
    runCatching {
        Calendar.getInstance().also {
            it.clear()
            it.time = Conf.dateFormatter.parse(this)
        }
    }.getOrNull()

fun Str.mask(c : Char = '_') = String(CharArray(length) { c })
fun Str.fixFileName() =
    replace("[^a-zA-Z0-9.-]".toRegex(), " ").replace("\\s{2,}".toRegex(), " ")