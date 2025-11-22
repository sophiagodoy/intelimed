// TELA DA LISTA DE PACIENTES PARA O MÉDICO

package br.com.ibm.intelimed

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
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.ibm.intelimed.ui.theme.IntelimedTheme
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore

class PatientListActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            IntelimedTheme {
                PatientList()
            }
        }
    }
}

// Modelo de dados
data class Paciente(
    val nome: String = "",
    val email: String = ""
)

data class Solicitacao(
    val solicitacaoId: String = "",
    val pacienteId: String = "",
    val nome: String = "",
    val email: String = "",
    val status: String = ""
)

fun listenAcceptedPatients(
    medicoId: String,
    onResult: (List<Paciente>) -> Unit
) {
    val db = Firebase.firestore

    db.collection("solicitacoes")
        .whereEqualTo("medicoId", medicoId)
        .whereEqualTo("status", "aceito")
        .addSnapshotListener { task, _ ->

            if (task == null || task.isEmpty) {
                onResult(emptyList())
                return@addSnapshotListener
            }

            val lista = mutableListOf<Paciente>()
            var contador = task.size()

            task.forEach { solicitacao ->

                val pacienteId = solicitacao.getString("pacienteId") ?: ""

                db.collection("paciente")
                    .document(pacienteId)
                    .get()
                    .addOnSuccessListener { paciente ->

                        lista.add(
                            Paciente(
                                nome = paciente.getString("nome") ?: "",
                                email = paciente.getString("email") ?: ""
                            )
                        )

                        contador--
                        if (contador == 0) {
                            onResult(lista)
                        }
                    }
            }
        }
}

@Composable
fun PatientList() {
        var abaSelecionada by remember { mutableStateOf(0) } // 0 = Aceitos, 1 = Solicitações

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(24.dp)
        ) {

            // TABS
            TabRow(selectedTabIndex = abaSelecionada) {

                Tab(
                    selected = abaSelecionada == 0,
                    onClick = { abaSelecionada = 0 },
                    text = { Text("Pacientes") }
                )

                Tab(
                    selected = abaSelecionada == 1,
                    onClick = { abaSelecionada = 1 },
                    text = { Text("Solicitações") }
                )
            }

            Spacer(Modifier.height(16.dp))

            when (abaSelecionada) {
                0 -> ListaPacientesAceitos()
                1 -> ListaSolicitacoesPendentes()
            }
        }
}
@Composable
fun ListaPacientesAceitos() {
    val medicoId = FirebaseAuth.getInstance().currentUser?.uid ?: return

    var pacientes by remember { mutableStateOf<List<Paciente>>(emptyList()) }
    var carregando by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        listenAcceptedPatients(medicoId) { lista ->
            pacientes = lista
            carregando = false
        }
    }

    when {
        carregando -> CircularProgressIndicator(color = Color(0xFF2FA49F))

        pacientes.isEmpty() ->
            Text("Nenhum paciente aceito ainda.", color = Color.Gray)

        else -> LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(pacientes) { paciente ->
                PacienteCardModern(paciente)
            }
        }
    }
}

@Composable
fun ListaSolicitacoesPendentes() {
    val medicoId = FirebaseAuth.getInstance().currentUser?.uid ?: return

    var solicitacoes by remember { mutableStateOf<List<Solicitacao>>(emptyList()) }
    var carregando by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        listenSolicitacoesPendentes(medicoId) { lista ->
            solicitacoes = lista
            carregando = false
        }
    }

    when {
        carregando -> CircularProgressIndicator(color = Color(0xFF2FA49F))

        solicitacoes.isEmpty() ->
            Text("Nenhuma solicitação pendente.", color = Color.Gray)

        else -> LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(solicitacoes) { solicitacao ->
                CardSolicitacao(solicitacao)
            }
        }
    }
}


fun listenSolicitacoesPendentes(
    medicoId: String,
    onResult: (List<Solicitacao>) -> Unit
) {
    val db = Firebase.firestore

    db.collection("solicitacoes")
        .whereEqualTo("medicoId", medicoId)
        .whereEqualTo("status", "pendente")
        .addSnapshotListener { task, _ ->

            if (task == null || task.isEmpty) {
                onResult(emptyList())
                return@addSnapshotListener
            }

            val lista = mutableListOf<Solicitacao>()
            var contador = task.size()

            task.forEach { doc ->

                val pacienteId = doc.getString("pacienteId") ?: ""

                db.collection("paciente")
                    .document(pacienteId)
                    .get()
                    .addOnSuccessListener { paciente ->

                        lista.add(
                            Solicitacao(
                                solicitacaoId = doc.id,
                                pacienteId = pacienteId,
                                nome = paciente.getString("nome") ?: "",
                                email = paciente.getString("email") ?: "",
                                status = doc.getString("status") ?: ""
                            )
                        )

                        contador--
                        if (contador == 0) {
                            onResult(lista)
                        }
                    }
            }
        }
}

fun aceitarSolicitacao(solicitacaoId: String) {
    Firebase.firestore.collection("solicitacoes")
        .document(solicitacaoId)
        .update("status", "aceito")
}

fun recusarSolicitacao(solicitacaoId: String) {
    Firebase.firestore.collection("solicitacoes")
        .document(solicitacaoId)
        .update("status", "recusado")
}

@Composable
fun CardSolicitacao(solicitacao: Solicitacao) {

    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF7FDFC)),
        modifier = Modifier.fillMaxWidth()
    ) {

        Column(Modifier.padding(16.dp)) {

            Text(solicitacao.nome, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text(solicitacao.email, color = Color.Gray)

            Spacer(Modifier.height(12.dp))

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = { aceitarSolicitacao(solicitacao.solicitacaoId) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2FA49F))
                ) {
                    Text("Aceitar")
                }

                Button(
                    onClick = { recusarSolicitacao(solicitacao.solicitacaoId) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Recusar", color = Color.White)
                }
            }
        }
    }
}

@Composable
fun PacienteCardModern(paciente: Paciente) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF7FDFC)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* TODO: MUDAR PARA A TELA QUE APARECE OS SINTOMAS DO PACIENTE */ }
            .padding(horizontal = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = paciente.nome,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF2FA49F)
                )
                Text(
                    text = paciente.email,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = null,
                tint = Color(0xFF2FA49F)
            )
        }
    }
}
@Preview
@Composable
fun PatientListPreview() {
    IntelimedTheme {
        PatientList()
    }
}

