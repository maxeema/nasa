package maxeem.america.nasa.ui.states

import androidx.compose.State
import androidx.compose.state
import androidx.compose.unaryPlus
import maxeem.america.common.Bool
import maxeem.america.nasa.misc.Foto
import maxeem.america.nasa.ui.models.FotoViewModel
import java.util.*

data class FotoState(
    val busy: State<Bool>,
    val foto: State<Foto?>,
    val hd: State<Bool>,
    val date: State<Calendar?>,
    val fullscreen: State<Bool>
)

fun FotoViewModel.createState() =
    FotoState(
        busy = +state { isExecuting },
        foto = +state { foto },
        hd = +state { hd.value!! },
        date = +state { date.value },
        fullscreen = +state { fullscreen }
    )
