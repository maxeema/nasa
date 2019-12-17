//package maxeem.america.nasa.ui
//
//import android.os.Bundle
//import androidx.compose.State
//import androidx.compose.state
//import androidx.compose.unaryPlus
//import androidx.lifecycle.Lifecycle
//import androidx.lifecycle.lifecycleScope
//import androidx.ui.animation.Crossfade
//import androidx.ui.core.Alignment
//import androidx.ui.core.Text
//import androidx.ui.core.dp
//import androidx.ui.core.setContent
//import androidx.ui.foundation.shape.corner.CircleShape
//import androidx.ui.graphics.Color
//import androidx.ui.layout.*
//import androidx.ui.material.Button
//import androidx.ui.material.TextButtonStyle
//import androidx.ui.text.TextStyle
//import kotlinx.coroutines.delay
//import kotlinx.coroutines.launch
//import maxeem.america.nasa.R
//import maxeem.america.nasa.Conf
//import maxeem.america.nasa.ext.asString
//import maxeem.america.nasa.ext.start
//import maxeem.america.nasa.ext.tru
//import maxeem.america.nasa.util.delayRandom
//import maxeem.america.nasa.util.open
//
//class WelcomeActivity : ComposeActivity() {
//
//    private lateinit var countdown : State<Int>
//    private suspend fun countdown() {
//        while (lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
//            delay(200)
//            if (countdown.value <= 0) break
//            countdown.value--
//        }
//    }
//    override fun onStop() { super.onStop()
//        countdown.value = -1
//    }
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContent {
//            var galState by +state { false }
//            var picState by +state { false }
//            countdown = +state { 15 }
//            lifecycleScope.launchWhenResumed {
//                launch { delayRandom(200); galState = true }
//                launch { delayRandom(200); picState = true }
//                launch { delay(100); countdown(); delay(50); if (countdown.value == 0) start<FotoActivity>() }
//            }
//            AppTheme {
//                Container(padding = EdgeInsets(30.dp), modifier = ExpandedWidth, alignment = Alignment.Center) {
//                    Text(R.string.welcome.asString(),
//                        style = TextStyle(color = colors.onBackground)
//                    )
//                }
//                FlexColumn(
//                    crossAxisAlignment = CrossAxisAlignment.Center
//                ) {
//                    expanded(.5F) {
//                        Container {  }
//                    }
//                    expanded(.3F) {
//                        Align(alignment = Alignment.Center) {
//                            Row(modifier = ExpandedWidth, arrangement = Arrangement.SpaceEvenly) {
//                                Crossfade(galState) {
//                                    it tru {
//                                        Button(R.string.gallery.asString(), onClick = { start<GalleryActivity>() })
//                                    }
//                                }
//                                Crossfade(picState) {
//                                    it tru {
//                                        FlexColumn(crossAxisAlignment = CrossAxisAlignment.Center) {
//                                            inflexible {
//                                                Button(R.string.day_foto.asString(), onClick = { start<FotoActivity>() })
//                                            }
//                                            inflexible {
//                                                Crossfade(countdown.value) {
//                                                    Text(if (it>0) "$it" else " ", style = TextStyle(color = Color.LightGray))
//                                                }
//                                            }
//                                        }
//                                    }
//                                }
//                            }
//                        }
//                    }
//                    inflexible {
//                        Button(
//                            Conf.Nasa.APOD_WEB_LABEL,
//                            modifier = Spacing(bottom = 5.dp),
//                            onClick = {
//                                open(
//                                    this@WelcomeActivity,
//                                    Conf.Nasa.APOD_WEB_URL
//                                )
//                            },
//                            style = TextButtonStyle(
//                                shape = CircleShape,
//                                contentColor = colors.onBackground.copy(alpha = 100F)
//                            )
//                        )
//                    }
//                }
//            }
//        }
//    }
//
//}
