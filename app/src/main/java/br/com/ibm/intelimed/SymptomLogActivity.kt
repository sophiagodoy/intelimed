package br.com.ibm.intelimed

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.ibm.intelimed.ui.theme.IntelimedTheme

class RegistroSintomasActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            IntelimedTheme {
                RegistroSintomasScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistroSintomasScreen() {
    var dor by remember { mutableStateOf(5f) }
    var febre by remember { mutableStateOf(TextFieldValue("")) }
    var cicatrizacao by remember { mutableStateOf("Boa") }
    var tomouMedicacao by remember { mutableStateOf(false) }
    var observacoes by remember { mutableStateOf(TextFieldValue("")) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Registro de sintomas", fontSize = 20.sp) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF007C7A),
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(20.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // Slider de Dor
            Text("Dor")
            Slider(
                value = dor,
                onValueChange = { dor = it },
                valueRange = 0f..10f,
                steps = 9,
                colors = SliderDefaults.colors(
                    thumbColor = Color(0xFF007C7A),
                    activeTrackColor = Color(0xFF007C7A)
                )
            )
            Text("${dor.toInt()} / 10")

            // Campo de Febre
            OutlinedTextField(
                value = febre,
                onValueChange = { febre = it },
                label = { Text("Febre (°C)") },
                modifier = Modifier.fillMaxWidth()
            )

            // Cicatrização
            Text("Cicatrização (estado)")
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = cicatrizacao == "Boa",
                    onClick = { cicatrizacao = "Boa" },
                    colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF007C7A))
                )
                Text("Boa")
                Spacer(modifier = Modifier.width(16.dp))
                RadioButton(
                    selected = cicatrizacao == "Regular",
                    onClick = { cicatrizacao = "Regular" },
                    colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF007C7A))
                )
                Text("Regular")
                Spacer(modifier = Modifier.width(16.dp))
                RadioButton(
                    selected = cicatrizacao == "Ruim",
                    onClick = { cicatrizacao = "Ruim" },
                    colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF007C7A))
                )
                Text("Ruim")
            }

            // Checkbox
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = tomouMedicacao,
                    onCheckedChange = { tomouMedicacao = it },
                    colors = CheckboxDefaults.colors(checkedColor = Color(0xFF007C7A))
                )
                Text("Tomei medicação")
            }

            // Observações
            OutlinedTextField(
                value = observacoes,
                onValueChange = { observacoes = it },
                label = { Text("Observações") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
            )

            // Botões
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(
                    onClick = { /* salvar rascunho */ },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF007C7A))
                ) {
                    Text("Salvar rascunho")
                }
                Button(
                    onClick = {
                        // enviar dados
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007C7A))
                ) {
                    Text("Enviar")
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun RegistroSintomasPreview() {
    IntelimedTheme {
        RegistroSintomasScreen()
    }
}
