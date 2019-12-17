package maxeem.america.nasa.ui.models

import androidx.lifecycle.MutableLiveData
import maxeem.america.nasa.misc.Foto
import maxeem.america.nasa.misc.ImageInfo
import maxeem.america.nasa.domain.UseCases
import maxeem.america.nasa.ext.asImmutable
import maxeem.america.nasa.ext.asMutable
import maxeem.america.common.Consumable
import java.io.File

class FotoSaveViewModel : RepoViewModel() {

    val savedEvent = MutableLiveData<Consumable<File>>().asImmutable()

    fun save(foto: Foto, toDir: File) {
        action(
            call = {
                UseCases.saveImage(
                    info = ImageInfo of foto,
                    toDir = toDir
                )
            },
            success = { file ->
                savedEvent.asMutable().value =
                    Consumable(file)
            }
        )
    }

}