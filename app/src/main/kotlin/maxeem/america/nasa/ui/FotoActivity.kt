package maxeem.america.nasa.ui

import android.os.Bundle
import android.os.Environment
import android.os.Parcelable
import android.view.KeyEvent
import androidx.activity.viewModels
import androidx.compose.Composable
import androidx.compose.State
import androidx.compose.state
import androidx.compose.unaryPlus
import androidx.core.app.ShareCompat
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.observe
import androidx.ui.animation.Crossfade
import androidx.ui.core.*
import androidx.ui.foundation.Clickable
import androidx.ui.foundation.DrawImage
import androidx.ui.graphics.Color
import androidx.ui.graphics.Color.Companion.Black
import androidx.ui.graphics.Color.Companion.Gray
import androidx.ui.graphics.Color.Companion.LightGray
import androidx.ui.graphics.Color.Companion.Transparent
import androidx.ui.graphics.Color.Companion.White
import androidx.ui.graphics.toArgb
import androidx.ui.graphics.vector.DrawVector
import androidx.ui.layout.*
import androidx.ui.material.Button
import androidx.ui.material.ContainedButtonStyle
import androidx.ui.material.OutlinedButtonStyle
import androidx.ui.material.TextButtonStyle
import androidx.ui.material.ripple.Ripple
import androidx.ui.material.surface.Surface
import androidx.ui.res.vectorResource
import androidx.ui.text.font.FontWeight
import androidx.ui.text.style.TextOverflow
import kotlinx.android.parcel.Parcelize
import kotlinx.coroutines.delay
import maxeem.america.common.Bool
import maxeem.america.common.Consumable
import maxeem.america.common.Str
import maxeem.america.nasa.R
import maxeem.america.nasa.domain.MediaType
import maxeem.america.nasa.ext.*
import maxeem.america.nasa.misc.Fullscreen
import maxeem.america.nasa.misc.ImagePalette
import maxeem.america.nasa.ui.models.FotoSaveViewModel
import maxeem.america.nasa.ui.models.FotoViewModel
import maxeem.america.nasa.ui.models.ModelStatus
import maxeem.america.nasa.ui.states.FotoState
import maxeem.america.nasa.ui.states.UiKeysState
import maxeem.america.nasa.ui.states.createState
import maxeem.america.nasa.util.open
import permissions.dispatcher.NeedsPermission
import permissions.dispatcher.RuntimePermissions

@RuntimePermissions
class FotoActivity : ComposeActivity() {

    @Parcelize
    data class Args(
        val date: Str, val fullscreen: Bool, val palette: ImagePalette?
    ) : Parcelable

    private enum class UiKeys { fullscreen, prev, next }
    private enum class UiOverflowKeys { overflow }

    private val fotoModel: FotoViewModel by viewModels()
    private val saveModel: FotoSaveViewModel by viewModels()

    private lateinit var state: FotoState
    private lateinit var busyTik: State<Int>
    private lateinit var uiKeysState: UiKeysState<UiKeys, Bool>
    private lateinit var uiOverflowState: UiKeysState<UiOverflowKeys, Bool>
    private var fullscreen: Fullscreen? = null
    private var autoLoad: Bool = true
    private var args: Args? = null
    private val actionsOnFotoLoad : MutableMap<Any, ()->Unit> = mutableMapOf()
    private var hasUserAction = false

    private val palette get() = state.foto.value?.image?.palette ?: args?.palette

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        args = Launcher.Extras.extract<Args>(intent)?.also {
            actionsOnFotoLoad[Launcher] = {
                Launcher.Extras.forget(intent)
            }
        }
        lg { "onCreate, args: $args" }

        setContent {
            busyTik = +state { 0 }
            state = fotoModel.createState()
            uiKeysState = UiKeysState(*UiKeys.values())
            uiOverflowState = UiKeysState(*UiOverflowKeys.values())
            AppTheme {
                Content()
            }
        }

        state.busy.value = fotoModel.isExecuting || saveModel.isExecuting
        autoLoad = state.foto.value.isnil()

        lifecycleScope.launchWhenCreated {
            if (state.busy.value.not() && state.foto.value.isnil()) {
                args?.date?.toApiCalendar()?.also {
                    fotoModel.loadWithDate(it)
                } ?: fotoModel.load()
            }
        }

        fotoModel.fotoEvent.observe(this) {
            val foto = it.consume() ?: return@observe
            lg { "foto event $foto"}
            state.foto.value = foto
            actionsOnFotoLoad.takeIf { it.isNotEmpty() }?.also {
                it.values.forEach { it.invoke() }
                it.clear()
            }
            invalidateOptionsMenu()
            tryAutoFullscreen(resetUserAction = true)
        }

        fotoModel.date.observe(this) { state.date.value = it }
        fotoModel.hd.observe(this) { state.hd.value = it }

