//package maxeem.america.nasa.ui
//
//import android.os.Bundle
//import androidx.activity.viewModels
//import androidx.compose.Composable
//import androidx.compose.State
//import androidx.compose.state
//import androidx.compose.unaryPlus
//import androidx.lifecycle.lifecycleScope
//import androidx.lifecycle.observe
//import androidx.ui.animation.Crossfade
//import androidx.ui.core.*
//import androidx.ui.foundation.Clickable
//import androidx.ui.foundation.DrawImage
//import androidx.ui.foundation.VerticalScroller
//import androidx.ui.graphics.Color
//import androidx.ui.layout.*
//import androidx.ui.material.Button
//import androidx.ui.material.Divider
//import androidx.ui.material.ripple.Ripple
//import androidx.ui.material.surface.Surface
//import androidx.ui.text.TextStyle
//import maxeem.america.nasa.R
//import maxeem.america.nasa.type.Bool
//import maxeem.america.nasa.domain.Apod
//import maxeem.america.nasa.ext.*
//import maxeem.america.nasa.misc.Fullscreen
//import maxeem.america.nasa.misc.Image
//import maxeem.america.nasa.ui.models.GalleryViewModel
//import maxeem.america.nasa.ui.states.GalleryState
//import maxeem.america.nasa.ui.states.createState
//import maxeem.america.nasa.util.asYearMonthDay
//import maxeem.america.nasa.util.toApiDate
//import java.util.*
//
//private const val ItemsSize  = 10
//private const val ItemHeight = 200
//
//class GalleryActivity : ComposeActivity() {
//
//    private val galleryModel by viewModels<GalleryViewModel>()
//    private lateinit var state : GalleryState
//    private lateinit var busyTik : State<Int>
//    private var autoLoad: Bool = true
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContent {
//            state = galleryModel.createState()
//            busyTik = +state { 0 }
//            AppTheme {
//                Content()
//            }
//        }
//        autoLoad = state.data.value.isnil()
//        lifecycleScope.launchWhenCreated {
//            if (state.busy.value.not() && state.data.value.isnil()) {
//                load()
//            }
//        }
//        state.data.value?.also {
//            Fullscreen.enterOn(this)
//        }
//        galleryModel.executingTickerEvent.observe(this) {
//            it.consume()?.also {
//                busyTik.value = it
//            }
//        }
//        galleryModel.statusEvent.observe(this) {
//            it.consume()?.also { status ->
//                lg { "status $status" }
//                state.busy.value = status.isBusy
//                status.isBad.tru {
//                    handleError(status.bad)
//                }
//                autoLoad = false
//            }
//        }
//        galleryModel.dataEvent.observe(this) {
//            state.data.value = it.consume() ?: return@observe
//            Fullscreen.enterOn(this)
//        }
//        galleryModel.fotoEvent.observe(this) {
//            val (apod, foto) = it.consume() ?: return@observe
//            state.fotos[apod]?.value = foto
//        }
//    }
//
//    @Composable
//    private fun Content() {
//        if (state.data.value.isnil()) {
//            val tik = busyTik.value
//            Crossfade(state.busy.value || autoLoad) {
//                when (it) {
//                    true ->
//                        Align(alignment = Alignment.Center) {
//                            Opacity(opacity = 0.8F) {
//                                Text(R.string.loading.asString().toTikString(tik),
//                                    style = TextStyle(
//                                        color = colors.onBackground
//                                    )
//                                )
//                            }
//                        }
//                    false ->
//                        Align(alignment = Alignment.Center) {
//                            Row(
//                                modifier = ExpandedWidth,
//                                arrangement = Arrangement.SpaceEvenly
//                            ) {
//                                Button(R.string.load.asString(), onClick = ::load)
//                            }
//                        }
//                }
//            }
//        } else {
//            VerticalScroller {
//                Column {
//                    state.data.value?.list?.also { list ->
//                        list.forEachIndexed { idx, apod ->
//                            if (state.fotos[apod].isnil())
//                                state.fotos[apod] = +state { null as Image? }
//                            Ripple(true) {
//                                FotoItem(apod)
//                            }
//                            if (idx < list.lastIndex)
//                                Divider(Height(1.dp), color = Color.DarkGray)
//                        }
//                    }
//                }
//            }
//        }
//    }
//
//    @Composable
//    private fun FotoItem(apod: Apod) {
//        Clickable(
//            onClick = {
//                Launcher.launchFotoActivity(
//                    ctx = this,
//                    date = apod.date,
//                    fullscreen = true,
//                    palette = state.fotos[apod]?.value?.palette
//                )
//            }
//        ) {
//            Container(modifier = Height(ItemHeight.dp) wraps ExpandedWidth) {
//                Stack {
//                    val img = state.fotos[apod]?.value
//                    img?.also {
//                        DrawImage(it)
//                        lg { "draw image: ${apod.url}" }
//                    }
//                    val titleBg = img?.palette?.rgb?.let { Color(it).copy(alpha = 55F) }
//                                        ?: Color.Black.copy(alpha = 155F)
//                    val titleColor = img?.palette?.title?.let { Color(it) }
//                                        ?: Color.White.copy(alpha = 55F)
//                    Align(alignment = Alignment.BottomLeft) {
//                        Surface(color = titleBg) {
//                            Padding(padding = EdgeInsets(top = 4.dp, bottom = 4.dp, left = 8.dp, right = 8.dp)) {
//                                FlexRow {
//                                    inflexible {
//                                        val textStyle = typography.caption
//                                            .copy(color = titleColor)
//                                        Text(apod.date, style = textStyle)
//                                        WidthSpacer(8.dp)
//                                        Text(apod.title, style = textStyle)
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//        }
//    }
//
//    private fun load() {
//        state.busy.value = true
//        Calendar.getInstance().asYearMonthDay().apply {
//            add(Calendar.DAY_OF_MONTH, -ItemsSize)
//            galleryModel.load(toApiDate(), ItemsSize)
//        }
//    }
//
//}
