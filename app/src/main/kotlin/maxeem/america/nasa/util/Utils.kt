package maxeem.america.nasa.util

import android.content.Intent
import android.net.Uri
import com.google.android.youtube.player.YouTubeStandalonePlayer
import kotlinx.coroutines.delay
import maxeem.america.common.Bool
import maxeem.america.common.Str
import maxeem.america.nasa.domain.Apod
import maxeem.america.nasa.domain.MediaType
import maxeem.america.nasa.ext.ensureApp
import maxeem.america.nasa.ext.handleError
import maxeem.america.nasa.ext.lg
import maxeem.america.nasa.Conf
import maxeem.america.nasa.ui.ComposeActivity
import kotlin.random.Random

suspend
fun delayRandom(until: Long) = delay(Random.nextLong(until))

fun open(ctx: ComposeActivity, apod: Apod, hd: Bool) {
    val url = hd.takeIf { it }?.let { apod.hdUrl } ?: apod.url
    if (apod.mediaType == MediaType.YouTube) {
        YouTubeStandalonePlayer.createVideoIntent(ctx, Conf.YouTube.API_KEY,
            MediaType.YouTube.videoIdOf(url)).also {
            runCatching {
                ctx.startActivity(it)
            }.onFailure {
                it.printStackTrace()
                open(ctx, url)
            }
        }
    } else {
        open(ctx, url)
    }
}
fun open(ctx: ComposeActivity, url: Str) {
    val fullUrl = url.takeIf {
        it.startsWith("https://") || url.startsWith("http://")
    } ?: "https://$url"
    Intent(Intent.ACTION_VIEW, Uri.parse(fullUrl)).apply {
        lg { "open url: $fullUrl" }
        runCatching {
            ctx.startActivity(this)
        }.onFailure {
            ctx.handleError(it.ensureApp())
        }
    }
}
