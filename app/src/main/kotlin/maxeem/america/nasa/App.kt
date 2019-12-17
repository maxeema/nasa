package maxeem.america.nasa

import android.app.Application

val app = App.instance

class App : Application() {

    companion object {
        lateinit var instance: App
            private set
    }

    init {
        instance = this
    }

}