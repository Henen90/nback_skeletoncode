package mobappdev.example.nback_cimpl.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import mobappdev.example.nback_cimpl.R
import mobappdev.example.nback_cimpl.ui.viewmodels.FakeVM
import mobappdev.example.nback_cimpl.ui.viewmodels.GameType
import mobappdev.example.nback_cimpl.ui.viewmodels.GameViewModel

/**
 * This is the Home screen composable
 *
 * Currently this screen shows the saved highscore
 * It also contains a button which can be used to show that the C-integration works
 * Furthermore it contains two buttons that you can use to start a game
 *
 * Date: 25-08-2023
 * Version: Version 1.0
 * Author: Yeetivity
 *
 */

@Composable
fun HomeScreen(
    vm: GameViewModel, onStartGameClicked: () -> Unit, onSettingsClicked: () -> Unit
) {
    val highscore by vm.highscore.collectAsState()  // Highscore is its own StateFlow
    val gameState by vm.gameState.collectAsState()
    val snackBarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var audioSelected by remember { mutableStateOf(false) }
    var visualSelected by remember { mutableStateOf(true)}

    fun updateGameType(){
        when{
            audioSelected && visualSelected -> vm.setGameType(GameType.AudioVisual)
            audioSelected -> vm.setGameType(GameType.Audio)
            visualSelected -> vm.setGameType(GameType.Visual)
            else -> vm.setGameType(GameType.Visual)
        }
    }

    Scaffold(
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                modifier = Modifier.padding(32.dp),
                text = "High-Score = $highscore",
                style = MaterialTheme.typography.headlineLarge
            )
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Selected mode: ${gameState.gameType.name}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Current N-Back: ${vm.nBack.collectAsState().value}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Current Interval Time: ${vm.eventInterval / 1000} s",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Number of Events: ${vm.nrOfEvents.collectAsState().value}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onSettingsClicked,
                        modifier = Modifier
                            .padding(16.dp)
                            .height(60.dp)
                            .width(200.dp)) {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_settings_24),
                            contentDescription = "Settings icon",
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text("Settings")
                    }
                }
            }

            Button(
                onClick = onStartGameClicked,
                modifier = Modifier
                    .padding(16.dp)
                    .height(60.dp)
                    .width(200.dp)
            ) {
                Text("Start Game".uppercase())
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(onClick = {
                    audioSelected = !audioSelected
                    updateGameType()
                },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if(audioSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                    )
                    ) {
                    Icon(
                        painter = painterResource(id = R.drawable.sound_on),
                        contentDescription = "Sound",
                        modifier = Modifier
                            .height(48.dp)
                            .aspectRatio(3f / 2f)
                    )
                }
                Button(
                    onClick = {
                        visualSelected = !visualSelected
                        updateGameType()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if(visualSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                    )
                    ) {
                    Icon(
                        painter = painterResource(id = R.drawable.visual),
                        contentDescription = "Visual",
                        modifier = Modifier
                            .height(48.dp)
                            .aspectRatio(3f / 2f)
                    )
                }
            }
        }
    }
}

/*@Preview
@Composable
fun HomeScreenPreview() {
    // Since I am injecting a VM into my homescreen that depends on Application context, the preview doesn't work.
    Surface(){
        HomeScreen(FakeVM()) {}
    }
}*/