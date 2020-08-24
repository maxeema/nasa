package maxeem.america.nasa.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import maxeem.america.nasa.ext.onClick
import maxeem.america.nasa.misc.ImageInfo
import maxeem.america.nasa.repo.ImageRepo
import maxeem.america.nasa.ui.models.FotoViewModel

class FotoViewActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FotoViewModel.foto?.also { foto ->
            var imgSource : ImageSource? = null
            //
            var useHd = false;
            if (!foto.hd && foto.apod.hdUrl != null) {
                val hdInfo = ImageInfo.of(foto.copy(hd = true, url = foto.apod.hdUrl))
                val hdCached = ImageRepo.getCachedFile(hdInfo)
                if (hdCached.isFile && hdCached.length() > 1000) {
                    // use HD photo it if is in cache
                    useHd = true
                    imgSource = ImageSource.uri(hdCached.toUri())
//                    println("use hd img source: ${hdCached.toUri()}")
                }
            }
            if (imgSource == null) {
                imgSource = ImageSource.bitmap(foto.image.bitmap)
            }
            //
            setContentView(SubsamplingScaleImageView(this).apply {
                lifecycleScope.launchWhenStarted {
                    setImage(imgSource)
                }
                onClick(::finish)
            }.apply {
                this.maxScale = if (useHd) 6f else 3f
            })
        } ?: finish()
    }

}
