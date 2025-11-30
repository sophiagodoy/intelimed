/**
 * Tela de esqueceu senha
 */
package br.com.ibm.intelimed

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import br.com.ibm.intelimed.ui.theme.IntelimedTheme
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth

class ForgotPasswordActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            IntelimedTheme {
                ForgotPassword()
            }
        }
    }
}

@Composable
fun ForgotPassword() {

    val context = LocalContext.current
    var email by remember { mutableStateOf(TextFieldValue("")) }

    // Cores que a gente vai usar na tela
    val teal = Color(0xFF2FA49F)
    val white = Color.White

    // Fundo principal da tela
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(teal)
    ) {

        // Seta de voltar lá em cima
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 48.dp)
                .align(Alignment.TopStart)
                .clickable {
                    (context as? ComponentActivity)?.finish()
                },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.ArrowBack,
                contentDescription = "Voltar",
                tint = Color.White
            )
        }

        // Bloco central com logo, título e formulário
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // “Logo” em texto
            Text(
                text = "INTELIMED",
                color = white,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Subtítulo da tela
            Text(
                text = "Recuperar Senha",
                color = white.copy(alpha = 0.9f),
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(36.dp))

            // Card branco com o campo de e-mail e o botão
            Column(
                modifier = Modifier
                    .background(white, RoundedCornerShape(20.dp))
                    .padding(vertical = 32.dp, horizontal = 20.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Informe o e-mail cadastrado para receber o link de redefinição:",
                    textAlign = TextAlign.Center,
                    color = teal,
                    fontSize = 16.sp,
                    lineHeight = 22.sp,
                    modifier = Modifier.padding(bottom = 20.dp)
                )

                // Campo de e-mail
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("E-mail") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Botão que dispara o envio do link de redefinição
                Button(
                    onClick = { sendPasswordResetEmail(email.text, context) },
                    colors = ButtonDefaults.buttonColors(containerColor = teal),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text(
                        text = "Enviar link",
                        fontSize = 18.sp,
                        color = white,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Atalho pra voltar pra tela de login
                Text(
                    text = "Voltar ao Login",
                    color = teal,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.clickable {
                        val intent = android.content.Intent(context, SignInActivity::class.java)
                        context.startActivity(intent)
                    }
                )
            }
        }
    }
}

// Função que conversa com o Firebase pra mandar o e-mail de redefinição
fun sendPasswordResetEmail(email: String, context: android.content.Context) {
    if (email.isEmpty()) {
        Toast.makeText(context, "Por favor, insira um e-mail válido.", Toast.LENGTH_SHORT).show()
        return
    }

    val auth = FirebaseAuth.getInstance()
    auth.sendPasswordResetEmail(email)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(
                    context,
                    "Um link de redefinição foi enviado para $email",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                val errorMessage = task.exception?.message ?: "Erro ao enviar e-mail."
                Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
            }
        }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ForgotPasswordPreview() {
    IntelimedTheme {
        ForgotPassword()
    }
}

