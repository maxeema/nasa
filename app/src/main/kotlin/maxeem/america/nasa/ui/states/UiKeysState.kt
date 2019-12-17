package maxeem.america.nasa.ui.states

import androidx.compose.State
import androidx.compose.state
import androidx.compose.unaryPlus
import androidx.lifecycle.LifecycleCoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val INITIAL_DELAY = 50L
private const val FINAL_DELAY = 1000L
private const val DELAY_STEP = 200L

class UiKeysState <K, V>(private vararg val keys: K) : Iterable<Map.Entry<K, State<V?>>> {

    private val map = keys.map { it to +state { null as V? } }.toTypedArray().let { mapOf(*it) }

    override fun iterator() = map.iterator()
    operator fun get(key: K): State<V?>? = map[key]

    private var job: Job? = null

    fun start(scope: LifecycleCoroutineScope, delayStep: Long = DELAY_STEP,
                                              initial: V, initialDelay: Long = INITIAL_DELAY,
                                                final: V, finalDelay: Long = FINAL_DELAY) {
        reset()
        job = scope.launchWhenResumed {
            for (item in map { it.value }.withIndex()) {
                val (idx, state) = item
                launch {
                    delay(initialDelay + idx * delayStep)
                    state.value = initial
                }.invokeOnCompletion {
                    launch {
                        delay(finalDelay + idx * delayStep)
                        state.value = final
                    }
                }
            }
        }
    }

    fun reset() {
        job?.cancel()
        map { it.value }.forEach { it.value = null }
    }

}