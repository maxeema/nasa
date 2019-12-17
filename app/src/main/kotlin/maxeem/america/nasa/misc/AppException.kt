package maxeem.america.nasa.misc

import maxeem.america.common.Str

class AppException(

    val msg: Str,
    val desc: Str? = null,
    val err: Throwable? = null

) : Exception(msg, err)
