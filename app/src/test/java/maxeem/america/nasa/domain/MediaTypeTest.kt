package maxeem.america.nasa.domain

import org.junit.Test
import org.junit.jupiter.api.Assertions.assertTrue

internal class MediaTypeTest {

    @Test
    fun youtubeVideoId() {
        "https://www.youtube.com/embed/ofZTOxC9JQ4?rel=0".apply {
            assertTrue { "ofZTOxC9JQ4" == MediaType.YouTube.videoIdOf(this) }
        }
        "https://www.youtube.com/embed/ofZTOxC9JQ4".apply {
            assertTrue { "ofZTOxC9JQ4" == MediaType.YouTube.videoIdOf(this) }
        }
    }
    @Test
    fun youtubePreviewUrl() {
        val url = "https://youtube.com/embed/da77saJa33x?rel=0&some_args"
        val expected = "https://img.youtube.com/vi/da77saJa33x/sddefault.jpg"
        assertTrue { expected == MediaType.YouTube.previewOf(url, hd = false) }
    }
    @Test
    fun youtubePreviewUrlHd() {
        val url = "https://www.youtube.com/embed/38saJaB9gs"
        val expected = "https://img.youtube.com/vi/38saJaB9gs/maxresdefault.jpg"
        assertTrue { expected == MediaType.YouTube.previewOf(url, hd = true) }
    }

}