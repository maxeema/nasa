package maxeem.america.nasa.ui.models

import maxeem.america.common.Bool
import maxeem.america.common.Str
import maxeem.america.nasa.misc.AppException

sealed class ModelStatus(val name: Str) {

    class  Ok<T>(val wrap: T): ModelStatus("okay")
    class  Bad(val wrap: AppException, dateIssue: Bool = false): ModelStatus("bad")
    object Busy : ModelStatus("busy")

    override fun toString(): String = name

    companion object {
        infix fun <T>of(value: T) = ModelStatus.Ok(value)
        fun of(exception: AppException, dateIssue: Bool) = Bad(exception, dateIssue)
    }

    val isOk get() = this is Ok<*>
    val isBad get() = this is Bad
    val isBusy get() = this === Busy

    val bad get() = (this as Bad)()

    operator fun <T> Ok<T>.invoke() = wrap
    operator fun Bad.invoke() = wrap

}
