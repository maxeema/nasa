package maxeem.america.nasa.net

import com.squareup.moshi.Json
import maxeem.america.nasa.domain.Apod
import maxeem.america.nasa.domain.MediaType
import maxeem.america.common.Str

data class ApodDTO(
    val url: Str,
    @Json(name = "hdurl") val hdUrl: Str?,
    val title: Str,
    val explanation: Str?,
    val date: Str,
    val copyright: Str?,
    @Json(name = "service_version") val version: Str,
    @Json(name = "media_type") val type: Str
)

fun ApodDTO.toDomain() = Apod(
        url = url,
        hdUrl = hdUrl,
        title = title,
        description = explanation,
        date = date,
        mediaType = toDomainType()
)

fun List<ApodDTO>.toDomain() = map {
        it.toDomain()
}

fun ApodDTO.toDomainType() = when {
        type.equals("image", ignoreCase = true) -> MediaType.Image
        type.equals("video", ignoreCase = true) && url.contains("youtube", ignoreCase = true) -> MediaType.YouTube
        else -> MediaType.Unknown(type)
}
