/**
 * Tela principal do médico
 */
package br.com.ibm.intelimed

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
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

class MainDoctorActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Deixa a tela aproveitando melhor a área (status bar, etc.)
        enableEdgeToEdge()
        setContent {
            IntelimedTheme {
                DoctorHome()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorHome() {

    val teal = Color(0xFF007C7A)
    val context = LocalContext.current

    // Controle do popup de sair
    var showLogoutDialog by remember { mutableStateOf(false) }

    // Nome que vai aparecer no "Olá, Dr. ..."
    var nome by remember { mutableStateOf("Usuário") }

    // Quando a tela entra, busca o nome do médico no Firebase
    LaunchedEffect(Unit) {
        buscarNomeUsuario { resultado ->
            nome = resultado
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
                    onClick = { /* já está na home */ },
                    icon = { Icon(Icons.Default.Home, contentDescription = null) },
                    label = { Text("Início") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { showLogoutDialog = true },
                    icon = { Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null) },
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
            // Título principal da tela
            Text(
                text = "Bem-vindo, Doutor(a)!",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            // Saudação com o nome do médico
            Text(
                text = "Olá, Dr. $nome",
                fontSize = 18.sp,
                color = Color.DarkGray
            )

            // Primeira linha de cards: Pacientes / Chat
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OptionCard("Pacientes", Icons.Default.Person) {
                    val intent = Intent(context, PatientListActivity::class.java)
                    context.startActivity(intent)
                }
                OptionCard("Chat", Icons.AutoMirrored.Filled.Chat) {
                    context.startActivity(
                        Intent(context, AcceptedChatsActivity::class.java)
                            .putExtra("isMedico", true)
                    )
                }
            }

            // Segunda linha de cards: Feedbacks / Guia de Atendimento
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OptionCard("Feedbacks", Icons.Default.ThumbUp) {
                    val intent = Intent(context, ReportsActivity::class.java)
                    context.startActivity(intent)
                }

                OptionCard("Guia de Atendimento", Icons.Default.MenuBook) {
                    val intent = Intent(context, DoctorInstructionsActivity::class.java)
                    context.startActivity(intent)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }

    // Popup pra confirmar se o médico quer realmente sair
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Confirmar saída") },
            text = { Text("Deseja realmente sair da sua conta?") },
            confirmButton = {
                TextButton(onClick = {
                    // Faz o logout no Firebase e manda para a tela de escolha de login
                    FirebaseAuth.getInstance().signOut()
                    showLogoutDialog = false
                    val intent = Intent(context, AuthChoiceActivity::class.java)
                    intent.flags =
                        Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    context.startActivity(intent)
                }) {
                    Text("Sim", color = teal)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Não", color = Color.Gray)
                }
            }
        )
    }
}

@Preview
@Composable
fun DoctorHomePreview() {
    IntelimedTheme {
        DoctorHome()
    }
}
