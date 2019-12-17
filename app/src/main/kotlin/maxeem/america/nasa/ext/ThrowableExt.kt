package maxeem.america.nasa.ext

import maxeem.america.common.Str
import maxeem.america.nasa.R
import maxeem.america.nasa.misc.AppException

fun Throwable.ensureApp(
    defaultMessage: Str = R.string.action_error.asString()
) = when (this) {
    is AppException -> this
    else -> AppException(
        msg = defaultMessage,
        err = this
    )
}