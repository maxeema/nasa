package maxeem.america.nasa.ui.models

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import maxeem.america.common.Str
import maxeem.america.nasa.domain.Apod
import maxeem.america.nasa.domain.Apods
import maxeem.america.nasa.misc.ImageInfo
import maxeem.america.nasa.domain.UseCases
import maxeem.america.nasa.ext.asMutable
import maxeem.america.nasa.ext.isnil
import maxeem.america.nasa.ext.lg
import maxeem.america.nasa.ext.prepareImageUrl
import maxeem.america.common.ConsumableLiveData
import maxeem.america.nasa.misc.Image
import maxeem.america.nasa.util.delayRandom

private const val FOTO_LOAD_RANDOM_INTERVAL = 1000L
private const val GALLERY_FOTO_DEFAULT_HD = false

class GalleryViewModel : RepoViewModel() {

    val dataEvent = ConsumableLiveData<Apods>()
    var data : Apods? = null
        private set

    val fotoEvent = ConsumableLiveData<Pair<Apod, Image>>()
    val fotos : Map<Apod, Image> = mutableMapOf()

    fun load(fromDate: Str, limit: Int) =
        action(
            call = { UseCases.loadGallery(fromDate, limit) },
            success = { apods ->
                data = apods
                dataEvent.set(apods)
                apods.list.filter { fotos[it].isnil() }
                    .forEach(::loadImage)
            }
        )

    private fun loadImage(apod: Apod) =
        viewModelScope.launch {
            val hd = GALLERY_FOTO_DEFAULT_HD
            runCatching {
                delayRandom(
                    FOTO_LOAD_RANDOM_INTERVAL
                )
                UseCases.loadImage(
                    ImageInfo.of(url = apod.prepareImageUrl(hd), date = apod.date,
                        hd = hd, title = apod.title, description = apod.description)
                ).run {
                    good ?: throw bad!!
                }
            }.fold(
                onSuccess = { img ->
                    fotos.asMutable()[apod] = img
                    fotoEvent.set(apod to img)
                },
                onFailure = {
                    lg { "- failed to load image ${apod.url}"}
                }
            )
        }.let { Unit }

}
