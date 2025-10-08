package br.com.ibm.intelimed

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.ibm.intelimed.ui.theme.IntelimedTheme

class AuthChoiceActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            IntelimedTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AuthChoice()
                }
            }
        }
    }
}

@Composable
fun AuthChoice() {
    val backgroundColor = Color(0xFF2FA49F)
    val buttonColor = Color(0xFF0E5E5B)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            // LOGO
            Image(
                painter = painterResource(id = R.drawable.ic_logo_intelimed),
                contentDescription = "Logo Intelimed",
                modifier = Modifier
                    .size(260.dp)
                    .padding(bottom = 4.dp),
                contentScale = ContentScale.Fit
            )

            // TÍTULO
            Text(
                text = "INTELIMED",
                color = Color.White,
                fontSize = 54.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 28.dp)
            )

            // BLOCO BRANCO COM CONTEÚDO
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .shadow(12.dp, RoundedCornerShape(24.dp))
                    .background(Color.White, RoundedCornerShape(24.dp))
                    .padding(vertical = 32.dp, horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // MENSAGEM DE BOAS-VINDAS
                Text(
                    text = "Bem-vindo(a)!",
                    color = Color(0xFF0E5E5B),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    text = "Sua saúde em boas mãos.\nEscolha uma opção para continuar:",
                    color = Color(0xFF2FA49F),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    lineHeight = 24.sp,
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .padding(bottom = 32.dp)
                )

                // BOTÕES
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Button(
                        onClick = { /* TODO: Tela de login */ },
                        modifier = Modifier
                            .width(240.dp)
                            .height(55.dp)
                            .shadow(6.dp, RoundedCornerShape(40.dp)),
                        colors = ButtonDefaults.buttonColors(containerColor = buttonColor),
                        shape = RoundedCornerShape(40.dp)
                    ) {
                        Text(
                            text = "Login",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Button(
                        onClick = { /* TODO: Tela de cadastro */ },
                        modifier = Modifier
                            .width(240.dp)
                            .height(55.dp)
                            .shadow(6.dp, RoundedCornerShape(40.dp)),
                        colors = ButtonDefaults.buttonColors(containerColor = buttonColor),
                        shape = RoundedCornerShape(40.dp)
                    ) {
                        Text(
                            text = "Cadastro",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // RODAPÉ
            Text(
                text = "\nCuidando de você com tecnologia e dedicação 💚",
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 32.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AuthChoicePreview() {
    IntelimedTheme {
        AuthChoice()
    }
}
