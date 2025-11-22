package br.com.ibm.intelimed

import android.os.Bundle
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

    var showDialog by remember { mutableStateOf(false) }
    var medicoSelecionado by remember { mutableStateOf<Medico?>(null) }
    var medicoPendenteId by remember { mutableStateOf<String?>(null) }
    var solicitacaoId by remember { mutableStateOf<String?>(null) }
    var medicoAceito by remember { mutableStateOf<Medico?>(null) }


    val teal = Color(0xFF007C7A)
    val lightBg = Color(0xFFF7FDFC)
    val softGray = Color(0xFF6D6D6D)

    val db = FirebaseFirestore.getInstance()

    var especialidades by remember { mutableStateOf(listOf<String>()) }
    var medicos by remember { mutableStateOf(listOf<Medico>()) }

    var loadingEspecialidades by remember { mutableStateOf(true) }
    var loadingMedicos by remember { mutableStateOf(false) }
    var erro by remember { mutableStateOf("") }

    var expanded by remember { mutableStateOf(false) }
    var especialidadeSelecionada by remember { mutableStateOf("") }

    // CARREGAR ESPECIALIDADES
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

    LaunchedEffect(Unit) {
        val uidPaciente = FirebaseAuth.getInstance().currentUser!!.uid

        db.collection("solicitacoes")
            .whereEqualTo("pacienteId", uidPaciente)
            .whereEqualTo("status", "pendente")
            .get()
            .addOnSuccessListener { result ->
                if (!result.isEmpty) {
                    val doc = result.documents.first()
                    medicoPendenteId = doc.getString("medicoId")
                    solicitacaoId = doc.id
                }
            }
    }

    LaunchedEffect(Unit) {
        val uidPaciente = FirebaseAuth.getInstance().currentUser!!.uid

        db.collection("solicitacoes")
            .whereEqualTo("pacienteId", uidPaciente)
            .whereEqualTo("status", "aceito")
            .addSnapshotListener { task, _ ->

                if (task == null || task.isEmpty) {
                    medicoAceito = null
                    return@addSnapshotListener
                }

                val solicitacao = task.documents.first()
                val medicoId = solicitacao.getString("medicoId") ?: return@addSnapshotListener

                db.collection("medico")
                    .document(medicoId)
                    .get()
                    .addOnSuccessListener { doc ->
                        medicoAceito = Medico(
                            uid = medicoId,
                            nome = doc.getString("nome") ?: "Sem nome",
                            crm = doc.getString("crm") ?: "",
                            especialidades = listOf()
                        )
                    }
            }
    }

    // BUSCAR MÉDICOS
    fun buscarMedicos() {
        loadingMedicos = true
        medicos = emptyList()

        val medicoAtual = medicoAceito?.uid  // <<=== médico já vinculado

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

                    // NÃO EXIBIR SE FOR O MÉDICO ATUAL
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
    }

        // ENVIAR SOLICITAÇÃO PARA O MÉDICO
    fun enviarSolicitacao(medicoId: String, pacienteId: String) {
        val db = FirebaseFirestore.getInstance()

        val dados = hashMapOf(
            "pacienteId" to pacienteId,
            "medicoId" to medicoId,
            "status" to "pendente",
            "timestamp" to com.google.firebase.Timestamp.now()
        )

        db.collection("solicitacoes")
            .add(dados)
    }

    fun cancelarSolicitacao() {
        val id = solicitacaoId ?: return

        db.collection("solicitacoes")
            .document(id)
            .update("status", "cancelado")
            .addOnSuccessListener {
                medicoPendenteId = null
                solicitacaoId = null
            }
    }

    fun cancelarAtendimento() {
        val uidPaciente = FirebaseAuth.getInstance().currentUser!!.uid

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
                title = {},
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

            // TÍTULO EM VERDE
            Text(
                "Escolher Médico",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = teal
            )

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
                            color = Color(0xFF007C7A)
                        )
                    }
                }
                Button(
                    onClick = { cancelarAtendimento() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Encerrar atendimento", color = Color.White)
                }
            }

            // LINHA SUTIL
            Divider(color = teal.copy(alpha = 0.2f), thickness = 1.dp)

            // DROPDOWN
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

            // LISTA DE MÉDICOS
            if (especialidadeSelecionada.isNotEmpty()) {

                Text(
                    "Médicos disponíveis (${medicos.size}):",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = teal
                )

                if (medicoPendenteId != null) {
                    Button(
                        onClick = { cancelarSolicitacao() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Cancelar solicitação", color = Color.White)
                    }
                }

                if (loadingMedicos) {
                    CircularProgressIndicator(color = teal)
                }

                if (medicos.isEmpty() && !loadingMedicos) {
                    Text("Nenhum médico encontrado.", color = Color.Gray)
                }

                medicos.forEach { medico ->

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

                            // AVATAR + NOME + CRM
                            Row(verticalAlignment = Alignment.CenterVertically) {

                                // AVATAR COM INICIAIS
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
                                            modifier = Modifier
                                                .size(48.dp),
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

                            // BOTÃO
                            val pendente = medicoPendenteId == medico.uid

                            Button(
                                onClick = {
                                    enviarSolicitacao(
                                        medicoId = medico.uid,
                                        pacienteId = FirebaseAuth.getInstance().currentUser!!.uid
                                    )
                                    medicoPendenteId = medico.uid
                                    medicoSelecionado = medico
                                    showDialog = true
                                },
                                enabled = medicoAceito == null && !pendente && medicoPendenteId == null, // desativa se já tem pendente
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (pendente) Color.Gray else teal
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
        // Exibe o popup SOMENTE se showDialog = true e se existe um médico selecionado
        if (showDialog && medicoSelecionado != null) {
            AlertDialog(
                // Quando clicar fora ou no botão OK → fecha o popup
                onDismissRequest = { showDialog = false },
                // Deixa o popup com bordas arredondadas bonitinhas
                shape = RoundedCornerShape(20.dp),
                // Cor de fundo branca (fica igual ao padrão do app)
                containerColor = Color.White,
                // Sombra suave para deixar o popup flutuando (efeito elegante)
                tonalElevation = 6.dp,

                // TÍTULO do popup
                title = {
                    Text(
                        "Solicitação enviada!",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = teal
                    )
                },

                // TEXTO explicando sobre a confirmação do médico
                text = {
                    Text(
                        "Aguarde a confirmação do médico ${medicoSelecionado!!.nome}.",
                        fontSize = 16.sp,
                        color = Color.DarkGray
                    )
                },

                // BOTÃO de confirmação (apenas "OK")
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

