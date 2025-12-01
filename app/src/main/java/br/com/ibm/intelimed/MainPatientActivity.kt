// Tela principal da home do paciente
package br.com.ibm.intelimed

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.ibm.intelimed.ui.theme.IntelimedTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

class MainPatientActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            IntelimedTheme {
                PatientHome()
            }
        }
    }
}

// Busca o nome do usuário logado (primeiro tenta em "paciente", depois em "medico")
fun buscarNomeUsuario(onResult: (String) -> Unit) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val currentUser = auth.currentUser ?: return
    val uid = currentUser.uid

    db.collection("paciente").document(uid).get()
        .addOnSuccessListener { doc ->
            if (doc.exists()) {
                val nome = doc.getString("nome") ?: "Paciente"
                onResult(nome.split(" ").firstOrNull() ?: nome)
            } else {
                db.collection("medico").document(uid).get()
                    .addOnSuccessListener { medicoDoc ->
                        val nome = medicoDoc.getString("nome") ?: "Médico"
                        onResult(nome.split(" ").firstOrNull() ?: nome)
                    }
                    .addOnFailureListener { onResult("Usuário") }
            }
        }
        .addOnFailureListener {
            onResult("Usuário")
        }
}

// Busca o médico atualmente aceito para o paciente (se existir)
// Se não tiver nenhum com status "aceito", devolve null
fun buscarMedicoVinculado(onResult: (String?) -> Unit) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val currentUser = auth.currentUser
    val uid = currentUser?.uid

    if (uid == null) {
        onResult(null)
        return
    }

    db.collection("solicitacoes")
        .whereEqualTo("pacienteId", uid)
        .whereEqualTo("status", "aceito")
        .limit(1)
        .get()
        .addOnSuccessListener { snapshot ->
            if (snapshot.isEmpty) {
                onResult(null)
            } else {
                val medicoId = snapshot.documents.first().getString("medicoId")
                onResult(medicoId)
            }
        }
        .addOnFailureListener {
            onResult(null)
        }
}

