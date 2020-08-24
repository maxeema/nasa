package maxeem.america.nasa.ui.models

import androidx.lifecycle.MutableLiveData
import maxeem.america.common.Bool
import maxeem.america.common.ConsumableLiveData
import maxeem.america.common.Str
import maxeem.america.nasa.Conf
import maxeem.america.nasa.R
import maxeem.america.nasa.domain.UseCases
import maxeem.america.nasa.ext.*
import maxeem.america.nasa.misc.*
import java.util.*

class FotoViewModel : RepoViewModel() {

    enum class Ui {
        Clear,
        Detailed;
    }

    companion object Cached {
        var foto : Foto? = null
            private set
    }

    override fun onCleared() { super.onCleared()
        Cached.foto = null
    }

    val fotoEvent = ConsumableLiveData<Foto>()
    var foto : Foto? = null
        private set(value: Foto?) {
            field = value;
            Cached.foto = value
            value?.also { fotoEvent.set(value) }
        }

    // use hd == false by default cuz some hd photos so huge that app crashes
    val hd = MutableLiveData(false).asImmutable()
    val date = MutableLiveData<Calendar>().asImmutable()

    val uiEvent = ConsumableLiveData<Ui>()
    var ui = Ui.Clear
        private set(value) {
            field = value
            uiEvent.set(value)
        }
    fun onSwitchUi(to: Ui? = null) {
        ui = to ?: when (ui) {
            Ui.Detailed -> Ui.Clear
            Ui.Clear -> Ui.Detailed
        }
    }

    private fun loadImpl(date: Str?, hd: Bool, failure: ((AppException)->Unit)? = null, success: (()->Unit) = { }) {
        action(
            call = {
                UseCases.loadFoto(date)
            },
            success = {
                val (apod, newFoto) = it
                foto = newFoto
                apod.date.toApiCalendar()?.also {
                    this@FotoViewModel.date.asMutable().value = it
                }
                success()
            },
            failure = {
                val err = it.ensureApp()
                if (err is RepoAppException && err.isDateRangeError()) {
                    if (date == null) {
                        status.asMutable().value = null
                        loadWithDate(Calendar.getInstance().apply {
                            add(Calendar.DAY_OF_MONTH, -1)
                        })
                    } else {
                        val parsedDate = Conf.dateFormatter.parse(date)!!
                        val formattedHumanDate = Conf.dateFormatterHuman.format(parsedDate)
//                        val formattedHumanTodayDate = Conf.dateFormatterHuman.format(Calendar.getInstance().time)
//                        if (formattedHumanTodayDate == formattedHumanDate) {
//                            err.msg = R.string.no_foto_for_today.asString()
//                        } else {
                            err.msg = R.string.no_foto_for_the_date_formatted.asString(formattedHumanDate)
//                        }
                        status.asMutable().value = ModelStatus.of(err, dateIssue = true)
                    }
                } else {
                    status.asMutable().value = ModelStatus.of(err, dateIssue = false)
                }
            }
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
