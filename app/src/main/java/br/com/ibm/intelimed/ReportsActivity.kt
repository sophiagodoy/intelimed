/**
 * Tela para o médico visualizar os relatórios dos pacientes
 */

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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Cores usadas na tela de relatórios
private val Teal = Color(0xFF007C7A)
private val CardBg = Color(0xFFFFFFFF)
private val FeedbackGreen = Color(0xFF4CAF50)
private val WaitingRed = Color(0xFFF44336)

// Modelo que representa um relatório na lista
data class Report(
    val id: String = "",
    val pacienteId: String = "",
    val pacienteNome: String = "",
    val date: String = "",
    val symptoms: String = "",
    val feedback: String = ""
)

class ReportsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ReportsScreen()
        }
    }
}

// Converte diferentes formatos de timestamp em uma data dd/MM/yyyy
fun convertTimestampToDate(value: Any?): String {
    if (value == null) return ""
    return try {
        val millis = when (value) {
            is Long -> value
            is Int -> value.toLong()
            is Double -> value.toLong()
            is com.google.firebase.Timestamp -> value.toDate().time
            is String -> value.toLong()
            else -> return ""
        }
        val date = Date(millis)
        val format = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        format.format(date)
    } catch (_: Exception) {
        ""
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen() {
    val context = LocalContext.current

    // Lista reativa de relatórios que serão exibidos
    val reports = remember { mutableStateListOf<Report>() }

    // Filtro selecionado no topo (não respondidos ou respondidos)
    var selectedFilter by remember { mutableStateOf("Não respondidos") }

    // Escuta em tempo real os relatórios do médico logado
    LaunchedEffect(Unit) {
        val db = FirebaseFirestore.getInstance()
        val user = FirebaseAuth.getInstance().currentUser

        // Se não houver médico logado, fecha a tela de forma segura
        if (user == null) {
            (context as? Activity)?.finish()
            return@LaunchedEffect
        }

        val uidMedico = user.uid

        db.collection("medico")
            .document(uidMedico)
            .collection("relatorios")
            .addSnapshotListener { snapshot, error ->
                reports.clear()

                // Se der erro na leitura, só não atualiza a lista
                if (error != null) {
                    return@addSnapshotListener
                }

                val docs = snapshot?.documents ?: return@addSnapshotListener
                if (docs.isEmpty()) return@addSnapshotListener

                docs.forEach { doc ->
                    val data = doc.data ?: return@forEach
                    val pacienteId = (data["pacienteId"] as? String).orElseEmpty()

                    // Se não tiver pacienteId, ainda assim mostra o relatório na lista
                    if (pacienteId.isBlank()) {
                        reports.add(
                            Report(
                                id = doc.id,
                                pacienteId = "",
                                pacienteNome = "Paciente não identificado",
                                date = convertTimestampToDate(data["dataRegistro"]),
                                symptoms = data["sentimento"]?.toString() ?: "",
                                feedback = data["feedback"]?.toString() ?: ""
                            )
                        )
                    } else {
                        // Busca o nome do paciente para mostrar junto do relatório
                        db.collection("paciente")
                            .document(pacienteId)
                            .get()
                            .addOnSuccessListener { pacienteDoc ->
                                val nomePaciente =
                                    pacienteDoc.getString("nome") ?: "Paciente"

                                reports.add(
                                    Report(
                                        id = doc.id,
                                        pacienteId = pacienteId,
                                        pacienteNome = nomePaciente,
                                        date = convertTimestampToDate(data["dataRegistro"]),
                                        symptoms = data["sentimento"]?.toString() ?: "",
                                        feedback = data["feedback"]?.toString() ?: ""
                                    )
                                )
                            }
                    }
                }
            }
    }

    // Aplica o filtro na lista antes de exibir
    val filteredReports = when (selectedFilter) {
        "Respondidos" -> reports.filter { it.feedback.isNotEmpty() }
        "Não respondidos" -> reports.filter { it.feedback.isEmpty() }
        else -> reports
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Meus relatórios",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        // Volta para a tela anterior do fluxo do médico
                        (context as? Activity)?.finish()
                    }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Voltar",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Teal,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
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
            // Botões de filtro "Não respondidos" / "Respondidos"
            FilterRow(
                selected = selectedFilter,
                onSelect = { selectedFilter = it }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Estado vazio quando não há relatórios para o filtro escolhido
            if (filteredReports.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Nenhum relatório encontrado.", color = Color.Gray)
                }
            } else {
                // Lista dos relatórios filtrados
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(filteredReports) { report ->
                        ReportCard(report = report)
                    }
                }
            }
        }
    }
}

// Helper simples para tratar String? nula
private fun String?.orElseEmpty() = this ?: ""

// Linha de filtros no topo da tela
@Composable
fun FilterRow(selected: String, onSelect: (String) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        val filters = listOf("Não respondidos", "Respondidos")

        filters.forEach { option ->
            val isSelected = selected == option
            Button(
                onClick = { onSelect(option) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSelected) Teal else Color.White,
                    contentColor = if (isSelected) Color.White else Teal
                ),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    width = 1.dp,
                    brush = SolidColor(Teal)
                ),
                shape = RoundedCornerShape(50),
                modifier = Modifier
                    .weight(1f)
                    .height(45.dp)
            ) {
                Text(option, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            }
            if (option != filters.last()) Spacer(modifier = Modifier.width(10.dp))
        }
    }
}

// Card individual de cada relatório na lista
@Composable
fun ReportCard(report: Report) {
    val context = LocalContext.current

    // Define status visual com base na existência de feedback
    val (statusText, statusColor) = if (report.feedback.isNotEmpty()) {
        "Feedback recebido" to FeedbackGreen
    } else {
        "Aguardando feedback" to WaitingRed
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 4.dp, shape = RoundedCornerShape(20.dp))
            .clickable {
                // Abre a tela de respostas do paciente.
                // Se já tiver feedback, abre em modo somente visualização.
                val intent = Intent(context, RespondingPatientActivity::class.java).apply {
                    putExtra("relatorioId", report.id)
                    putExtra("pacienteId", report.pacienteId)
                    putExtra("somenteVisualizar", report.feedback.isNotEmpty())
                }
                context.startActivity(intent)
            }
    ) {
        Column(
            modifier = Modifier.padding(
                start = 20.dp,
                end = 20.dp,
                top = 24.dp,
                bottom = 16.dp
            )
        ) {
            // Nome do paciente
            Text(
                text = report.pacienteNome,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Teal
            )

            Spacer(Modifier.height(4.dp))

            // Data do relatório
            Text(
                text = report.date,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                color = Color.Black
            )

            Spacer(Modifier.height(12.dp))

            // Status do feedback (já respondido ou não)
            Text(
                text = statusText,
                color = statusColor,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(Modifier.height(10.dp))

            // Pequeno resumo usando o sentimento/sintomas do dia
            Text(
                text = "Sintomas: ${report.symptoms}",
                color = Color.DarkGray,
                fontSize = 14.sp
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ReportsScreenPreview() {
    ReportsScreen()
}
