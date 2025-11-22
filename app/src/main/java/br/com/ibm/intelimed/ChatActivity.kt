package br.com.ibm.intelimed

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.ibm.intelimed.network.Cliente
import br.com.ibm.intelimed.network.PedidoMensagem
import br.com.ibm.intelimed.ui.theme.IntelimedTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

data class Mensagem(
    val text: String,
    val senderId: String
)

class ChatActivity : ComponentActivity() {

    private val cliente = Cliente()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val uidAtual = intent.getStringExtra("uidAtual") ?: ""
        val uidOutro = intent.getStringExtra("uidOutro") ?: ""
        val nomeOutro = intent.getStringExtra("nomeOutro") ?: "Chat"
        val mensagens = mutableStateListOf<Mensagem>()

        // Conecta ao servidor em background
        CoroutineScope(Dispatchers.IO).launch {
            try {
                cliente.conectarServidor("10.0.2.2", 3000, uidAtual)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Define listener para receber mensagens do servidor
        cliente.setListener { pedido ->
            val msg = Mensagem(
                text = pedido.getConteudo(),
                senderId = pedido.getUidRemetente()
            )
            runOnUiThread {
                mensagens.add(msg)
            }
        }

        setContent {
            IntelimedTheme {
                ChatScreen(
                    uidAtual = uidAtual,
                    uidOutro = uidOutro,
                    nomeOutro = nomeOutro,
                    cliente = cliente,
                    mensagens = mensagens
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    uidAtual: String,
    uidOutro: String,
    nomeOutro: String,
    cliente: Cliente,
    mensagens: SnapshotStateList<Mensagem>
) {
    val teal = Color(0xFF007C7A)
    var mensagemDigitada by remember { mutableStateOf("") }
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            nomeOutro,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Chat",
                            color = Color.White.copy(0.8f),
                            fontSize = 14.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { (context as? ComponentActivity)?.finish() }) {
                        Icon(
                            Icons.Filled.ArrowBackIosNew,
                            contentDescription = "Voltar",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = teal)
            )
        },
        bottomBar = {
            ChatInputBar(
                mensagem = mensagemDigitada,
                onChange = { mensagemDigitada = it },
                onSend = {
                    if (mensagemDigitada.isNotBlank()) {
                        // Envia ao servidor em background
                        CoroutineScope(Dispatchers.IO).launch {
                            try {
                                cliente.enviarMensagem(mensagemDigitada, uidOutro)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }

                        // Adiciona localmente
                        mensagens.add(Mensagem(text = mensagemDigitada, senderId = uidAtual))
                        mensagemDigitada = ""
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(mensagens) { msg ->
                    MensagemBubble(
                        texto = msg.text,
                        enviadaPeloUsuario = msg.senderId == uidAtual
                    )
                }
            }
        }
    }
}

@Composable
fun ChatInputBar(
    mensagem: String,
    onChange: (String) -> Unit,
    onSend: () -> Unit
) {
    val teal = Color(0xFF007C7A)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextField(
            value = mensagem,
            onValueChange = onChange,
            placeholder = { Text("Enviar mensagem") },
            modifier = Modifier
                .weight(1f)
                .height(50.dp),
            shape = RoundedCornerShape(25.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color(0xFFF7FDFC),
                unfocusedContainerColor = Color(0xFFF7FDFC),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
            )
        )

        Spacer(modifier = Modifier.width(8.dp))

        IconButton(
            onClick = onSend,
            modifier = Modifier
                .size(45.dp)
                .background(teal, CircleShape)
        ) {
            Icon(
                Icons.Default.Send,
                contentDescription = "Enviar",
                tint = Color.White
            )
        }
    }
}

@Composable
fun MensagemBubble(texto: String, enviadaPeloUsuario: Boolean) {
    val teal = Color(0xFF007C7A)
    val bg = if (enviadaPeloUsuario) teal else Color(0xFFEFEFEF)
    val textoCor = if (enviadaPeloUsuario) Color.White else Color.Black

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (enviadaPeloUsuario) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .background(bg, RoundedCornerShape(16.dp))
                .padding(12.dp)
                .widthIn(max = 280.dp)
        ) {
            Text(texto, color = textoCor, fontSize = 16.sp)
        }
    }
}
