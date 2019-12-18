package maxeem.america.nasa.ui

import android.app.AlertDialog
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
import androidx.ui.core.Modifier
import androidx.ui.core.Text
import androidx.ui.core.dp
import androidx.ui.core.setContent
import androidx.ui.foundation.Clickable
import androidx.ui.foundation.DrawImage
import androidx.ui.foundation.VerticalScroller
import androidx.ui.graphics.Color
import androidx.ui.graphics.Color.Companion.Black
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
import maxeem.america.nasa.ui.models.FotoViewModel.Ui
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
        val date: Str, val palette: ImagePalette?
    ) : Parcelable

    private enum class UiKeys { prev, open, next }

    private val fotoModel: FotoViewModel by viewModels()
    private val saveModel: FotoSaveViewModel by viewModels()

    private lateinit var state: FotoState
    private lateinit var busyTik: State<Int>
    private var autoLoad: Bool = true
    private var args: Args? = null
    private val actionsOnFotoLoad : MutableMap<Any, ()->Unit> = mutableMapOf()

    private val palette get() = state.foto.value?.image?.palette ?: args?.palette

    private lateinit var uiKeysState: UiKeysState<UiKeys, Bool>

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
        }

        fotoModel.date.observe(this) { state.date.value = it }
        fotoModel.hd.observe(this) { state.hd.value = it }

        fotoModel.uiEvent.observe(this) {
            val value = it.consume() ?: return@observe
            state.ui.value = value
            when (value) {
                Ui.Clear ->
                    state.busy.value fals {
                        uiKeysState.start(lifecycleScope, initial = true, final = false, finalDelay = 500L)
                    }
                else ->
                    uiKeysState.reset()
            }
        }

        val stateListener : (Consumable<ModelStatus>)->Unit = code@  {
            val status = it.consume() ?: return@code
            lg { "status $status" }
            state.busy.value = fotoModel.isExecuting || saveModel.isExecuting
            state.busy.value tru {
                uiKeysState.reset()
            }
            status.isBad.tru {
                handleError(status.bad)
            }
            invalidateOptionsMenu()
            autoLoad = false
        }
        fotoModel.statusEvent.observe(this, stateListener)
        saveModel.statusEvent.observe(this, stateListener)

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

        state.foto.value nil {
            actionsOnFotoLoad[uiKeysState] = {
                uiKeysState.start(lifecycleScope, initial = true, final = false, finalDelay = 500L)
            }
        }
    }

    @Composable
    fun Content() {
        Bg()
        FlexColumn {
            expanded(1F) {
                Foto()
            }
        }
        Status()
        Controls()
        Atop()
    }

    @Composable
    fun Atop() {
        (state.ui.detailed && state.foto.value.notnil()) tru {
            Clickable(onClick = { fotoModel.onSwitchUi(to = Ui.Clear) } ) {
                Container(modifier = Expanded) {
                    Surface(
                        elevation = 1.dp,
                        color = Black.copy(75F)
                    ) {
                        Padding(padding = EdgeInsets(left = 24.dp, right = 24.dp, top = 32.dp, bottom = 32.dp)) {
                            Description()
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun Description() {
        FlexColumn(
            modifier = inLandscape() tru { MaxWidth(480.dp) } ?: Modifier.None,
            crossAxisSize = LayoutSize.Expand,
            mainAxisAlignment = MainAxisAlignment.SpaceEvenly,
            crossAxisAlignment = CrossAxisAlignment.Stretch
        ) {
            val foto = state.foto.value!!
            inflexible {
                HeightSpacer(8.dp)
            }
            inflexible {
                Text(
                    text = foto.apod.title,
                    style = typography.h4.copy(
                        color = colors.onPrimary,
                        fontWeight = FontWeight.W300
                    )
                )
            }
            inflexible {
                HeightSpacer(8.dp)
            }
            inflexible {
                Text(
                    text = foto.apod.date,
                    style = typography.subtitle2.copy(
                        color = colors.onPrimary.copy(alpha = 125F)
                    )
                )
            }
            inflexible {
                HeightSpacer(16.dp)
            }
            flexible(flex = 1F) {
                foto.apod.description?.also { description ->
                    VerticalScroller {
                        Clickable(onClick = { fotoModel.onSwitchUi(to = Ui.Clear) }) {
                            Text(
                                text = description,
                                overflow = TextOverflow.Fade,
                                style = typography.body1.copy(
                                    color = colors.onPrimary.copy(alpha = 55F)
                                )
                            )
                        }
                    }
                }
            }
            inflexible {
                HeightSpacer(16.dp)
            }
            inflexible {
                Row(
                    modifier = ExpandedWidth,
                    arrangement = Arrangement.SpaceEvenly
                ) {
                    WidthSpacer(24.dp)
                    listOf(
                        R.drawable.ic_share,
                        R.drawable.ic_calendar_today,
                        R.drawable.ic_sd_storage
                    ).forEach { id ->
                        Action(
                            id,
                            enabled = when (id) {
                                R.drawable.ic_sd_storage -> saveModel.isExecuting.not()
                                R.drawable.ic_calendar_today -> fotoModel.isExecuting.not()
                                else -> true
                            }
                        )
                        WidthSpacer(16.dp)
                    }
                }
            }
        }
    }

    @Composable
    fun Action(id: Int, enabled: Bool) {
        Ripple(true, enabled = enabled) {
            Clickable(onClick = enabled tru { { execute(id) } } ) {
                Padding(top = 10.dp, bottom = 10.dp) {
                    Container(modifier = androidx.ui.layout.Size(50.dp, 20.dp)) {
                        DrawVector(
                            +vectorResource(id),
                            tintColor = Color(R.color.ic_menu_item_color.asColor()).let { enabled tru { it } ?: it.copy(alpha = 155F) }
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
                    fotoModel.onSwitchUi(to = Ui.Detailed)
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
        when {
            busy ->
                Center {
                    Crossfade(state.ui.clear) {
                        when (it) {
                            true ->
                                Progress()
                        }
                    }
                }
            foto.isnil() ->
                Center {
                    Button(
                        text = R.string.reload.asString(),
                        onClick = fotoModel::load,
                        style = OutlinedButtonStyle(
                            color = White.copy(alpha = 200F),
                            contentColor = colors.onBackground
                        )
                    )
                }
        }
    }

    @Composable
    fun Progress() {
        val noFoto = fotoModel.foto.isnil()
        Surface(
            color = if (noFoto) Transparent else Black.copy(115F),
            elevation = 0.dp,
            modifier = Spacing(
                top = 10.dp, bottom = 10.dp,
                left = 16.dp, right = 16.dp
            ) wraps { inLandscape() tru { MaxWidth(360.dp) } ?: Modifier.None }()
        ) {
            FlexRow(
                modifier = ExpandedWidth wraps Spacing(20.dp),
                mainAxisAlignment = MainAxisAlignment.Center,
                crossAxisAlignment = CrossAxisAlignment.Center
            ) {
                flexible(1F) {
                    val text = when {
                        fotoModel.isExecuting && saveModel.isExecuting ->
                            R.string.executing.asString()
                        fotoModel.isExecuting -> R.string.loading.asString()
                        saveModel.isExecuting -> R.string.saving.asString()
                        else -> R.string.executing.asString()
                    }
                    Text(
                        text = text.toTikString(busyTik.value),
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
        PlayControl()
        PrevNextControls()
        PrevNextTooltips()
    }
    @Composable
    fun PlayControl() {
        when (state.foto.value?.apod?.mediaType == MediaType.YouTube && state.busy.value.not()) {
            true -> Center {
                Button(
                    style = ContainedButtonStyle(color = Color.Red)
                ) {
                    Container(modifier = androidx.ui.layout.Size(50.dp, 30.dp)) {
                        DrawVector(
                            +vectorResource(R.drawable.ic_play_arrow),
                            tintColor = White
                        )
                    }
                }
            }
        }
    }

    @Composable
    fun PrevNextControls() {
        val foto = fotoModel.foto ?: return
        (state.ui.clear && state.busy.value.not()) tru {
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
                    Button(
                        onClick = { fotoModel.onSwitchUi(to = Ui.Detailed) },
                        style = TextButtonStyle(
                            contentColor = White.copy(alpha = 200F)
                        )
                    ) {
                        FlexColumn(crossAxisAlignment = CrossAxisAlignment.Stretch) {
                            expanded(1F) { Container {} }
                            expanded(1F) {
                                Button(
                                    onClick = {
                                        when (foto.apod.mediaType) {
                                            MediaType.YouTube ->
                                                open(this@FotoActivity, foto.apod, foto.hd)
                                            else ->
                                                execute(R.drawable.ic_photo)
                                        }
                                    },
                                    style = TextButtonStyle(
                                        contentColor = White.copy(alpha = 200F)
                                    )
                                ) {
                                    Container(modifier = ExpandedHeight) { }
                                }
                            }
                            expanded(1F) { Container {} }
                        }
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
                Crossfade(uiKeysState[UiKeys.open]?.value) {
                    Center {
                        when (it) {
                            true ->
                                Container(modifier = androidx.ui.layout.Size(40.dp, 40.dp)) {
                                    DrawVector(
                                        +vectorResource(
                                            when (fotoModel.foto?.apod?.mediaType) {
                                                MediaType.YouTube ->
                                                    R.drawable.ic_play_arrow
                                                else ->
                                                    R.drawable.ic_photo
                                            }
                                        ),
                                        tintColor = White
                                    )
                                }
                        }
                    }
                }
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

    @NeedsPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
    fun save() = onWriteAvailable(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)) {
        saveModel.save(fotoModel.foto!!, it)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<Str>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        onRequestPermissionsResult(requestCode, grantResults)
    }

    private fun execute(id: Int) {
        when (id) {
            R.drawable.ic_calendar_today ->
                selectDate(
                    state.date.value,
                    // on some configurations system bottom nav will appear on Dialog showing / dismissing
                    onShow = { d ->
                        lifecycleScope.launchWhenStarted {
                            (d is AlertDialog) tru { Fullscreen.enterOn(d as AlertDialog) }
                            delay(100)
                            enterFullscreen()
                        }
                    },
                    onDismiss = { enterFullscreen() }
                ) {
                    fotoModel.onSwitchUi(to = Ui.Clear)
                    fotoModel.loadWithDate(it)
                }
            R.drawable.ic_share ->
                fotoModel.foto!!.also { foto ->
                    ShareCompat.IntentBuilder.from(this)
                        .setSubject(foto.apod.title)
                        .setText(foto.apod.hdUrl ?: foto.apod.url)
                        .setType("image/*")
                        .startChooser()
                }
            R.drawable.ic_photo -> start<FotoViewActivity>()
            R.drawable.ic_sd_storage -> saveWithPermissionCheck()
        }
    }
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Bool {
        if (state.ui.detailed) {
            fotoModel.onSwitchUi(to = Ui.Clear)
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onResume() {
        super.onResume()
        enterFullscreen()
    }

    private fun enterFullscreen() = Fullscreen.enterOn(this)

    val State<Ui>.detailed get() = this.value === Ui.Detailed
    val State<Ui>.clear get() = this.value === Ui.Clear

}
