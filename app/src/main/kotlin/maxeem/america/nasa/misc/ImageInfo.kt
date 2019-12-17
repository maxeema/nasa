package maxeem.america.nasa.misc

import maxeem.america.common.Bool
import maxeem.america.common.Str
import maxeem.america.nasa.Conf
import maxeem.america.nasa.ext.fals
import maxeem.america.nasa.ext.fixFileName

private const val SAVE_FILE_NAME_MAX_LENGTH = 100
private const val DEFAULT_PICTURE_EXTENSION = "jpg"
private       val SUPPORTED_PICTURE_EXTENSIONS = arrayOf("jpg", "jpeg", "png", "webp", "gif")

class ImageInfo private constructor(
    val url : Str,
    val date: Str,
    val hd: Bool,
    val title: Str,
    val description: Str?
) {
    companion object {
        fun of(url: Str, date: Str, hd: Bool, title: Str, description: Str?)
                = ImageInfo(url = url, date = date, hd = hd, title = title, description = description)
        infix fun of(foto: Foto) = of(
            url = foto.url, date = foto.apod.date,
            hd = foto.hd, title = foto.apod.title,
            description = foto.apod.description
        )
    }

    fun getSaveFileName() = getSaveFileNameImpl(simple = true)
    fun getTitledSaveFileName() = getSaveFileNameImpl(simple = false)

    private fun getSaveFileNameImpl(simple: Bool = false) : String {
        return when {
            hd -> Conf.Nasa.SAVE_FILE_MASK_HD
            else -> Conf.Nasa.SAVE_FILE_MASK
        }.let {
            val ext = url.substringAfterLast(".").let { ext ->
                SUPPORTED_PICTURE_EXTENSIONS.firstOrNull { it.equals(ext, ignoreCase = true) }
            } ?: DEFAULT_PICTURE_EXTENSION
            it.replace("%date%", date).replace("%name%", simple fals { title } ?: "" )
                .fixFileName()
                .take(SAVE_FILE_NAME_MAX_LENGTH - 1 - ext.length).trim()
                .plus(".$ext")
        }
    }


}



