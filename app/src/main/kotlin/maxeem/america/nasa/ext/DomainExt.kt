package maxeem.america.nasa.ext

import maxeem.america.common.Bool
import maxeem.america.nasa.domain.Apod
import maxeem.america.nasa.domain.MediaType

fun Apod.prepareImageUrl(hd: Bool = false) = when (mediaType) {
    MediaType.YouTube ->
        MediaType.YouTube.previewOf(url, hd)
    else ->
        hd tru { hdUrl } ?: url
}