package maxeem.america.nasa.ui.models

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import maxeem.america.common.Consumable
import maxeem.america.common.ConsumableLiveData
import maxeem.america.nasa.ext.*
import maxeem.america.nasa.misc.AppException
import maxeem.america.nasa.misc.RepoAppException
import maxeem.america.nasa.repo.RepoResult
import kotlin.time.measureTime

private const val MIN_ACTION_DURATION = 500L
private const val LOAD_TIK_INTERVAL = 200L

abstract class RepoViewModel : ViewModel() {

    val status = MutableLiveData<ModelStatus>().asImmutable()
    val statusEvent = status.map { Consumable(it) }
    val executingTickerEvent = ConsumableLiveData<Int>()
    val isExecuting get() = status.value == ModelStatus.Busy

    protected fun <V, T> action(call: suspend ()->RepoResult<V>,
                                success: suspend (T)->Unit,
                                failure: (Throwable)->Unit = {
                                    val err = it.ensureApp()
                                    status.asMutable().value = ModelStatus of err
                                },
                                process: suspend CoroutineScope.(V)->T = { it as T}) {
        if (isExecuting) return
        status.asMutable().value = ModelStatus.Busy
        viewModelScope.launch {
            launch { var i = 0
                while (isExecuting) {
                    executingTickerEvent.set(i++)
                    delay(LOAD_TIK_INTERVAL)
                }
            }
            val result: Result<T>
            measureTime {
                runCatching {
                    withContext(Dispatchers.Default) {
                        query(call) {
                            onSuccess {
                                process(good!!)
                            } ?: throw bad!!.toDomain()
                        }
                    }
                }.let {
                    result = it
                }
            }.apply {
                lg { "- execution time: millis: ${toLongMilliseconds()}, $this" }
                if (toLongMilliseconds() < MIN_ACTION_DURATION)
                    delay(MIN_ACTION_DURATION - toLongMilliseconds())
            }
            result.fold(
                onSuccess = {
                    status.asMutable().value = ModelStatus of it
                    success(it)
                },
                onFailure = failure
            )
        }
    }
    protected fun <T> action(call: suspend ()->RepoResult<T>, success: suspend (T)->Unit) {
        action<T, T>(call, success)
    }

    private suspend
    fun <R, T> query(call: suspend()->RepoResult<R>, execute: suspend RepoResult<R>.()-> T) = call().execute()
    private suspend
    fun <R, T> RepoResult<R>.onSuccess(execute: suspend RepoResult<R>.(data: R)-> T) = isGood tru { execute(good!!) }

}
