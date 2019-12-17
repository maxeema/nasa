package maxeem.america.nasa.ext

import maxeem.america.nasa.misc.ImageInfo
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import kotlin.random.Random
import kotlin.random.nextInt

class ImageInfoTest {

    @Test
    fun saveFileName_extensions() {
        for ((idx, ext) in arrayOf("WEBP", "Jpeg", "JPG", "PNG", "Gif").withIndex()) {
            ImageInfo.of("https://some.host/nice-photo-$idx.$ext", "2019-01-2$idx", true, "Mars Photo $idx", null).apply {
                println("${getTitledSaveFileName()} <- $url <- $date <- $title")
                assertTrue { getTitledSaveFileName().endsWith(ext.toLowerCase()) }
            }
        }
    }
    @Test
    fun saveFileName_noExtension() {
        ImageInfo.of("https://some.host/moon-pic/792354620", "2019-01-23", true, "Moon", null).apply {
            println("${getTitledSaveFileName()} <- $url <- $date <- $title")
            assertTrue { getTitledSaveFileName().endsWith(".jpg") }
        }
    }
    @Test
    fun saveFileName_badChars() {
        val expected = "NASA 2019-12-10 Super 1 Cool Mars.Ok - HD.jpg"
        val title = "Super\\ :1 ::  /| ? ! \uD83D\uDE0A Cool\t Mars.Ok"
        val info = ImageInfo.of("https://some.host/testpic/123", "2019-12-10", true, title, null)
        val saveFileName = info.getTitledSaveFileName()
        //
        println("result: $saveFileName\ninput: $title")
        //
        assertTrue { saveFileName.length <= 64 }
        assertFalse { saveFileName.contains("  ")}
        assertFalse { saveFileName.contains(": ")}
        assertFalse { saveFileName.contains("\n") }
        assertFalse { saveFileName.contains("\t") }
        assertFalse { saveFileName.contains("\\`") }
        assertEquals ( saveFileName, expected )
        //
    }
    @Test
    fun saveFileName_maxLength100() {
        val less = CharArray(size = 25) { Random.nextInt('a'.toInt()..'z'.toInt()).toChar() }.let { String(it) }
        val greater = CharArray(size = 155) { 'g' }.let { String(it) }
        for (title in arrayOf(less, greater)) {
            val info = ImageInfo.of("https://some.host/some/pic123.webp", "2019-12-15", true, title, null)
            val saveFileName = info.getTitledSaveFileName()
            println("${saveFileName.length}/${title.length}: $saveFileName")
            assertTrue { saveFileName.length <= 100 }
        }
    }

}