// Verifica se o paciente pode registrar sintomas agora
// Regra:
// - pega o último registro em paciente/{id}/sintomas, ordenando por dataRegistro desc
// - se nunca registrou, pode
// - se registrou:
//      * se foi para OUTRO médico pode registrar de novo
//      * se foi para o MESMO médico só pode se já passaram 24h
fun verificarPodeRegistrarSintomasHoje(
    medicoAtualId: String,
    onResult: (Boolean) -> Unit
) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val currentUser = auth.currentUser
    val uid = currentUser?.uid

    if (uid == null) {
        onResult(false)
        return
    }

    db.collection("paciente")
        .document(uid)
        .collection("sintomas")
        .orderBy("dataRegistro", Query.Direction.DESCENDING)
        .limit(1)
        .get()
        .addOnSuccessListener { snapshot ->
            // nunca registrou nada ainda
            if (snapshot.isEmpty) {
                onResult(true)
                return@addOnSuccessListener
            }

            val doc = snapshot.documents.first()
            val ultimoRegistro = doc.getLong("dataRegistro") ?: 0L
            val medicoDoUltimoRegistro = doc.getString("medicoId")

            val agora = System.currentTimeMillis()
            val vinteQuatroHorasEmMillis = 24L * 60L * 60L * 1000L

            // se o último registro foi para outro médico, libero
            if (medicoDoUltimoRegistro != null && medicoDoUltimoRegistro != medicoAtualId) {
                onResult(true)
                return@addOnSuccessListener
            }

            val podeRegistrar = (agora - ultimoRegistro) >= vinteQuatroHorasEmMillis
            onResult(podeRegistrar)
        }
        .addOnFailureListener {
            // se deu erro na consulta, por segurança NÃO libero
            onResult(false)
        }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientHome() {
    val teal = Color(0xFF007C7A)
    val context = LocalContext.current
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current

    // Nome exibido na saudação
    var nome by remember { mutableStateOf("Usuário") }

    // Situação do vínculo com médico
    var temMedicoVinculado by remember { mutableStateOf(false) }
    var carregandoVinculoMedico by remember { mutableStateOf(true) }
    var medicoAtualId by remember { mutableStateOf<String?>(null) }

    // Diálogos
    var mostrarDialogoSair by remember { mutableStateOf(false) }
    var mostrarDialogoPrecisaMedico by remember { mutableStateOf(false) }
    var mostrarDialogoLimite24h by remember { mutableStateOf(false) }

    // Carrega nome e médico vinculado na primeira vez que entra na tela
    LaunchedEffect(Unit) {
        buscarNomeUsuario { resultado ->
            nome = resultado
        }

        buscarMedicoVinculado { medicoId ->
            medicoAtualId = medicoId
            temMedicoVinculado = medicoId != null
            carregandoVinculoMedico = false
        }
    }

    // Sempre que a Activity volta para RESUMED, revalida o médico vinculado
    // Isso garante que cancelar um médico ou trocar de médico,
    // a home reflete esse estado corretamente
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                carregandoVinculoMedico = true
                buscarMedicoVinculado { medicoId ->
                    medicoAtualId = medicoId
                    temMedicoVinculado = medicoId != null
                    carregandoVinculoMedico = false
                }
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "INTELIMED",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = teal),
            )
        },
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                NavigationBarItem(
                    selected = true,
                    onClick = {}, // no momento só essa aba
                    icon = { Icon(Icons.Default.Home, contentDescription = null) },
                    label = { Text("Início") }
                )

                NavigationBarItem(
                    selected = false,
                    onClick = { mostrarDialogoSair = true },
                    icon = { Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Sair") },
                    label = { Text("Sair") }
                )
            }
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .padding(20.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Text(
                text = "Home",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Text(
                text = "Olá, $nome",
                fontSize = 18.sp,
                color = Color.DarkGray
            )

            // Primeira linha de cards: Chat / Escolher Médico
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OptionCard("Chat", Icons.AutoMirrored.Filled.Chat) {
                    context.startActivity(
                        Intent(context, AcceptedChatsActivity::class.java)
                            .putExtra("isMedico", false)
                    )
                }

                OptionCard("Escolher Médico", Icons.Default.Description) {
                    val intent = Intent(context, SelectDoctorActivity::class.java)
                    context.startActivity(intent)
                }
            }

            // Segunda linha de cards: Orientação fixa / Feedback médico
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OptionCard("Orientação Fixa", Icons.AutoMirrored.Filled.ArrowForward) {
                    val intent = Intent(context, MedicalGuidanceActivity::class.java)
                    context.startActivity(intent)
                }

                OptionCard("Feedback Médico", Icons.Default.Warning) {
                    val intent = Intent(context, PatientFeedbackListActivity::class.java)
                    context.startActivity(intent)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Botão principal:
            // - precisa ter médico vinculado
            // - e respeita a regra de 24h por médico
            Button(
                onClick = {
                    // evita clique enquanto ainda está consultando o Firestore
                    if (carregandoVinculoMedico) return@Button

                    val medicoId = medicoAtualId

                    if (!temMedicoVinculado || medicoId == null) {
                        // não tem médico aceito: obriga escolher um primeiro
                        mostrarDialogoPrecisaMedico = true
                    } else {
                        // tem médico aceito: agora checa a regra de 24h para ESSE médico
                        verificarPodeRegistrarSintomasHoje(medicoId) { podeRegistrar ->
                            if (podeRegistrar) {
                                val intent = Intent(context, SymptomLogActivity::class.java)
                                context.startActivity(intent)
                            } else {
                                mostrarDialogoLimite24h = true
                            }
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = teal),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Registrar sintomas hoje", color = Color.White, fontSize = 16.sp)
            }

            // Mensagem de orientação quando já sabemos que não há médico vinculado
            if (!carregandoVinculoMedico && !temMedicoVinculado) {
                Text(
                    text = "Para registrar seus sintomas, primeiro escolha um médico para te acompanhar.",
                    fontSize = 13.sp,
                    color = Color.Gray
                )
            }
        }

        // Diálogo de confirmação de logout
        if (mostrarDialogoSair) {
            AlertDialog(
                onDismissRequest = { mostrarDialogoSair = false },
                title = { Text("Confirmar saída") },
                text = { Text("Deseja realmente sair da sua conta?") },
                confirmButton = {
                    TextButton(onClick = {
                        mostrarDialogoSair = false
                        val intent = Intent(context, AuthChoiceActivity::class.java)
                        intent.addFlags(
                            Intent.FLAG_ACTIVITY_CLEAR_TOP or
                                    Intent.FLAG_ACTIVITY_NEW_TASK
                        )
                        context.startActivity(intent)
                    }) {
                        Text("Sim", color = teal)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { mostrarDialogoSair = false }) {
                        Text("Não", color = Color.Gray)
                    }
                }
            )
        }

        // Diálogo pedindo para escolher médico antes de registrar sintomas
        if (mostrarDialogoPrecisaMedico) {
            AlertDialog(
                onDismissRequest = { mostrarDialogoPrecisaMedico = false },
                title = { Text("Escolha um médico primeiro") },
                text = {
                    Text(
                        "Para registrar seus sintomas, você precisa primeiro escolher um médico " +
                                "que ficará responsável pelo seu acompanhamento."
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        mostrarDialogoPrecisaMedico = false
                        val intent = Intent(context, SelectDoctorActivity::class.java)
                        context.startActivity(intent)
                    }) {
                        Text("Escolher médico", color = teal)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { mostrarDialogoPrecisaMedico = false }) {
                        Text("Agora não", color = Color.Gray)
                    }
                }
            )
        }

        // Diálogo quando já registrou para o mesmo médico nas últimas 24h
        if (mostrarDialogoLimite24h) {
            AlertDialog(
                onDismissRequest = { mostrarDialogoLimite24h = false },
                title = { Text("Registro diário já enviado") },
                text = {
                    Text(
                        "Você já registrou seus sintomas para este médico nas últimas 24 horas. " +
                                "Você poderá enviar um novo registro depois desse prazo."
                    )
                },
                confirmButton = {
                    TextButton(onClick = { mostrarDialogoLimite24h = false }) {
                        Text("OK", color = teal)
                    }
                }
            )
        }
    }
}

// Card de opção usado na Home (Chat, Escolher Médico, etc)
@Composable
fun RowScope.OptionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    val teal = Color(0xFF007C7A)
    val cardBg = Color(0xFFF7FDFC)

    Card(
        modifier = Modifier
            .weight(1f)
            .height(100.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(12.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = teal, modifier = Modifier.size(30.dp))
            Text(title, color = Color.Black, fontSize = 14.sp)
        }
    }
}

@Preview
@Composable
fun MainPatientActivityPreview() {
    IntelimedTheme {
        PatientHome()
    }
}
