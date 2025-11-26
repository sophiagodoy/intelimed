/**
 * Tela de orientações para o médico em relação aos pacientes
 */
package br.com.ibm.intelimed

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.ibm.intelimed.ui.theme.IntelimedTheme

class DoctorInstructionsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            IntelimedTheme {
                MedicalGuidelinesScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicalGuidelinesScreen() {

    val teal = Color(0xFF007C7A)
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Guia de Atendimento ao Paciente",
                        fontSize = 20.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        (context as? Activity)?.finish()
                    }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Voltar",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = teal)
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 20.dp, vertical = 20.dp)
                .verticalScroll(scrollState)
                .fillMaxSize()
                .padding(bottom = 60.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            // Título
            Text(
                "Como auxiliar o paciente",
                fontWeight = FontWeight.Bold,
                color = teal,
                fontSize = 20.sp
            )

            // ============================
            // CARD: Interpretação dos sintomas
            // ============================

            Text(
                "Interpretação dos sintomas",
                fontWeight = FontWeight.Bold,
                color = teal,
                fontSize = 18.sp
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9)),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Analise o relato do paciente observando intensidade, frequência e possíveis padrões. Verifique se houve piora desde o último registro e considere a combinação de sintomas ao direcionar sua orientação.",
                        color = Color.DarkGray,
                        fontSize = 15.sp
                    )
                }
            }

            // ============================
            // CARD: Comunicação com clareza
            // ============================

            Text(
                "Comunicação clara",
                fontWeight = FontWeight.Bold,
                color = teal,
                fontSize = 18.sp
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9)),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Utilize linguagem simples e direta. Evite termos excessivamente técnicos. Deixe claro o que o paciente deve fazer após receber seu feedback.",
                        color = Color.DarkGray,
                        fontSize = 15.sp
                    )
                }
            }

            // ============================
            // CARD: Feedback útil
            // ============================

            Text(
                "Como escrever um bom feedback",
                fontWeight = FontWeight.Bold,
                color = teal,
                fontSize = 18.sp
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9)),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "• Foque em orientar o que o paciente deve fazer.\n\n" +
                                "• Explique possíveis causas dos sintomas.\n\n" +
                                "• Alerte sinais de gravidade quando for necessário.\n\n" +
                                "• Sugira mudanças práticas: hidratação, repouso, alimentação, ajustes na rotina.\n\n" +
                                "• Caso precise avaliar novamente, informe quando o paciente deve enviar outro registro.",
                        color = Color.DarkGray,
                        fontSize = 15.sp
                    )
                }
            }

            // ============================
            // CARD: Quando orientar busca de atendimento
            // ============================

            Text(
                "Quando orientar atendimento presencial",
                fontWeight = FontWeight.Bold,
                color = teal,
                fontSize = 18.sp
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9)),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Caso o paciente apresente sinais de alerta como dor intensa, febre persistente, dificuldade respiratória, sangramento anormal ou piora progressiva, recomende buscar atendimento presencial imediatamente.",
                        color = Color.DarkGray,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}

@Preview(showSystemUi = true, showBackground = true)
@Composable
fun MedicalGuidelinesPreview() {
    IntelimedTheme {
        MedicalGuidelinesScreen()
    }
}
