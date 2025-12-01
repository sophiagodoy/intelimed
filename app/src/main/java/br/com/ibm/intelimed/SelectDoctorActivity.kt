package br.com.ibm.intelimed

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
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
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SelectDoctorActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            IntelimedTheme {
                SelectDoctorScreen(onBack = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectDoctorScreen(onBack: (() -> Unit)? = null) {

    val context = androidx.compose.ui.platform.LocalContext.current

    // Paleta básica da tela
    val teal = Color(0xFF007C7A)
    val lightBg = Color(0xFFF7FDFC)
    val softGray = Color(0xFF6D6D6D)
    val green = Color(0xFF2ECC71)

    val db = FirebaseFirestore.getInstance()

    // Filtros / listas principais
    var especialidades by remember { mutableStateOf(listOf<String>()) }
    var medicos by remember { mutableStateOf(listOf<Medico>()) }

    // Estados de loading / erro
    var loadingEspecialidades by remember { mutableStateOf(true) }
    var loadingMedicos by remember { mutableStateOf(false) }
    var erro by remember { mutableStateOf("") }

    // Estado do dropdown de especialidade
    var expanded by remember { mutableStateOf(false) }
    var especialidadeSelecionada by remember { mutableStateOf("") }

    // Médico já aceito e médico que está só aguardando confirmação
    var medicoAceito by remember { mutableStateOf<Medico?>(null) }
    var medicoPendente by remember { mutableStateOf<Medico?>(null) }
    var medicoPendenteId by remember { mutableStateOf<String?>(null) }
    var solicitacaoId by remember { mutableStateOf<String?>(null) }

    // Popup de confirmação quando o paciente envia solicitação
    var showDialog by remember { mutableStateOf(false) }
    var medicoSelecionado by remember { mutableStateOf<Medico?>(null) }

    //  CARREGAR ESPECIALIDADES
    LaunchedEffect(Unit) {
        getEspecialidades(
            onSucesso = {
                especialidades = it.sorted()
                loadingEspecialidades = false
            },
            onErro = {
                erro = "Erro ao carregar especialidades!"
                loadingEspecialidades = false
            }
        )
    }

    //  MÉDICO ACEITO EM TEMPO REAL
    LaunchedEffect(Unit) {
        val uidPaciente = FirebaseAuth.getInstance().currentUser?.uid ?: return@LaunchedEffect

        db.collection("solicitacoes")
            .whereEqualTo("pacienteId", uidPaciente)
            .whereEqualTo("status", "aceito")
            .addSnapshotListener { result, _ ->
                if (result == null || result.isEmpty) {
                    // Sem médico aceito no momento
                    medicoAceito = null
                } else {
                    val solicitacao = result.documents.first()
                    val medicoId = solicitacao.getString("medicoId")

                    if (medicoId != null) {
                        db.collection("medico")
                            .document(medicoId)
                            .get()
                            .addOnSuccessListener { doc ->
                                medicoAceito = Medico(
                                    uid = medicoId,
                                    nome = doc.getString("nome") ?: "Sem nome",
                                    crm = doc.getString("crm") ?: "",
                                    especialidades = emptyList()
                                )
                            }
                    }
                }
            }
    }

    //  MÉDICO PENDENTE EM TEMPO REAL
    LaunchedEffect(Unit) {
        val uidPaciente = FirebaseAuth.getInstance().currentUser?.uid ?: return@LaunchedEffect

        db.collection("solicitacoes")
            .whereEqualTo("pacienteId", uidPaciente)
            .whereEqualTo("status", "pendente")
            .addSnapshotListener { result, _ ->
                if (result != null && !result.isEmpty) {
                    val doc = result.documents.first()
                    solicitacaoId = doc.id
                    medicoPendenteId = doc.getString("medicoId")

                    val medicoId = medicoPendenteId
                    if (medicoId != null) {
                        db.collection("medico")
                            .document(medicoId)
                            .get()
                            .addOnSuccessListener { medDoc ->
                                medicoPendente = Medico(
                                    uid = medicoId,
                                    nome = medDoc.getString("nome") ?: "Sem nome",
                                    crm = medDoc.getString("crm") ?: "",
                                    especialidades = emptyList()
                                )
                            }
                    }
                } else {
                    // Não tem nada pendente
                    solicitacaoId = null
                    medicoPendenteId = null
                    medicoPendente = null
                }
            }
    }

    //  BUSCAR MÉDICOS POR ESPECIALIDADE
    fun buscarMedicos() {
        loadingMedicos = true
        medicos = emptyList()

        val medicoAtual = medicoAceito?.uid

        db.collection("medico").get()
            .addOnSuccessListener { task ->
                val lista = mutableListOf<Medico>()

                for (doc in task) {
                    val raw = doc.get("especialidade")
                    val listaEsp = when (raw) {
                        is String -> listOf(raw)
                        is List<*> -> raw.map { it.toString() }
                        else -> emptyList()
                    }

                    // Não mostra o médico que já está vinculado
                    if (doc.id == medicoAtual) continue

                    if (especialidadeSelecionada in listaEsp) {
                        lista.add(
                            Medico(
                                uid = doc.getString("uid") ?: doc.id,
                                nome = doc.getString("nome") ?: "Sem nome",
                                crm = doc.getString("crm") ?: "",
                                especialidades = listaEsp
                            )
                        )
                    }
                }

                medicos = lista
                loadingMedicos = false
            }
            .addOnFailureListener {
                erro = "Erro ao carregar médicos!"
                loadingMedicos = false
            }
    }

    //  ENVIAR SOLICITAÇÃO PARA O MÉDICO
    fun enviarSolicitacao(medico: Medico, pacienteId: String) {
        val dados = hashMapOf(
            "pacienteId" to pacienteId,
            "medicoId" to medico.uid,
            "status" to "pendente",
            "timestamp" to Timestamp.now()
        )

        db.collection("solicitacoes")
            .add(dados)
            .addOnSuccessListener { document ->
                // Guarda o estado da solicitação pendente
                solicitacaoId = document.id
                medicoPendenteId = medico.uid
                medicoPendente = medico
            }
    }

    // Cancela a solicitação que ainda está pendente
    fun cancelarSolicitacao() {
        val id = solicitacaoId ?: return

        db.collection("solicitacoes")
            .document(id)
            .update("status", "cancelado")
            .addOnSuccessListener {
                medicoPendenteId = null
                medicoPendente = null
                solicitacaoId = null
                Toast.makeText(context, "Solicitação cancelada!", Toast.LENGTH_SHORT).show()
            }
    }

    // Encerra o atendimento com o médico atual (muda de "aceito" para "cancelado")
    fun cancelarAtendimento() {
        val uidPaciente = FirebaseAuth.getInstance().currentUser?.uid ?: return

        db.collection("solicitacoes")
            .whereEqualTo("pacienteId", uidPaciente)
            .whereEqualTo("status", "aceito")
            .get()
            .addOnSuccessListener { result ->
                if (!result.isEmpty) {
                    val doc = result.documents.first()
                    db.collection("solicitacoes")
                        .document(doc.id)
                        .update("status", "cancelado")
                        .addOnSuccessListener {
                            medicoAceito = null
                        }
                }
            }
    }

    // UI
    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = { onBack?.invoke() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar",
                            tint = Color.White
                        )
                    }
                },
                title = {
                    Text(
                        "Escolher Médico",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
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
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(22.dp)
        ) {

            // Bloco que mostra o médico já aceito
            if (medicoAceito != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(Color(0xFFE7FFF9)),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            "Seu médico atual:",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF006D6D)
                        )
                        Text(
                            medicoAceito!!.nome,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = teal
                        )
                        Text(
                            "CRM: ${medicoAceito!!.crm}",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                }

                Button(
                    onClick = { cancelarAtendimento() },
                    colors = ButtonDefaults.buttonColors(containerColor = green),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Encerrar atendimento", color = Color.White)
                }
            }

            // Linha sutil entre o topo e o restante da tela
            HorizontalDivider(
                color = teal.copy(alpha = 0.2f),
                thickness = 1.dp
            )

            // DROPDOWN DE ESPECIALIDADE
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = especialidadeSelecionada,
                    onValueChange = {},
                    label = { Text("Selecione a especialidade", color = softGray) },
                    readOnly = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = teal,
                        unfocusedBorderColor = teal.copy(alpha = 0.4f),
                        cursorColor = teal,
                        focusedLabelColor = teal
                    ),
                    trailingIcon = {
                        IconButton(onClick = { expanded = !expanded }) {
                            Icon(
                                if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                tint = teal
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expanded = true }
                )

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    especialidades.forEach { item ->
                        DropdownMenuItem(
                            text = { Text(item) },
                            onClick = {
                                especialidadeSelecionada = item
                                expanded = false
                                buscarMedicos()
                            }
                        )
                    }
                }
            }

            if (erro.isNotEmpty())
                Text(erro, color = Color.Red)

            //  CARD DO MÉDICO PENDENTE
            if (medicoPendente != null && medicoAceito == null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(Color(0xFFE7FFF9)),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            "Médico selecionado (aguardando confirmação):",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = teal
                        )
                        Text(
                            medicoPendente!!.nome,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = teal
                        )
                        Text(
                            "CRM: ${medicoPendente!!.crm}",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = { cancelarSolicitacao() },
                            colors = ButtonDefaults.buttonColors(containerColor = green),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Cancelar solicitação", color = Color.White)
                        }
                    }
                }
            }

            //  LISTA DE MÉDICOS PARA ESCOLHER
            if (especialidadeSelecionada.isNotEmpty()) {

                if (medicoAceito != null || medicoPendenteId != null) {
                    Text(
                        text = "Você já possui um médico selecionado. " +
                                "Para escolher outro, encerre o atendimento ou cancele a solicitação atual.",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                }

                Text(
                    "Médicos disponíveis (${medicos.size}):",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = teal
                )

                if (loadingMedicos) {
                    CircularProgressIndicator(color = teal)
                }

                if (medicos.isEmpty() && !loadingMedicos) {
                    Text("Nenhum médico encontrado.", color = Color.Gray)
                }

                medicos.forEach { medico ->

                    val pendente = medicoPendenteId == medico.uid

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = lightBg),
                        elevation = CardDefaults.cardElevation(6.dp)
                    ) {

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            // Avatar + nome + CRM
                            Row(verticalAlignment = Alignment.CenterVertically) {

                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .padding(end = 14.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(50),
                                        color = teal.copy(alpha = 0.15f)
                                    ) {
                                        Box(
                                            modifier = Modifier.size(48.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                medico.nome.take(1).uppercase(),
                                                fontSize = 20.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = teal
                                            )
                                        }
                                    }
                                }

                                Column {
                                    Text(
                                        medico.nome,
                                        fontSize = 18.sp,
                                        color = teal,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        "CRM: ${medico.crm}",
                                        color = Color.Gray,
                                        fontSize = 14.sp
                                    )
                                }
                            }

                            Button(
                                onClick = {
                                    enviarSolicitacao(
                                        medico = medico,
                                        pacienteId = FirebaseAuth.getInstance().currentUser!!.uid
                                    )
                                    medicoSelecionado = medico
                                    showDialog = true
                                },
                                enabled = medicoAceito == null && !pendente && medicoPendenteId == null,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (pendente) Color.Gray else green
                                ),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(
                                    if (pendente) "Pendente" else "Selecionar",
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }

        //  POPUP DE CONFIRMAÇÃO DA SOLICITAÇÃO
        if (showDialog && medicoSelecionado != null) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                shape = RoundedCornerShape(20.dp),
                containerColor = Color.White,
                tonalElevation = 6.dp,
                title = {
                    Text(
                        "Solicitação enviada!",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = teal
                    )
                },
                text = {
                    Text(
                        "Aguarde a confirmação do médico ${medicoSelecionado!!.nome}.",
                        fontSize = 16.sp,
                        color = Color.DarkGray
                    )
                },
                confirmButton = {
                    Button(
                        onClick = { showDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = teal),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("OK", color = Color.White)
                    }
                }
            )
        }
    }
}

// Modelo do médico que usamos nessa tela
data class Medico(
    val uid: String = "",
    val nome: String,
    val crm: String,
    val especialidades: List<String>
)

@Preview(showBackground = true)
@Composable
fun SelectDoctorPreview() {
    IntelimedTheme {
        SelectDoctorScreen()
    }
}
