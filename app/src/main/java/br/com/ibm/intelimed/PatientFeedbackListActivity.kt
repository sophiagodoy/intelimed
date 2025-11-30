package br.com.ibm.intelimed

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.ibm.intelimed.ui.theme.IntelimedTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

// Cores base que estamos usando na tela
private val Teal = Color(0xFF007C7A)
private val CardBg = Color(0xFFFFFFFF)

// Modelo do item que o paciente enxerga na lista de feedbacks
data class PatientFeedback(
    val id: String = "",
    val date: String = "",
    val sentimento: String = "",
    val feedback: String = ""
)

class PatientFeedbackListActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            IntelimedTheme {
                PatientFeedbackListScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientFeedbackListScreen() {
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()

    // Lista de feedbacks que vai sendo preenchida com o que vem do Firestore
    val feedbacks = remember { mutableStateListOf<PatientFeedback>() }

    // Controle de carregamento e erro de tela
    var carregando by remember { mutableStateOf(true) }
    var erro by remember { mutableStateOf<String?>(null) }

    // Busca inicial dos feedbacks do paciente logado
    LaunchedEffect(Unit) {
        val uidPaciente = FirebaseAuth.getInstance().currentUser?.uid

        if (uidPaciente == null) {
            erro = "Usuário não autenticado."
            carregando = false
            return@LaunchedEffect
        }

        db.collection("paciente")
            .document(uidPaciente)
            .collection("sintomas")
            .get()
            .addOnSuccessListener { task ->
                feedbacks.clear()

                for (doc in task) {
                    val feedbackTexto = doc.getString("feedback") ?: ""
                    if (feedbackTexto.isNotBlank()) {
                        val data = convertTimestampToDate(doc.get("dataRegistro"))
                        val sentimento = doc.getString("sentimento") ?: ""

                        feedbacks.add(
                            PatientFeedback(
                                id = doc.id,
                                date = data,
                                sentimento = sentimento,
                                feedback = feedbackTexto
                            )
                        )
                    }
                }

                carregando = false
            }
            .addOnFailureListener { e ->
                erro = "Erro ao carregar feedbacks: ${e.message}"
                carregando = false
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Feedbacks do médico",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        (context as? Activity)?.finish()
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Teal
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(paddingValues)
                .padding(horizontal = 24.dp, vertical = 24.dp)
        ) {

            // Estado de carregando
            if (carregando) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Teal)
                }
                return@Column
            }

            // Estado de erro
            if (erro != null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(erro ?: "Erro", color = Color.Red)
                }
                return@Column
            }

            // Nenhum feedback ainda
            if (feedbacks.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Você ainda não tem feedbacks do médico.", color = Color.Gray)
                }
            } else {
                // Lista de feedbacks do paciente
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(feedbacks) { item ->
                        PatientFeedbackCard(feedback = item)
                    }
                }
            }
        }
    }
}

@Composable
fun PatientFeedbackCard(feedback: PatientFeedback) {
    val context = LocalContext.current

    // Cada card representa um registro de sintomas que recebeu retorno do médico
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 4.dp, shape = RoundedCornerShape(20.dp))
            .clickable {
                // Ao clicar, abre a tela de detalhes do feedback
                val intent = Intent(context, DoctorFeedbackActivity::class.java).apply {
                    putExtra("feedback", feedback.feedback)
                    putExtra("dataRegistro", feedback.date)
                    putExtra("sentimento", feedback.sentimento)
                    putExtra("relatorioId", feedback.id)
                }
                context.startActivity(intent)
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            // Data do registro
            Text(
                text = feedback.date,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color.Black
            )

            Spacer(Modifier.height(6.dp))

            // Sentimento do paciente no dia
            if (feedback.sentimento.isNotBlank()) {
                Text(
                    text = "Como você estava:",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Text(
                    text = feedback.sentimento,
                    color = Color.DarkGray,
                    fontSize = 14.sp
                )

                Spacer(Modifier.height(8.dp))
            }

            // Título do trecho de feedback
            Text(
                text = "Feedback do médico:",
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = Teal
            )

            // Mostro só o começo do feedback aqui, o restante fica na tela de detalhes
            Text(
                text = feedback.feedback.take(120) +
                        if (feedback.feedback.length > 120) "..." else "",
                color = Color.DarkGray,
                fontSize = 14.sp
            )
        }
    }
}

@Preview(showSystemUi = true, showBackground = true)
@Composable
fun PatientFeedbackListPreview() {
    IntelimedTheme {
        PatientFeedbackListScreen()
    }
}
