package maxeem.america.nasa.net

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import maxeem.america.nasa.domain.MediaType
import maxeem.america.nasa.net.ApodDTORaw.mediaImage
import maxeem.america.nasa.net.ApodDTORaw.mediaImageUppercase
import maxeem.america.nasa.net.ApodDTORaw.mediaUnknownGallery
import maxeem.america.nasa.net.ApodDTORaw.mediaUnknownVideo
import maxeem.america.nasa.net.ApodDTORaw.mediaYouTube
import maxeem.america.nasa.net.ApodDTORaw.mediaYouTubeUppercase
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class DTOsKtTest {

    private lateinit var adapter: JsonAdapter<ApodDTO>

    private fun String.fromJson() = adapter.fromJson(this)

    @BeforeAll
    fun prepare() {
        Moshi.Builder().run {
            add(KotlinJsonAdapterFactory())
            build().let {
                adapter = it.adapter<ApodDTO>(ApodDTO::class.java)
            }
        }
    }

    @Test
    fun toDomainType_mediaImage() {
        for (raw in arrayOf(mediaImage, mediaImageUppercase))
            toDomainType(raw, MediaType.Image)
    }
    @Test
    fun toDomainType_mediaVideo() {
        for (raw in arrayOf(mediaYouTube, mediaYouTubeUppercase))
            toDomainType(raw, MediaType.YouTube)
    }
    @Test
    fun toDomainType_mediaUnknown() {
        toDomainType(mediaUnknownGallery, MediaType.Unknown("gallery"))
        toDomainType(mediaUnknownVideo, MediaType.Unknown("gallery"))
    }

    private fun toDomainType(raw: String, mediaType: MediaType) {
        raw.fromJson()!!.also {
            assertTrue { it.toDomainType().javaClass === mediaType::class.java }
        }
    }

}