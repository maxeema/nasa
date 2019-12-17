package maxeem.america.nasa.misc

import maxeem.america.common.Bool
import maxeem.america.common.Str
import maxeem.america.nasa.domain.Apod

data class Foto(

    val apod: Apod,
    val hd: Bool,
    val url: Str,
    val image: Image

)