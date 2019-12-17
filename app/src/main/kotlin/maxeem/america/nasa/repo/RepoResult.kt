package maxeem.america.nasa.repo

import maxeem.america.nasa.R
import maxeem.america.nasa.misc.AppException
import maxeem.america.common.Str
import maxeem.america.nasa.ext.asString
import maxeem.america.nasa.ext.notnil
import java.io.IOException

class RepoResult<T> private constructor(val good: T?, val bad: Failure?) {

    companion object {
        infix fun <T>successOf(value: T) = RepoResult(value, null)
        infix fun <T>of(error: Failure) = RepoResult<T>(null, error)
    }

    val isGood = good.notnil()
    val isBad = !isGood

    sealed class Failure(msg: Str? = null, err: Throwable? = null) : Exception(msg, err) {
        class Api(val msg: Str, val desc: Str? = null, err: Throwable? = null) : Failure(msg, err)
        class Lang(val err: Throwable) : Failure(err = err)
        class Network(val err: IOException) : Failure(err = err)

        fun toDomain() = when(this) {
            is Api -> AppException(msg, desc, cause)
            is Lang -> AppException(R.string.response_process_error.asString(), err = err)
            is Network -> AppException(R.string.network_error.asString(), err = err)
        }
    }

}
