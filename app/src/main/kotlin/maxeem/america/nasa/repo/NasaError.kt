package maxeem.america.nasa.repo

import maxeem.america.common.Str
import maxeem.america.nasa.ext.tru

object NasaError {

    class Info(val type: Type, val text: Str?)

    enum class Type {
        InternalServer,
        ApiKeyMissing,
        OutOfRange,
        Blank,
        Raw
    }

    fun extract(str: Str) =
        str.isBlank() tru {
            Info(Type.Blank, "")
        } ?: runCatching {
            val msgKey = listOf("msg", "message").firstOrNull { str.contains("\"$it\":") }
            str.substringAfter("\"$msgKey\":").substringAfter("\"").substringBefore("\"")
        }.fold(
            onSuccess = { msg ->
                when {
                    msg.contains("internal", ignoreCase = true) -> Info(Type.InternalServer, msg)
                    msg.contains("api_key", ignoreCase = true) -> Info(Type.ApiKeyMissing, msg)
                    msg.contains("date ", ignoreCase = true) -> Info(Type.OutOfRange, msg)
                    else -> Info(Type.Raw, str)
                }
            },
            onFailure = {
                Info(Type.Raw, str)
            }
        )

}