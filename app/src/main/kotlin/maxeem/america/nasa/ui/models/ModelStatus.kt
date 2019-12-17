package maxeem.america.nasa.ui.models

import maxeem.america.nasa.misc.AppException
import maxeem.america.common.Str

sealed class ModelStatus(val name: Str) {

    class  Ok<T>(val wrap: T): ModelStatus("okay")
    class  Bad(val wrap: AppException): ModelStatus("bad")
    object Busy : ModelStatus("busy")

    override fun toString(): String = name

    companion object {
        infix fun <T>of(value: T) = ModelStatus.Ok(value)
        infix fun of(exception: AppException) = Bad(exception)
    }

    val isOk get() = this is Ok<*>
    val isBad get() = this is Bad
    val isBusy get() = this === Busy

    val bad get() = (this as Bad)()

    operator fun <T> Ok<T>.invoke() = wrap
    operator fun Bad.invoke() = wrap

}
