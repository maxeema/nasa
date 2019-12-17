package maxeem.america.nasa.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import maxeem.america.nasa.ext.onClick
import maxeem.america.nasa.ui.models.FotoViewModel

class FotoViewActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FotoViewModel.foto?.also { foto ->
            setContentView(SubsamplingScaleImageView(this).apply {
                lifecycleScope.launchWhenStarted {
                    setImage(ImageSource.bitmap(foto.image.bitmap))
                }
                onClick(::finish)
            })
        } ?: finish()
    }

}
