package maxeem.america.nasa.misc

import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import maxeem.america.common.Bool
import maxeem.america.nasa.ext.lg

data class Fullscreen private constructor(
    val systemUiVisibility: Int,
    val showActionbar: Bool,
    val windowAttributesFlags: Int
) {

    fun backOn(a: AppCompatActivity) = Companion.backOn(a, this)

    companion object {
        fun enterOn(a: AppCompatActivity): Fullscreen {
            lg { "fullscreen enter on"}
            with(a) {
                val view = window.decorView.findViewById<ViewGroup>(android.R.id.content)
                    ?: window.decorView
                val winParams = window.attributes

                val restore = Fullscreen(view.systemUiVisibility, supportActionBar?.isShowing ?: false, winParams.flags)

                view.systemUiVisibility =
                    View.SYSTEM_UI_FLAG_LOW_PROFILE or View.SYSTEM_UI_FLAG_IMMERSIVE or
                            View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN

                winParams.flags = winParams.flags or WindowManager.LayoutParams.FLAG_FULLSCREEN
                window.attributes = winParams

                supportActionBar?.hide()

                return restore
            }
        }

        fun backOn(a: AppCompatActivity, restore: Fullscreen) {
            lg { "fullscreen back on, $restore"}
            with(a) {
                val view = window.decorView.findViewById<ViewGroup>(android.R.id.content)
                    ?: window.decorView
                val winParams = window.attributes

                view.systemUiVisibility = restore.systemUiVisibility

                winParams.flags = restore.windowAttributesFlags
                window.attributes = winParams

                if (restore.showActionbar)
                    supportActionBar?.show()
            }
        }
    }
}