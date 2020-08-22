package maxeem.america.nasa.misc

import maxeem.america.common.Str
import maxeem.america.nasa.repo.NasaError
import maxeem.america.nasa.repo.RepoResult

open class AppException(

    var msg: Str,
    val desc: Str? = null,
    val err: Throwable? = null

) : Exception(msg, err)

class RepoAppException(
    msg: Str,
    desc: Str? = null,
    err: Throwable? = null,
    val failure: RepoResult.Failure
) : AppException(msg, desc, err)

fun RepoAppException.isDateRangeError() =
    failure is RepoResult.Failure.Api && failure.info?.type == NasaError.Type.OutOfRange

