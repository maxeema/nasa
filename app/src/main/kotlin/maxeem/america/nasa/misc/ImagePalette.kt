package maxeem.america.nasa.misc

import android.os.Parcelable
import androidx.palette.graphics.Palette
import kotlinx.android.parcel.Parcelize
import maxeem.america.nasa.ext.notnil

@Parcelize
data class ImagePalette(

    val rgb: Int,
    val body: Int,
    val title: Int

): Parcelable {

    companion object {

        fun of(palette: Palette) = with(palette) {
            (listOf(mutedSwatch, vibrantSwatch, lightMutedSwatch) + swatches)
                .firstOrNull{ it.notnil() }?.let {
                    ImagePalette(
                        rgb = it.rgb,
                        body = it.bodyTextColor,
                        title = it.titleTextColor
                    )
                }
        }

    }

}