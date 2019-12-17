package maxeem.america.common

class Consumable<out T>(private var data: T?) {

    fun consume() = data?.also { data = null }

    companion object {
        infix fun <T> of(t: T) = Consumable(t)
    }

}
