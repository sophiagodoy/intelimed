package br.com.ibm.intelimed

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.ibm.intelimed.ui.theme.IntelimedTheme

class MainPatientActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            IntelimedTheme {
                PatientHomeScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientHomeScreen() {
    val teal = Color(0xFF007C7A)
    val cardBg = Color(0xFFF7FDFC)
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("INTELIMED", color = Color.White, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = teal),
                actions = {
                    IconButton(onClick = { /* Notificações */ }) {
                        Icon(Icons.Default.Notifications, contentDescription = null, tint = Color.White)
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                NavigationBarItem(
                    selected = true,
                    onClick = {},
                    icon = { Icon(Icons.Default.Home, contentDescription = null) },
                    label = { Text("Início") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { /* perfil */ },
                    icon = { Icon(Icons.Default.Person, contentDescription = null) },
                    label = { Text("Perfil") }
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
                text = "Olá, Arthur",
                fontSize = 18.sp,
                color = Color.DarkGray
            )

            // Cards de opções
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OptionCard("Chat", Icons.Default.Chat) { /* abrir chat */ }
                OptionCard("Escolher Medico", Icons.Default.Description) { /* abrir relatórios */ }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OptionCard("Orientação Fixa", Icons.Default.ArrowForward) {
                    val intent = Intent(context, MedicalGuidanceActivity::class.java)
                    context.startActivity(intent)
                }
                OptionCard("Feedback Medico", Icons.Default.Warning) { /* abrir sintomas */ }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { /* registrar sintomas */ },
                colors = ButtonDefaults.buttonColors(containerColor = teal),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Registrar sintomas hoje", color = Color.White, fontSize = 16.sp)
            }

            Button(
                onClick = {
                    val intent = Intent(context, MedicalGuidanceActivity::class.java)
                    context.startActivity(intent)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                border = ButtonDefaults.outlinedButtonBorder,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Ver orientações médicas", color = teal, fontSize = 16.sp)
            }
        }
    }
}

@Composable
fun RowScope.OptionCard(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
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
        PatientHomeScreen()
    }
}