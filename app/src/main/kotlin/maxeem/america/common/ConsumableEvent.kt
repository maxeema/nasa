package maxeem.america.common

import androidx.lifecycle.MutableLiveData

class ConsumableEvent : MutableLiveData<Consumable<Boolean>>() {
    
    fun setValue(newValue: Boolean) {
        super.setValue(Consumable(newValue))
    }
    
}
