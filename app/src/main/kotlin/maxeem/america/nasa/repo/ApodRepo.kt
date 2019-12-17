package maxeem.america.nasa.repo

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import maxeem.america.nasa.domain.Apod
import maxeem.america.nasa.domain.Apods
import maxeem.america.nasa.domain.MediaType
import maxeem.america.common.Str
import maxeem.america.nasa.ext.lg
import maxeem.america.nasa.net.NasaApi
import maxeem.america.nasa.net.toDomain

object ApodRepo : NasaRepo() {

    private val cache : MutableMap<Str, Apod> = mutableMapOf()

    suspend fun loadByDate(date: Str?) : RepoResult<Apod> =
        cache[date]?.let {
            lg { "- use cached $date - $it" }
            RepoResult successOf it
        } ?: process(
            call = NasaApi.service.getApodAsync(date)::await,
            process = {
                it.toDomain().also { apod ->
                    cache[apod.date] = apod
                }
            }
        )

    suspend fun loadFromDate(fromDate: Str, limit: Int) : RepoResult<Apods> =
        process(
            call = NasaApi.service.getApodsAsync(startDate = fromDate)::await,
            process = { data ->
                withContext(Dispatchers.Default) {
                    lg { "- data size: ${data.size}" }
                    data.toDomain()
                        .asSequence()
                        .filterNot { it.mediaType is MediaType.Unknown }
                        .distinct()
                        .sortedByDescending { it.date }
                        .take(limit)
                        .toList().let { list ->
                            Apods(list, fromDate).also { apods ->
                                cache.putAll(apods.list.map { it.date to it } )
                            }
                        }
                }
            }
        )

}