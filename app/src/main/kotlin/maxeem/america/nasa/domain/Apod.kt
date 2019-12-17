package maxeem.america.nasa.domain

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.RawValue
import maxeem.america.common.Bool
import maxeem.america.common.Str
import maxeem.america.nasa.Conf

/**
 * Astronomy Picture of the Day
 */
@Parcelize
class Apod(

    val url: Str,
    val hdUrl: Str?,
    val title: Str,
    val description: Str?,
    val date: Str,
    val mediaType: @RawValue MediaType

): Parcelable {

    override fun equals(other: Any?) = other is Apod && other.date == date
    override fun hashCode() = date.hashCode()
    override fun toString() = "APOD $date - $title"

}

sealed class MediaType {

    companion object

    @Parcelize
    object Image : @RawValue MediaType(), Parcelable

    @Parcelize
    object YouTube : @RawValue MediaType(), Parcelable {

        fun previewOf(url: Str, hd: Bool) =
            if (hd) Conf.YouTube.PREVIEW_URL_MASK_HD.replace("%video-id%", videoIdOf(url))
            else Conf.YouTube.PREVIEW_URL_MASK.replace("%video-id%", videoIdOf(url))

        fun videoIdOf(url: Str) = url.substringAfterLast("/").substringBefore("?")

    }

    class Unknown(val value: Str) : MediaType()

    val isUnknown get() = this is Unknown
    val unknown get() = this as Unknown

}