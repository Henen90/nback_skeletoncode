package mobappdev.example.nback_cimpl.ui.screens


import android.speech.tts.TextToSpeech
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import mobappdev.example.nback_cimpl.ui.viewmodels.GameViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.delay
import mobappdev.example.nback_cimpl.ui.viewmodels.FakeVM
import mobappdev.example.nback_cimpl.ui.viewmodels.GameType
import java.util.Locale

@Composable
fun GameScreen(vm: GameViewModel, onBackToMenuClicked: () -> Unit, onStartGameClicked: () -> Unit){
    val gameState by vm.gameState.collectAsState()
    val score by vm.score.collectAsState()
    val eventTic by vm.eventTic.collectAsState()
    val context = LocalContext.current
    var ttsReady by remember {mutableStateOf(false)}

    val tts = remember {
        TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                ttsReady = true
            }
        }
    }

LaunchedEffect(eventTic) {
    if ((ttsReady && gameState.gameType == GameType.Audio) || (ttsReady && gameState.gameType == GameType.AudioVisual)) {
        val letters = listOf("A", "B", "C", "D", "E", "F", "G", "H", "I")
        val letter = letters[gameState.eventValue - 1]
        println("Speaking letter: $letter")
        tts.language = Locale.US
        tts.speak(letter, TextToSpeech.QUEUE_FLUSH, null, null)
    }
}

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Game Screen", style = MaterialTheme.typography.headlineLarge)

            Spacer(modifier = Modifier.height(16.dp))

            Text("N = ${vm.nBack.collectAsState().value}", style = MaterialTheme.typography.headlineLarge)

            Spacer(modifier = Modifier.height(16.dp))
            if(gameState.gameType == GameType.Visual || gameState.gameType == GameType.AudioVisual) {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    for (row in 0 until 3) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            for (col in 0 until 3) {
                                val index = row * 3 + col + 1
                                val isActive = gameState.eventValue == index

                                var color by remember { mutableStateOf(Color.LightGray) }

                                LaunchedEffect(eventTic) {
                                    if (isActive) {
                                        color = Color.Green
                                        delay(1000)
                                        color = Color.LightGray
                                    }
                                }
                                val animatedColor by animateColorAsState(
                                    targetValue = color,
                                    animationSpec = tween(durationMillis = 300),
                                    label = ""
                                )
                                Box(
                                    modifier = Modifier
                                        .padding(4.dp)
                                        .size(100.dp)
                                        .background(
                                            color = animatedColor,
                                            shape = RoundedCornerShape(12.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {

                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Score: $score")

            Button(
                onClick = { vm.checkMatch() },
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth()
                    .height(80.dp)
            ) {
                Text("Match!", style = MaterialTheme.typography.headlineSmall)
            }
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onStartGameClicked,
                modifier = Modifier
                    .padding(24.dp)
                    .height(60.dp)
                    .width(200.dp)
            ) {
                Text("New Game")
            }

        }
        Button(
            onClick = onBackToMenuClicked,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(24.dp)
                .height(60.dp)
                .width(200.dp)
        ) {
            Text("Back to Menu")
        }
    }
    DisposableEffect(Unit) {
        onDispose {
            tts.stop()
            tts.shutdown()
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GameScreenPreview() {
    GameScreen(
        FakeVM(),
        onBackToMenuClicked = TODO()
    ) {}
}