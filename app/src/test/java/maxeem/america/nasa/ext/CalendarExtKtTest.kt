package maxeem.america.nasa.ext

import maxeem.america.nasa.Conf
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.*

internal class CalendarExtKtTest {

    val dateTextArray = arrayOf("2019-01-31", "2019-12-31")

    @Test
    fun test_toApiDate() {
        for (dateText in dateTextArray) {
            Calendar.getInstance().apply {
                time = Conf.dateFormatter.parse(dateText)!!
                assertTrue { dateText == toApiDate() }
            }
        }
    }
    @Test
    fun test_asYearMonthDay() {
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)
        c.asYearMonthDay().apply {
            assertTrue { 0 == get(Calendar.MILLISECOND) }
            assertTrue { 0 == get(Calendar.SECOND) }
            assertTrue { 0 == get(Calendar.MINUTE) }
            assertTrue { 0 == get(Calendar.HOUR) }
            //
            assertEquals(year, get(Calendar.YEAR))
            assertEquals(month, get(Calendar.MONTH))
            assertEquals(day, get(Calendar.DAY_OF_MONTH))
        }
    }

}