package br.com.ibm.intelimed

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.ibm.intelimed.network.Cliente
import br.com.ibm.intelimed.ui.theme.IntelimedTheme
import kotlinx.coroutines.*

// Model simples da mensagem que vamos mostrar na tela
data class Mensagem(
    val text: String,
    val senderId: String,
    val timestamp: Long
)

// ViewModel cuida da parte “chat em si” (socket, fila de envio, lista de mensagens)
class ChatViewModel(
    private var uidAtual: String,
    private var uidOutro: String
) : ViewModel() {

    // Lista reativa que a UI observa
    val mensagens: SnapshotStateList<Mensagem> = mutableStateListOf()

    // Fila para guardar mensagens quando der erro na conexão
    private val filaEnvio = mutableListOf<Mensagem>()

    // Cliente do nosso servidor de chat
    var cliente = Cliente()
    private var conectado = false

    init {
        // Já configura o listener e tenta conectar assim que o ViewModel nasce
        configurarListener()
        conectar()
    }

    // Sempre que chegar algo do servidor, transformo em Mensagem e jogo na lista
    private fun configurarListener() {
        cliente.setListener { pedido ->
            val msg = Mensagem(
                pedido.getConteudo(),
                pedido.getUidRemetente(),
                pedido.getTimestamp()
            )

            synchronized(mensagens) {
                // Evita duplicar mensagem quando o servidor reenvia algo
                if (!mensagens.any { it.timestamp == msg.timestamp && it.senderId == msg.senderId }) {
                    mensagens.add(msg)
                    mensagens.sortBy { it.timestamp }
                }
            }
        }
    }

    // Loop de tentativa de conexão (fica tentando até conseguir)
    private fun conectar() {
        viewModelScope.launch(Dispatchers.IO) {
            while (!conectado) {
                try {
                    cliente.conectarServidor("10.0.2.2", 3000, uidAtual, uidOutro)
                    conectado = true
                    enviarFilaPendentes()
                } catch (e: Exception) {
                    // Se der erro, espero um pouco e tento de novo
                    delay(2000)
                }
            }
        }
    }

    // Se tinha mensagem pendente na fila, mando tudo assim que conectar
    private fun enviarFilaPendentes() {
        synchronized(filaEnvio) {
            filaEnvio.forEach { enviarMensagemParaServidor(it) }
            filaEnvio.clear()
        }
    }

    // Dispara uma nova mensagem (salva na lista e tenta mandar pro servidor)
    fun enviarMensagem(conteudo: String) {
        if (conteudo.isBlank()) return

        val msg = Mensagem(conteudo, uidAtual, System.currentTimeMillis())

        // Já mostro na tela mesmo antes de ter certeza que enviou
        synchronized(mensagens) {
            mensagens.add(msg)
            mensagens.sortBy { it.timestamp }
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (!conectado) conectar()
                enviarMensagemParaServidor(msg)
            } catch (e: Exception) {
                // Se der erro no envio, guardo na fila pra tentar depois
                synchronized(filaEnvio) { filaEnvio.add(msg) }
            }
        }
    }

    // Encapsulei a chamada de envio num método separado
    private fun enviarMensagemParaServidor(msg: Mensagem) {
        cliente.enviarMensagem(msg.text)
    }

    // Caso eu queira “trocar de conta” usando o mesmo ViewModel (ex: reabrir chat com outra pessoa)
    fun trocarConta(novoUidAtual: String, novoUidOutro: String) {
        viewModelScope.launch(Dispatchers.IO) {
            // Fecha a conexão antiga
            cliente.fecharConexao()

            // Cria um novo cliente zerado
            cliente = Cliente()
            uidAtual = novoUidAtual
            uidOutro = novoUidOutro
            configurarListener()

            // E conecta de novo com os novos ids
            conectar()
        }
    }

    // Quando o ViewModel morre, derrubo a conexão com o servidor
    override fun onCleared() {
        super.onCleared()
        cliente.fecharConexao()
        conectado = false
    }
}

// Activity só faz o “meio de campo” entre Intent e Compose + ViewModel
class ChatActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Pego infos que vieram da tela anterior
        val uidAtual = intent.getStringExtra("uidAtual") ?: ""
        val uidOutro = intent.getStringExtra("uidOutro") ?: ""
        val nomeOutro = intent.getStringExtra("nomeOutro") ?: "Chat"

        // Crio o ViewModel passando os dois uids
        val viewModel: ChatViewModel by viewModels {
            object : androidx.lifecycle.ViewModelProvider.Factory {
                override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                    @Suppress("UNCHECKED_CAST")
                    return ChatViewModel(uidAtual, uidOutro) as T
                }
            }
        }

        setContent {
            IntelimedTheme {
                ChatScreen(
                    uidAtual = uidAtual,
                    uidOutro = uidOutro,
                    nomeOutro = nomeOutro,
                    viewModel = viewModel
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
    viewModel: ChatViewModel
) {
    val teal = Color(0xFF007C7A)
    val mensagens = viewModel.mensagens
    var mensagemDigitada by remember { mutableStateOf("") }
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(nomeOutro, color = Color.White, fontWeight = FontWeight.Bold)
                        Text("Chat", color = Color.White.copy(0.8f), fontSize = 14.sp)
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
                        viewModel.enviarMensagem(mensagemDigitada)
                        mensagemDigitada = ""
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Para cada mensagem, mostro um “balão” alinhado à esquerda ou direita
            items(mensagens) { msg ->
                MensagemBubble(msg.text, msg.senderId == uidAtual)
            }
        }
    }
}

// Barra de digitação + botão de enviar
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
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextField(
            value = mensagem,
            onValueChange = onChange,
            placeholder = { Text("Enviar mensagem") },
            modifier = Modifier.weight(1f),
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
            Icon(Icons.Default.Send, contentDescription = "Enviar", tint = Color.White)
        }
    }
}

// Balão de mensagem (decide cor/alinhamento dependendo de quem enviou)
@Composable
fun MensagemBubble(texto: String, enviadaPeloUsuario: Boolean) {
    val teal = Color(0xFF007C7A)
    val bg = if (enviadaPeloUsuario) teal else Color(0xFFEFEFEF)
    val cor = if (enviadaPeloUsuario) Color.White else Color.Black

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (enviadaPeloUsuario) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            color = bg,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.padding(4.dp)
        ) {
            Text(
                texto,
                modifier = Modifier
                    .padding(12.dp)
                    .widthIn(max = 260.dp),
                color = cor,
                fontSize = 16.sp
            )
        }
    }
}
