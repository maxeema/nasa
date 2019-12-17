package maxeem.america.nasa.ui.models

import androidx.lifecycle.MutableLiveData
import maxeem.america.common.Bool
import maxeem.america.common.ConsumableEvent
import maxeem.america.common.ConsumableLiveData
import maxeem.america.common.Str
import maxeem.america.nasa.R
import maxeem.america.nasa.domain.UseCases
import maxeem.america.nasa.ext.*
import maxeem.america.nasa.misc.AppException
import maxeem.america.nasa.misc.Foto
import maxeem.america.nasa.misc.ImageInfo
import java.util.*

class FotoViewModel : RepoViewModel() {

    companion object Cached {
        var foto : Foto? = null
            private set
    }

    override fun onCleared() { super.onCleared()
        Cached.foto = null
    }

    val fotoEvent = ConsumableLiveData<Foto>()
    var foto : Foto?
        get() = Cached.foto
        private set(value: Foto?) {
            Cached.foto = value
        }

    val hd = MutableLiveData(true).asImmutable()
    val date = MutableLiveData<Calendar>().asImmutable()

    val fullscreenEvent = ConsumableEvent().asImmutable()
    var fullscreen : Bool = false
        private set

    fun enterFullscreen() = enterFullscreen(true)
    fun exitFullscreen() = enterFullscreen(false)
    private fun enterFullscreen(value: Bool) {
        fullscreen = value
        fullscreenEvent.asConsumable().setValue(value)
    }

    private fun loadImpl(date: Str?, hd: Bool, failure: ((AppException)->Unit)? = null, success: (()->Unit) = { }) {
        action(
            call = { UseCases.loadFoto(date) },
            success = {
                val (apod, newFoto) = it
                foto = newFoto
                fotoEvent.set(newFoto)
                apod.date.toApiCalendar()?.also {
                    this@FotoViewModel.date.asMutable().value = it
                }
                success()
            },
            failure = failure
        ) { apod ->
            apod.mediaType.isUnknown tru {
                throw AppException(R.string.unsupported_media.asString(apod.mediaType.unknown.value))
            }
            val url = apod.prepareImageUrl(hd)
            val info = ImageInfo.of(url, apod.date, hd, apod.title, apod.description)
            val img = UseCases.loadImage(info).run {
                good ?: throw bad!!.toDomain()
            }
            apod to Foto(apod, hd, url, img)
        }
    }

    fun load() = loadImpl(date.value?.toApiDate(), hd.value!!)

    fun loadWithDate(value: Calendar) {
        val backup = date.value
        date.asMutable().value = value
        loadImpl(
            date = value.toApiDate(),
            hd = hd.value!!,
            failure = {
                date.asMutable().value = backup
            }
        )
    }

    fun loadWithHD(value: Bool) {
        val backup = hd.value
        hd.asMutable().value = value
        loadImpl(
            date = date.value?.toApiDate(),
            hd = value,
            failure = {
                hd.asMutable().value = backup
            }
        )
    }

    fun loadNext() = loadWithOffset(1)
    fun loadPrev() = loadWithOffset(-1)

    private fun loadWithOffset(offset: Int) {
        (date.value?.clone() as Calendar? ?: Calendar.getInstance().asYearMonthDay()).apply {
            add(Calendar.DAY_OF_MONTH, offset)
        }.also {
            loadWithDate(it)
        }
    }

}
