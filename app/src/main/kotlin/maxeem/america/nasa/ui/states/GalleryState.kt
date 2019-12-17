package maxeem.america.nasa.ui.states

import androidx.compose.State
import androidx.compose.state
import androidx.compose.unaryPlus
import maxeem.america.nasa.domain.Apod
import maxeem.america.nasa.domain.Apods
import maxeem.america.common.Bool
import maxeem.america.nasa.misc.Image
import maxeem.america.nasa.ui.models.GalleryViewModel

data class GalleryState(
    val busy: State<Bool>,
    val data: State<Apods?>,
    val fotos: MutableMap<Apod, State<Image?>>
)

fun GalleryViewModel.createState() =
    GalleryState(
        busy = +state { isExecuting },
        data = +state { data },
        fotos = fotos.mapValues { +state { it.value } as State<Image?> }.toMutableMap()
    )
