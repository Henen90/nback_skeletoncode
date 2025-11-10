package mobappdev.example.nback_cimpl.ui.screens

import android.R.id.input
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ModifierLocalBeyondBoundsLayout
import androidx.compose.ui.unit.dp
import mobappdev.example.nback_cimpl.ui.viewmodels.GameVM

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingScreen(vm: GameVM, onBackToMenuClicked: () -> Unit){
    var nValue by remember {mutableStateOf(2) }
    var expanded1 by remember {mutableStateOf(false)}
    var expanded2 by remember {mutableStateOf(false)}
    var eventTimer by remember {mutableStateOf(2000)}

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ){
        Text("Settings", style = MaterialTheme.typography.headlineLarge)

        Spacer(modifier = Modifier.height(24.dp))

        ExposedDropdownMenuBox(
            expanded = expanded1,
            onExpandedChange = {expanded1 = !expanded1}
        ) {
            OutlinedTextField(
                value = "N-back: $nValue",
                onValueChange = {},
                label = {Text("Select N-Back value")},
                readOnly = true,
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded1)
                },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(0.7f)
            )
            ExposedDropdownMenu(
                expanded = expanded1,
                onDismissRequest = {expanded1 = false}
            ) {
                (1..5).forEach { n ->
                    DropdownMenuItem(
                        text = {Text("N = $n") },
                        onClick = {
                            nValue = n
                            expanded1 = false
                        }
                    )
                }
            }
        }
        ExposedDropdownMenuBox(
            expanded = expanded2,
            onExpandedChange = {expanded2 = !expanded2}
        ) {
            OutlinedTextField(
                value = "Event timer: ${eventTimer/1000}s",
                onValueChange = {},
                label = {Text("Select Event Interval")},
                readOnly = true,
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded2)
                },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(0.7f)
            )
            ExposedDropdownMenu(
                expanded = expanded2,
                onDismissRequest = {expanded2 = false}
            ) {
                (1000..10000 step 1000).forEach { ms ->
                    DropdownMenuItem(
                        text = {Text("${ms/1000}s") },
                        onClick = {
                            eventTimer = ms
                            expanded2 = false
                        }
                    )
                }
            }
        }
        Button(
            onClick = { },
            modifier = Modifier
                .padding(24.dp)
                .height(60.dp)
                .width(200.dp)
        ) {
            Text("Save Changes")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onBackToMenuClicked,
            modifier = Modifier
                .padding(24.dp)
                .height(60.dp)
                .width(200.dp)
        ) {
            Text("Back to Menu")
        }
    }


}