        val stateListener : (Consumable<ModelStatus>)->Unit = code@  {
            val status = it.consume() ?: return@code
            lg { "status $status" }
            state.busy.value = status.isBusy
            status.isBad.tru {
                handleError(status.bad)
            }
            invalidateOptionsMenu()
            autoLoad = false
        }
        fotoModel.statusEvent.observe(this, stateListener)
        saveModel.statusEvent.observe(this, stateListener)

        fotoModel.fullscreenEvent.observe(this) {
            val value = it.consume() ?: return@observe
            if (value && fullscreen.notnil())
                return@observe
            state.fullscreen.value = value
            when (value) {
                true -> {
                    fullscreen = Fullscreen.enterOn(this)
                    val uiKeysStateAction = { uiKeysState.start(lifecycleScope, initial = true, final = false) }
                    fotoModel.foto nil {
                        actionsOnFotoLoad[uiKeysState] = uiKeysStateAction
                    } ?: uiKeysStateAction()
                }
                else -> {
                    actionsOnFotoLoad.remove(UiKeys.fullscreen)
                    fullscreen?.backOn(this)?.also {
                        fullscreen = null
                    }
                }
            }
        }

        val tikListener = code@ { it: Consumable<Int> ->
            busyTik.value = it.consume() ?: return@code
        }
        fotoModel.executingTickerEvent.observe(this, tikListener)
        saveModel.executingTickerEvent.observe(this, tikListener)

        saveModel.savedEvent.observe(this) {
            it.consume()?.also { path ->
                showMsg(R.string.saved.asString(), path.canonicalPath)
            }
        }

