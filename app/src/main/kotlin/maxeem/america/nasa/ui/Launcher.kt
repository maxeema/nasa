package maxeem.america.nasa.ui

import android.content.Intent
import android.os.Bundle
import maxeem.america.common.Str
import maxeem.america.nasa.ext.start
import maxeem.america.nasa.misc.ImagePalette

object Launcher {

    object Extras {
        const val KEY = "launch_args"
        inline fun <reified T> extract(intent: Intent) =
                intent.extras?.get(KEY)?.takeIf { it is T }?.let { it as T }
        fun forget(intent: Intent) = intent.removeExtra(KEY)
    }

    fun launchFotoActivity(ctx: ComposeActivity, date: Str, palette: ImagePalette?) {
        Bundle().apply {
            putParcelable(Extras.KEY, FotoActivity.Args(date, palette))
            ctx.start<FotoActivity>(this)
        }
    }

}