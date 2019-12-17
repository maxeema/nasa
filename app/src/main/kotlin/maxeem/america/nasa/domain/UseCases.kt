package maxeem.america.nasa.domain

import maxeem.america.common.Str
import maxeem.america.nasa.misc.ImageInfo
import maxeem.america.nasa.repo.ApodRepo
import maxeem.america.nasa.repo.ImageRepo
import java.io.File

object UseCases {

    suspend fun loadFoto(date: Str?) =
        ApodRepo.loadByDate(date)

    suspend fun loadGallery(fromDate: Str, limit: Int) =
        ApodRepo.loadFromDate(fromDate, limit)

    suspend fun loadImage(info: ImageInfo) =
        ImageRepo.load(info)

    suspend fun saveImage(info: ImageInfo, toDir: File) =
        ImageRepo.save(info, toDir)

}
