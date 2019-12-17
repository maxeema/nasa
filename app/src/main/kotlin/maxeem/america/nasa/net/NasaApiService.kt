package maxeem.america.nasa.net

import kotlinx.coroutines.Deferred
import maxeem.america.nasa.Conf
import maxeem.america.common.Str
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface NasaApiService {

    private companion object {
        const val APOD_PATH = "planetary/apod"
    }

    @GET(APOD_PATH)
    fun getApodAsync(@Query("date") date: Str?,
                     @Query("api_key") apiKey: Str = Conf.Nasa.API_KEY
    ) : Deferred<Response<ApodDTO>>

    @GET(APOD_PATH)
    fun getApodsAsync(@Query("start_date") startDate: Str,
                      @Query("end_date") endDate: Str? = null,
                      @Query("api_key") apiKey: Str = Conf.Nasa.API_KEY
    ) : Deferred<Response<List<ApodDTO>>>

}
