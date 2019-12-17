package maxeem.america.nasa

import okhttp3.logging.HttpLoggingInterceptor
import java.text.SimpleDateFormat

object Conf {

    val dateFormatter = SimpleDateFormat("yyyy-MM-dd")

    object Log {
        val HTTP_LEVEL = HttpLoggingInterceptor.Level.BASIC
    }

    object Nasa {
        const val API_KEY = "TM31glFTVVLW8dzi10AACUc8KPH7kZCM2VclSX7s" // Insert your NASA developer API key
        const val API_URL = "https://api.nasa.gov"
        //
        const val SAVE_FILE_MASK    = "NASA %date% %name%"
        const val SAVE_FILE_MASK_HD = "NASA %date% %name% - HD"
        //
//        const val APOD_WEB_LABEL = "apod.nasa.gov/apod/"
//        const val APOD_WEB_URL   = "https://apod.nasa.gov/apod/"
    }

    object YouTube {
        const val API_KEY = "AIzaSyAjRvGrVVvHT98_ExsoB1FvwKZfgdUr7Ec" // Insert your YouTube developer API key
        //
        const val PREVIEW_URL_MASK = "https://img.youtube.com/vi/%video-id%/sddefault.jpg"
        const val PREVIEW_URL_MASK_HD = "https://img.youtube.com/vi/%video-id%/maxresdefault.jpg"
    }

}

