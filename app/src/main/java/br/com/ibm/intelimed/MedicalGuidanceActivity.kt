package br.com.ibm.intelimed

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import br.com.ibm.intelimed.ui.theme.IntelimedTheme

class MedicalGuidanceActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            IntelimedTheme {
                OrientacoesMedicasScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrientacoesMedicasScreen() {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Voltar",
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("INTELIMED", color = Color.White, fontSize = 20.sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF007C7A)
                )
            )
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
                text = "Orientações médicas",
                fontSize = 22.sp,
                color = Color.Black
            )

            val orientacoes = listOf(
                "Mantenha uma alimentação saudável",
                "Evite atividades físicas intensas",
                "Tome a medicação conforme prescrito",
                "Faça repouso quando necessário",
                "Use a pomada na ferida duas vezes ao dia"
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                orientacoes.forEach { item ->
                    Row(verticalAlignment = Alignment.Top) {
                        Text("• ", fontSize = 18.sp)
                        Text(item, fontSize = 16.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            var detalhes by remember { mutableStateOf(TextFieldValue("")) }

            OutlinedTextField(
                value = detalhes,
                onValueChange = { detalhes = it },
                label = { Text("Detalhar situação") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            )

            Button(
                onClick = { /* Ação para enviar detalhes */ },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007C7A)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Enviar detalhes")
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun OrientacoesMedicasPreview() {
    IntelimedTheme {
        OrientacoesMedicasScreen()
    }
}
