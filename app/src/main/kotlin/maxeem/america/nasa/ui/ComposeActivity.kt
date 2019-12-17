package maxeem.america.nasa.ui

import androidx.appcompat.app.AppCompatActivity
import androidx.compose.Composable
import androidx.compose.unaryPlus
import androidx.ui.graphics.Color
import androidx.ui.material.ColorPalette
import androidx.ui.material.MaterialTheme
import maxeem.america.nasa.R
import maxeem.america.nasa.ext.asColor

abstract class ComposeActivity : AppCompatActivity() {

    val colors get() = +MaterialTheme.colors()
    val typography get() = +MaterialTheme.typography()

    @Composable
    fun AppTheme(children: @Composable() () -> Unit) {
        MaterialTheme(
            colors = ColorPalette(
                background = Color(R.color.window_background.asColor()),
                onBackground = Color(R.color.onBackground.asColor())
            ),
            children = children
        )
    }

}