        (args?.fullscreen ?: state.fullscreen.value) tru {
            fotoModel.enterFullscreen()
        }
    }

    @Composable
    fun Content() {
        Bg()
        FlexColumn {
            expanded(1F) {
                Foto()
            }
            inflexible {
                Bottom()
            }
        }
        Top()
        Status()
        Controls()
    }

    @Composable
    fun Top() {
        val overflow = uiOverflowState[UiOverflowKeys.overflow]?.value ?: false
        if (state.fullscreen.value.not() && (state.foto.value.notnil() || overflow)) {
            Container(alignment = Alignment.TopLeft) {
                Surface(elevation = 2.dp, color = colors.primary) {
                    Clickable(onClick = { /* consume clicks */}) {
                        Padding(
                            padding = EdgeInsets(left = 16.dp, right = 8.dp, top = 10.dp, bottom = 10.dp)
                        ) {
                            TopAppbar()
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun TopAppbar() {
        Crossfade(uiOverflowState[UiOverflowKeys.overflow]?.value?.not() ?: true) {
            when (it) {
                true ->
                    FlexRow(
                        crossAxisAlignment = CrossAxisAlignment.Center
                    ) {
                        expanded(1F) {
                            Text(text = fotoModel.foto!!.apod.title,
                                maxLines = 2,
                                overflow = TextOverflow.Fade,
                                style = typography.h6
                            )
                        }
                        inflexible {
                            Action(R.drawable.ic_fullscreen, true)
                            OverflowAction()
                        }
                    }
                else ->
                    FlexRow(
                        crossAxisAlignment = CrossAxisAlignment.Center
                    ) {
                        expanded(1F) {
                            Text(text = " ",
                                style = typography.h6.copy(fontWeight = FontWeight.W200)
                            )
                        }
                        inflexible {
                            Row {
                                listOf(
                                    R.drawable.ic_share,
                                    R.drawable.ic_sd_storage,
                                    R.drawable.ic_photo,
                                    R.drawable.ic_details
                                ).forEach { id ->
                                    Action(id, (id == R.drawable.ic_sd_storage) tru { saveModel.isExecuting.not() } ?: true)
                                }
                            }
                        }
                    }

            }
        }
    }
    @Composable
    fun Action(id: Int, enabled: Bool) {
        Ripple(true, enabled = enabled) {
            Clickable(onClick = enabled tru { { processAction(id) } } ) {
                Padding(top = 10.dp, bottom = 10.dp) {
                    Container(modifier = androidx.ui.layout.Size(50.dp, 20.dp)) {
                        DrawVector(
                            +vectorResource(id),
                            tintColor = White.let { enabled tru { it } ?: it.copy(alpha = 77F) }
                        )
                    }
                }
            }
        }
    }
    @Composable
    fun OverflowAction() {
        Ripple(true) {
            Clickable(onClick = { processAction(R.drawable.ic_more_horiz) } ) {
                Padding(top = 10.dp, bottom = 10.dp) {
                    Container(modifier = androidx.ui.layout.Size(32.dp, 20.dp)) {
                        DrawVector(
                            +vectorResource(R.drawable.ic_more_horiz),
                            tintColor = White
                        )
                    }
                }
            }
        }
    }

    @Composable
    fun Bg() {
        Surface(color = Color(palette?.rgb ?: colors.background.toArgb() )) { }
        Clickable(
            onClick = {
                fotoModel.foto?.also {
                    processAction(R.drawable.ic_photo)
                }
            }
        ) {
            Container(modifier = Expanded) { }
        }
    }

    @Composable
    fun Foto() {
        Stack(modifier = Expanded) {
            state.foto.value?.image?.also {
                DrawImage(image = it)
            }
        }
    }

    @Composable
    fun Status() {
        val busy = state.busy.value || autoLoad
        val foto = state.foto.value
        (busy || foto.isnil()) tru {
            when (busy) {
                true -> Center {
                    Crossfade(busy) {
                        Opacity(opacity = 0.8F) {
                            Progress()
                        }
                    }
                }
                else -> foto.isnil() tru {
                    Center {
                        Opacity(opacity = 0.8F) {
                            Button(
                                text = R.string.load.asString(),
                                onClick = fotoModel::load
                            )
                        }
                    }

                }
            }
        }
    }

    @Composable
    fun Progress() {
        val noFoto = fotoModel.foto.isnil()
        Surface(
            color = if (noFoto) Transparent else Black.copy(155F),
            elevation = 0.dp,
            modifier = Spacing(
                top = 10.dp, bottom = 10.dp,
                left = 16.dp, right = 16.dp
            )
        ) {
            FlexRow(
                modifier = ExpandedWidth wraps Spacing(20.dp),
                mainAxisAlignment = MainAxisAlignment.Center,
                crossAxisAlignment = CrossAxisAlignment.Center
            ) {
                flexible(1F) {
                    Text(
                        text = when {
                            fotoModel.isExecuting ->
                                R.string.loading.asString().toTikString(busyTik.value)
                            saveModel.isExecuting ->
                                R.string.saving.asString().toTikString(busyTik.value)
                            else -> " "
                        },
                        style = typography.subtitle1
                            .copy(
                                color = noFoto tru { palette?.body?.let { Color(it) } } ?: colors.onBackground,
                                fontWeight = FontWeight.Bold
                            )
                    )
                }
            }
        }
    }

    @Composable
    fun Controls() {
        PrevNextControls()
        PrevNextTooltips()

        when (state.fullscreen.value) {
            true ->
                Align(alignment = Alignment.TopRight) {
                    Button(
                        style = TextButtonStyle(
                            contentColor = White.copy(alpha = 200F)
                        ),
                        onClick = {
                            processAction(R.drawable.ic_fullscreen_exit)
                        }
                    ) {
                        Crossfade(uiKeysState[UiKeys.fullscreen]?.value) {
                            when (it) {
                                true ->
                                    Container(modifier = androidx.ui.layout.Size(114.dp, 80.dp)) {
                                        DrawVector(
                                            +vectorResource(R.drawable.ic_fullscreen_exit),
                                            tintColor = White
                                        )
                                    }
                                else ->
                                    Container(modifier = androidx.ui.layout.Size(100.dp, 80.dp)) { }
                            }
                        }
                    }
                }
            else -> when (state.foto.value?.apod?.mediaType == MediaType.YouTube && state.busy.value.not()) {
                true -> Center {
                    Button(
                        onClick = { open(this, fotoModel.foto!!.apod, fotoModel.foto!!.hd) },
                        style = ContainedButtonStyle(color = Color.Red)
                    ) {
                        Container(modifier = androidx.ui.layout.Size(60.dp, 36.dp)) {
                            DrawVector(
                                +vectorResource(R.drawable.ic_play_arrow),
                                tintColor = White
                            )
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun PrevNextControls() {
        (state.busy.value.not() && state.fullscreen.value) tru {
            FlexRow {
                expanded(1F) {
                    Button(
                        onClick = fotoModel::loadPrev,
                        style = TextButtonStyle(
                            contentColor = White.copy(alpha = 200F)
                        )
                    ) {
                        Container(modifier = ExpandedHeight) { }
                    }
                }
                expanded(1F) {
                    Clickable( onClick = { processAction(R.drawable.ic_fullscreen_exit) } ) {
                        Container(modifier = ExpandedHeight) { }
                    }
                }
                expanded(1F) {
                    Button(
                        onClick = fotoModel::loadNext,
                        style = TextButtonStyle(
                            contentColor = White.copy(alpha = 200F)
                        )
                    ) {
                        Container(modifier = ExpandedHeight) { }
                    }
                }
            }
        }
    }

    @Composable
    fun PrevNextTooltips() {
        FlexRow(
            crossAxisAlignment = CrossAxisAlignment.Center
        ) {
            expanded(1F) {
                Crossfade(uiKeysState[UiKeys.prev]?.value) {
                    Center {
                        when (it) {
                            true ->
                                Container(modifier = androidx.ui.layout.Size(40.dp, 40.dp)) {
                                    DrawVector(
                                        +vectorResource(R.drawable.ic_arrow_back),
                                        tintColor = White
                                    )
                                }
                        }
                    }
                }
            }
            expanded(1F) {
                Container { }
            }
            expanded(1F) {
                Crossfade(uiKeysState[UiKeys.next]?.value) {
                    Center {
                        when (it) {
                            true ->
                                Container(modifier = androidx.ui.layout.Size(40.dp, 40.dp)) {
                                    DrawVector(
                                        +vectorResource(R.drawable.ic_arrow_forward),
                                        tintColor = White
                                    )
                                }
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun Bottom() {
        Clickable(onClick = { /* consume clicks */}) {
            Crossfade(state.fullscreen.value.not() && state.foto.value.notnil()) {
                when (it) {
                    true ->
                        Surface(elevation = 1.dp, color = colors.primaryVariant) {
                            Padding(
                                padding = EdgeInsets(left = 16.dp, right = 16.dp, top = 14.dp, bottom = 14.dp)
                            ) {
                                BottomBar()
                            }
                        }
                }
            }
        }
    }

    @Composable
    fun BottomBar() {
        FlexRow(
            modifier = ExpandedWidth,
            crossAxisAlignment = CrossAxisAlignment.Center
        ) {
            val locked = (state.busy.value && fotoModel.isExecuting) || autoLoad
            expanded(0.5F) {
                val hd = state.hd.value
                Button(
                    style = when (hd) {
                        true ->
                            OutlinedButtonStyle(
                                color = colors.primary,
                                contentColor = colors.onPrimary
                            )
                        else ->
                            OutlinedButtonStyle(
                                color = Gray.copy(alpha = 127F),
                                contentColor = LightGray.copy(alpha = 77F)
                            )
                    },
                    onClick = {
                        fotoModel.loadWithHD(!hd)
                    }.takeUnless { locked }
                ) {
                    Text(text = R.string.hd.asString().let { if (locked) it.mask() else it },
                        style = typography.caption.copy(fontWeight = FontWeight.W300)
                    )
                }
            }
            inflexible {
                WidthSpacer(16.dp)
            }
            expanded(1.2F) {
                val date = state.foto.value?.apod?.date
                if (locked) {
                    Button(
                        text = (date ?: "__").mask()
                    )
                } else {
                    val onClick = {
                        selectDate(state.date.value) {
                            fotoModel.loadWithDate(it)
                        }
                    }
                    date nil {
                        Button(
                            onClick = onClick
                        ) {
                            val tintColor = colors.onPrimary.copy(alpha = 55F)
                            DrawVector(
                                +vectorResource(R.drawable.ic_calendar_today),
                                tintColor = tintColor
                            )
                        }
                    } ?: Button(
                        text = date!!,
                        onClick = onClick
                    )
                }
            }
        }
    }

    @NeedsPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
    fun save() = onWriteAvailable(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)) {
        saveModel.save(fotoModel.foto!!, it)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<Str>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        onRequestPermissionsResult(requestCode, grantResults)
    }

    private fun processAction(id: Int) {
        when (id) {
            R.drawable.ic_more_horiz ->
                uiOverflowState.start(lifecycleScope, initial = true, final = false, finalDelay = 3000L)
            R.drawable.ic_fullscreen ->
                fotoModel.enterFullscreen()
            R.drawable.ic_fullscreen_exit -> {
                uiKeysState.reset()
                fotoModel.exitFullscreen()
            }
            R.drawable.ic_share ->
                fotoModel.foto!!.also { foto ->
                    ShareCompat.IntentBuilder.from(this)
                        .setSubject(foto.apod.title)
                        .setText(foto.apod.hdUrl ?: foto.apod.url)
                        .setType("image/*")
                        .startChooser()
                }
            R.drawable.ic_details ->
                fotoModel.foto?.also { foto ->
                    showMsg(foto.apod.title, foto.apod.description)
                }
            R.drawable.ic_photo -> start<FotoViewActivity>()
            R.drawable.ic_sd_storage -> saveWithPermissionCheck()
        }
    }

    private fun tryAutoFullscreen(resetUserAction: Bool = false) {
        resetUserAction tru {
            hasUserAction = false
        }
        lifecycleScope.launchWhenStarted {
            delay(3000)
            (fullscreen.isnil() && !hasUserAction) tru {
                fotoModel.enterFullscreen()
            }
        }
    }

    override fun onUserInteraction() { super.onUserInteraction()
        lg { "user interaction" }
        hasUserAction = true
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Bool {
        if (state.fullscreen.value) {
            processAction(R.drawable.ic_fullscreen_exit)
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

}
