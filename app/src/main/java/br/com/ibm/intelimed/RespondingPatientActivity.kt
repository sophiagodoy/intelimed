// TELA PARA O MÉDICO VER OS SINTOMAS DO PACIENTE E RESPONDER ELE

package br.com.ibm.intelimed

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.ibm.intelimed.ui.theme.IntelimedTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RespondingPatient() {
    val teal = Color(0xFF007C7A)
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    // Simulando dados do paciente (futuramente virão do Firestore)
    val nomePaciente = "Maria Silva"
    val sintomas = mapOf(
        "Sentimento" to "Cansada e com dor de cabeça",
        "Dormiu bem" to "Não",
        "Cansaço" to "Sim",
        "Hidratação" to "Parcial",
        "Febre" to "Sim (37.9°C)",
        "Tomou medicação" to "Paracetamol às 08h",
        "Observações" to "Dores leves persistem desde ontem"
    )

    var textoFeedback by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Respostas de $nomePaciente",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = teal)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(20.dp)
                .verticalScroll(scrollState)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // ======= SEÇÃO DE SINTOMAS =======
            Text(
                "Respostas do Paciente",
                fontWeight = FontWeight.Bold,
                color = teal,
                fontSize = 20.sp
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9)),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    sintomas.forEach { (pergunta, resposta) ->
                        Text(
                            text = pergunta,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            color = Color.Black
                        )
                        Text(
                            text = resposta,
                            color = Color.DarkGray,
                            fontSize = 15.sp,
                            modifier = Modifier.padding(bottom = 10.dp)
                        )
                    }
                }
            }

            // ======= SEÇÃO DE FEEDBACK =======
            Text(
                "Responder ao Paciente",
                fontWeight = FontWeight.Bold,
                color = teal,
                fontSize = 20.sp
            )

            OutlinedTextField(
                value = textoFeedback,
                onValueChange = { textoFeedback = it },
                label = { Text("Escreva seu feedback aqui") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            )

            Button(
                onClick = {
                    if (textoFeedback.isNotBlank()) {
                        Toast.makeText(context, "Feedback enviado com sucesso!", Toast.LENGTH_SHORT).show()
                        textoFeedback = ""
                    } else {
                        Toast.makeText(context, "Escreva um feedback antes de enviar.", Toast.LENGTH_SHORT).show()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = teal),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Enviar Feedback", fontSize = 18.sp)
            }
        }
    }
}

@Preview(showSystemUi = true, showBackground = true)
@Composable
fun RespondingPatientPreview() {
    IntelimedTheme {
        RespondingPatient()
    }
}
