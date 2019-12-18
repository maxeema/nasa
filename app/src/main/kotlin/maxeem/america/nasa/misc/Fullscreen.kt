package maxeem.america.nasa.misc

import android.app.AlertDialog
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import maxeem.america.nasa.ext.lg

data class Fullscreen private constructor(
    val systemUiVisibility: Int,
    val windowAttributesFlags: Int
) {

    fun backOn(window: Window) = Companion.backOn(window, this)

    companion object {
        fun enterOn(a: AppCompatActivity) = a.window?.let { w -> enterOn(w) }
        fun enterOn(a: AlertDialog) = a.window?.let { w -> enterOn(w) }
        fun enterOn(window: Window): Fullscreen {
            lg { "fullscreen enter on"}
                val view = window.decorView.findViewById<ViewGroup>(android.R.id.content)
                    ?: window.decorView
                val winParams = window.attributes

                val restore = Fullscreen(view.systemUiVisibility, winParams.flags)

                view.systemUiVisibility =
                    View.SYSTEM_UI_FLAG_LOW_PROFILE or View.SYSTEM_UI_FLAG_IMMERSIVE or
                            View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN

                winParams.flags = winParams.flags or WindowManager.LayoutParams.FLAG_FULLSCREEN
                window.attributes = winParams

                return restore
        }

        fun backOn(window: Window, restore: Fullscreen) {
            lg { "fullscreen back on, $restore"}
            val view = window.decorView.findViewById<ViewGroup>(android.R.id.content)
                ?: window.decorView
            val winParams = window.attributes

            view.systemUiVisibility = restore.systemUiVisibility

            winParams.flags = restore.windowAttributesFlags
            window.attributes = winParams
        }
    }
}