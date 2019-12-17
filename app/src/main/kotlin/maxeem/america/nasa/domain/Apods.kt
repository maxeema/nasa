package maxeem.america.nasa.domain

import maxeem.america.common.Str

data class Apods(

    val list: List<Apod>,
    val fromDate: Str

)