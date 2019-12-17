package maxeem.america.nasa.repo

import android.graphics.BitmapFactory
import android.media.MediaScannerConnection
import androidx.palette.graphics.Palette
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import maxeem.america.common.Bool
import maxeem.america.nasa.R
import maxeem.america.nasa.app
import maxeem.america.nasa.ext.asString
import maxeem.america.nasa.ext.lg
import maxeem.america.nasa.misc.Image
import maxeem.america.nasa.misc.ImageInfo
import maxeem.america.nasa.misc.ImagePalette
import maxeem.america.nasa.repo.RepoResult.Failure
import java.io.ByteArrayOutputStream
import java.io.File
import java.net.URL

object ImageRepo {

    fun getCachedFile(info: ImageInfo) = File(app.cacheDir, info.getSaveFileName())

    suspend fun save(info: ImageInfo, toDir: File, retry: Bool = false) : RepoResult<File> =
        withContext(Dispatchers.IO) {
            runCatching {
                val cachedFile = getCachedFile(info)
                if (!cachedFile.exists())
                    load(info)
                runCatching {
                    // try saving with title "NASA 2019-12-15 Cool Mars Pic of the Day - HD.jpg"
                    File(toDir, info.getTitledSaveFileName()).apply {
                        lg { "save $cachedFile to $this" }
                        cachedFile.copyTo(this, overwrite = true)
                    }
                }.recover { it.printStackTrace()
                    // on saving with title failure, try simple "NASA 2019-12-15 - HD.jpg"
                    File(toDir, info.getSaveFileName()).apply {
                        lg { "save simple $cachedFile to $this" }
                        cachedFile.copyTo(this, overwrite = true)
                    }
                }.getOrThrow()
            }.fold(
                onSuccess = { file ->
                    MediaScannerConnection.scanFile(app, arrayOf(file.absolutePath), null, null)
                    RepoResult successOf file
                },
                onFailure = { RepoResult of Failure.Api(R.string.save_foto_error.asString(), err = it) }
            )
        }

    suspend fun load(info: ImageInfo) : RepoResult<Image> =
        withContext(Dispatchers.IO) {
            runCatching {
                loadImpl(info)
            }.fold(
                onSuccess = { RepoResult successOf it },
                onFailure = { RepoResult of Failure.Api(R.string.load_foto_error.asString(), err = it) }
            )
        }

    private fun loadImpl(info: ImageInfo) : Image {
        val url = URL(info.url)
        val cachedFile = getCachedFile(info)
        lg { "load $url" }
        val bitmap = if (cachedFile.isFile && cachedFile.length() > 1) {
            cachedFile.inputStream().buffered().use {
                lg { "- use cached, ${cachedFile.length()} - $cachedFile" }
                BitmapFactory.decodeStream(it)
            }
        } else {
            lg { "- load remote" }
            val raw = url.openStream().buffered().use { urlIn ->
                val bout = ByteArrayOutputStream()
                urlIn.copyTo(bout)
                bout.toByteArray()
            }
            runCatching {
                lg { "- save to cache, ${raw.size} - $cachedFile" }
                cachedFile.outputStream().buffered().use { cachedOut ->
                    cachedOut.write(raw)
                }
            }.onFailure {
                lg { "- saving to cache failed, continue without cache" }
            }
            BitmapFactory.decodeByteArray(raw, 0, raw.size)
        }
        Palette.from(bitmap).generate().let {
            ImagePalette.of(it)
        }.let {
            return Image(bitmap, it)
        }
    }

}
