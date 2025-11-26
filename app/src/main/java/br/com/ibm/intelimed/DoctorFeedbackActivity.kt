/**
 * Tela para o paciente ler o feedback do médico
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
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.ibm.intelimed.ui.theme.IntelimedTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class DoctorFeedbackActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val feedbackText = intent.getStringExtra("feedback") ?: ""
        val dataRegistro = intent.getStringExtra("dataRegistro") ?: ""
        val sentimento = intent.getStringExtra("sentimento") ?: ""

        val pacienteId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        val relatorioId = intent.getStringExtra("relatorioId") ?: ""

        setContent {
            IntelimedTheme {
                DoctorFeedbackScreen(
                    feedbackText = feedbackText,
                    dataRegistro = dataRegistro,
                    sentimento = sentimento,
                    pacienteId = pacienteId,
                    relatorioId = relatorioId
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorFeedbackScreen(
    feedbackText: String,
    dataRegistro: String,
    sentimento: String,
    pacienteId: String,
    relatorioId: String
) {
    val teal = Color(0xFF007C7A)
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()

    var sintomasMap by remember { mutableStateOf<Map<String, Any>>(emptyMap()) }
    var carregando by remember { mutableStateOf(true) }
    var erro by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(pacienteId, relatorioId) {
        if (pacienteId.isBlank() || relatorioId.isBlank()) {
            erro = "Não foi possível carregar os detalhes do seu relatório."
            carregando = false
        } else {
            db.collection("paciente")
                .document(pacienteId)
                .collection("sintomas")
                .document(relatorioId)
                .get()
                .addOnSuccessListener { doc ->
                    val data = doc.data
                    if (data != null) {
                        sintomasMap = data
                    } else {
                        erro = "Dados do relatório não foram encontrados."
                    }
                    carregando = false
                }
                .addOnFailureListener { e ->
                    erro = "Erro ao carregar dados: ${e.message}"
                    carregando = false
                }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Resposta do médico",
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
                .padding(20.dp)
                .verticalScroll(scrollState)
                .fillMaxSize()
                .padding(bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            Text(
                "Detalhes do seu relatório",
                fontWeight = FontWeight.Bold,
                color = teal,
                fontSize = 20.sp
            )

            if (dataRegistro.isNotBlank()) {
                Text(
                    text = "Data do registro: $dataRegistro",
                    fontSize = 15.sp,
                    color = Color.DarkGray
                )
            }

            if (sentimento.isNotBlank()) {
                Text(
                    text = "Como você relatou que estava: $sentimento",
                    fontSize = 15.sp,
                    color = Color.DarkGray
                )
            }

            // ========== CARD COM OS SINTOMAS ==========
            Text(
                "O que você relatou nesse dia",
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
                    when {
                        carregando -> {
                            Text(
                                "Carregando suas respostas...",
                                color = Color.Gray
                            )
                        }

                        erro != null -> {
                            Text(
                                erro ?: "Erro ao carregar respostas.",
                                color = Color.Red
                            )
                        }

                        sintomasMap.isEmpty() -> {
                            Text(
                                "Não encontramos os detalhes desse relatório.",
                                color = Color.Gray
                            )
                        }

                        else -> {
                            val camposExibicao = listOf(
                                "sentimento"         to "Como você disse que estava se sentindo?",
                                "dormiuBem"          to "Dormiu bem na última noite?",
                                "cansaco"            to "Cansaço",
                                "alimentacao"        to "Alimentação",
                                "hidratacao"         to "Hidratação",
                                "sentiuDor"          to "Estava sentindo dor?",
                                "intensidadeDor"     to "Intensidade da dor (0 a 10)",
                                "localDor"           to "Local da dor",
                                "tipoDorMudou"       to "O tipo da dor mudou?",
                                "febre"              to "Teve febre nas últimas 24h?",
                                "temperatura"        to "Temperatura (°C)",
                                "enjoo"              to "Enjoo, vômito ou diarreia",
                                "tontura"            to "Tontura ou fraqueza",
                                "sangramento"        to "Sangramento / secreção / inchaço",
                                "fezCicatrizacao"    to "Fez cicatrização ou procedimento recente?",
                                "estadoCicatrizacao" to "Como estava a cicatrização?",
                                "tomouMedicacao"     to "Tomou medicação nas últimas 24h?",
                                "qualMedicacao"      to "Qual medicação?",
                                "horarioMedicacao"   to "Horário da medicação",
                                "observacoes"        to "Observações gerais"
                            )

                            for ((chave, label) in camposExibicao) {
                                val textoValor = sintomasMap[chave]?.toString().orEmpty()

                                if (textoValor.isNotBlank()) {
                                    Text(
                                        text = label,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 15.sp,
                                        color = Color.Black
                                    )
                                    Text(
                                        text = textoValor,
                                        color = Color.DarkGray,
                                        fontSize = 14.sp,
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ========== CARD DO FEEDBACK ==========
            Text(
                "Feedback do médico",
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
                        text = feedbackText.ifBlank {
                            "Este relatório ainda não possui feedback do médico."
                        },
                        fontSize = 16.sp,
                        color = Color.DarkGray
                    )
                }
            }
        }
    }
}

@Preview(showSystemUi = true, showBackground = true)
@Composable
fun DoctorFeedbackPreview() {
    IntelimedTheme {
        DoctorFeedbackScreen(
            feedbackText = "Exemplo de feedback do médico para o paciente.",
            dataRegistro = "01/11/2025",
            sentimento = "Com dor de cabeça e febre leve",
            pacienteId = "PACIENTE_TESTE",
            relatorioId = "RELATORIO_TESTE"
        )
    }
}
