package maxeem.america.nasa.repo

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import maxeem.america.nasa.ext.extractError
import maxeem.america.nasa.ext.tru
import maxeem.america.nasa.repo.RepoResult.Failure
import retrofit2.Response
import java.io.IOException

abstract class NasaRepo {

    suspend fun <D, R> process(call: suspend ()-> Response<R>,
                               process: suspend (data: R) -> D
    ) = withContext(Dispatchers.IO) {
        runCatching {
            query(call) {
                onSuccess { data ->
                    RepoResult successOf process(data)
                } ?: RepoResult.of(errorBody().extractError(code()))
            }
        }.fold(
            onSuccess = { it },
            onFailure = { err ->
                when (err) {
                    is IOException -> RepoResult of Failure.Network(err)
                    else -> RepoResult of Failure.Lang(err)
                }
            }
        )
    }

    private suspend
    fun <D, V> Response<D>.onSuccess(execute: suspend Response<D>.(body: D)-> V)
            = isSuccessful.tru { execute(body()!!) }

    private suspend
    fun <D, V> NasaRepo.query(call: suspend()->Response<D>, execute: suspend Response<D>.()->RepoResult<V>)
            = call().execute()

}

