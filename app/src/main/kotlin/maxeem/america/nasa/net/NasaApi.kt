package maxeem.america.nasa.net

import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import maxeem.america.nasa.Conf
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object NasaApi {

    val service by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
        retrofit.create(NasaApiService::class.java)
    }

    private val retrofit = Retrofit.Builder().apply {
        baseUrl(Conf.Nasa.API_URL)
        addCallAdapterFactory(CoroutineCallAdapterFactory())
        addConverterFactory(
            MoshiConverterFactory.create(Moshi.Builder().add(KotlinJsonAdapterFactory()).build())
        )
        client(OkHttpClient.Builder().apply {
//            addInterceptor(MockInterceptor())
            if (Conf.Log.HTTP_LEVEL > HttpLoggingInterceptor.Level.NONE) {
                addInterceptor(HttpLoggingInterceptor().apply {
                    level = Conf.Log.HTTP_LEVEL
                })
            }
        }.build())
    }.build()

}

//class MockInterceptor : Interceptor {
//
//    override fun intercept(chain: Interceptor.Chain): Response {
//println("intercept: $chain")
//        val responseString = "{\"code\":404,\"msg\":\"No foto for date: today.\",\"service_version\":\"v1\"}"
//        return chain.proceed(chain.request())
//            .newBuilder()
//            .code(404)
//            .protocol(Protocol.HTTP_2)
//            .message(responseString)
//            .body(
//                ResponseBody.create(
//                    "application/json".toMediaType(),
//                    responseString.toByteArray()))
//            .addHeader("content-type", "application/json")
//            .build()
//    }
//}