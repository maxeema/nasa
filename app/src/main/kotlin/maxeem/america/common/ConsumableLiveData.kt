package maxeem.america.common

import androidx.lifecycle.LiveData

class ConsumableLiveData <T> : LiveData<Consumable<T>>() {

    fun set(value: T) { this.value = Consumable(value)
    }
    fun post(value: T) = postValue(Consumable(value))

}
