package mobappdev.example.nback_cimpl.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import mobappdev.example.nback_cimpl.ui.viewmodels.GameViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment

@Composable
fun GameScreen(){
    var message by androidx.compose.runtime.remember {
        androidx.compose.runtime.mutableStateOf("Game Screen")
    }
    var counter by androidx.compose.runtime.remember {
        androidx.compose.runtime.mutableStateOf(0)
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(message)

        Spacer(modifier = Modifier.height(16.dp))

        Text("Värdet är: $counter")
        Button(onClick = {
            counter++
        }) {
            Text("Tryck här")
        }
        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = {
            counter = 0
        }) {
            Text("Återställ")
        }
    }

}

@Preview(showBackground = true)
@Composable
fun GameScreenPreview() {
    GameScreen()
}