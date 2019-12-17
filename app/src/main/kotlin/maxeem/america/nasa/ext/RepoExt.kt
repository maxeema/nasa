package maxeem.america.nasa.ext

import maxeem.america.nasa.R
import maxeem.america.nasa.repo.NasaError
import maxeem.america.nasa.repo.RepoResult
import okhttp3.ResponseBody

fun ResponseBody?.extractError(code: Int) : RepoResult.Failure =
    nil {
        RepoResult.Failure.Api(
            msg = R.string.bad_response.asString(
                code
            )
        )
    } ?: runCatching {
        this!!.string().let {
            NasaError.extract(it.trim())
        }
    }.fold(
        onSuccess = { info ->
            when (info.type) {
                NasaError.Type.OutOfRange, NasaError.Type.InternalServer -> R.string.no_foto_at_the_date.asString()
                NasaError.Type.ApiKeyMissing -> R.string.api_key_issue.asString()
                else -> R.string.bad_response.asString(code)
            }.let { msg ->
                RepoResult.Failure.Api(
                    msg = msg,
                    desc = info.text
                )
            }
        },
        onFailure = {
            RepoResult.Failure.Api(
                msg = R.string.bad_response.asString(
                    code
                )
            )
        }
    )
