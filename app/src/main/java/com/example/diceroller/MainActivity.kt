package com.example.diceroller

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.diceroller.ui.theme.DiceRollerTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DiceRollerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    DiceRollerApp()
                }
            }
        }
    }
}

@Preview
@Composable
fun DiceRollerApp() {
    var selectedDice by remember { mutableStateOf("D6") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1A1A2E)),  // sfondo scuro
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Titolo
        Text(
            text = "🎲 Dice Roller",
            color = Color(0xFFE0E0FF),
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(24.dp))

        DiceTypeDropdown(
            selected = selectedDice,
            onSelectedChange = { selectedDice = it }
        )
        Spacer(modifier = Modifier.height(40.dp))
        DiceWithButtonAndImage(diceType = selectedDice)
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun DiceTypeDropdown(
    selected: String,
    onSelectedChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val options = listOf("D4", "D6", "D8", "D10", "D12", "D20")
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        Button(
            onClick = { expanded = true },
            colors = ButtonDefaults.buttonColors(
                backgroundColor = Color(0xFF6C63FF),
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp),
            elevation = ButtonDefaults.elevation(6.dp)
        ) {
            Text(
                text = "Dado: $selected  ▾",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(onClick = {
                    onSelectedChange(option)
                    expanded = false
                }) {
                    Text(option, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

@Composable
fun DiceWithButtonAndImage(
    diceType: String,
    modifier: Modifier = Modifier
) {
    val diceRange = when (diceType) {
        "D4"  -> 1..4
        "D6"  -> 1..6
        "D8"  -> 1..8
        "D10" -> 1..10
        "D12" -> 1..12
        "D20" -> 1..20
        else  -> 1..6
    }

    var result by remember { mutableStateOf(1) }
    var isRolling by remember { mutableStateOf(false) }

    // Animazioni
    val rotation = remember { Animatable(0f) }
    val scale = remember { Animatable(1f) }
    val coroutineScope = rememberCoroutineScope()

    val imageResource = when (diceType) {
        "D4" -> when (result) {
            1 -> R.drawable.d4_1; 2 -> R.drawable.d4_2
            3 -> R.drawable.d4_3; else -> R.drawable.d4_4
        }
        "D6" -> when (result) {
            1 -> R.drawable.d6_01; 2 -> R.drawable.d6_02; 3 -> R.drawable.d6_03
            4 -> R.drawable.d6_04; 5 -> R.drawable.d6_05; else -> R.drawable.d6_06
        }
        "D8" -> when (result) {
            1 -> R.drawable.d8_01; 2 -> R.drawable.d8_02; 3 -> R.drawable.d8_03
            4 -> R.drawable.d8_04; 5 -> R.drawable.d8_05; 6 -> R.drawable.d8_06
            7 -> R.drawable.d8_07; else -> R.drawable.d8_08
        }
        "D10" -> when (result) {
            1 -> R.drawable.d10_01; 2 -> R.drawable.d10_02; 3 -> R.drawable.d10_03
            4 -> R.drawable.d10_04; 5 -> R.drawable.d10_05; 6 -> R.drawable.d10_06
            7 -> R.drawable.d10_07; 8 -> R.drawable.d10_08; 9 -> R.drawable.d10_09
            else -> R.drawable.d10_10
        }
        "D12" -> when (result) {
            1 -> R.drawable.d12_01; 2 -> R.drawable.d12_02; 3 -> R.drawable.d12_03
            4 -> R.drawable.d12_04; 5 -> R.drawable.d12_05; 6 -> R.drawable.d12_06
            7 -> R.drawable.d12_07; 8 -> R.drawable.d12_08; 9 -> R.drawable.d12_09
            10 -> R.drawable.d12_10; 11 -> R.drawable.d12_11; else -> R.drawable.d12_12
        }
        "D20" -> when (result) {
            1 -> R.drawable.d20_01; 2 -> R.drawable.d20_02; 3 -> R.drawable.d20_03
            4 -> R.drawable.d20_04; 5 -> R.drawable.d20_05; 6 -> R.drawable.d20_06
            7 -> R.drawable.d20_07; 8 -> R.drawable.d20_08; 9 -> R.drawable.d20_09
            10 -> R.drawable.d20_10; 11 -> R.drawable.d20_11; 12 -> R.drawable.d20_12
            13 -> R.drawable.d20_13; 14 -> R.drawable.d20_14; 15 -> R.drawable.d20_15
            16 -> R.drawable.d20_16; 17 -> R.drawable.d20_17; 18 -> R.drawable.d20_18
            19 -> R.drawable.d20_19; else -> R.drawable.d20_20
        }
        else -> R.drawable.d6_01
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Card con l'immagine del dado
        Box(
            modifier = Modifier
                .size(200.dp)
                .shadow(12.dp, RoundedCornerShape(24.dp))
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFF16213E)),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = imageResource),
                contentDescription = "$diceType - $result",
                modifier = Modifier
                    .size(150.dp)
                    .graphicsLayer {
                        rotationZ = rotation.value
                        scaleX = scale.value
                        scaleY = scale.value
                    }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Badge con il numero risultante
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(Color(0xFF6C63FF)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$result",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Bottone Roll
        Button(
            onClick = {
                if (!isRolling) {
                    coroutineScope.launch {
                        isRolling = true

                        // Spin + bounce in parallelo
                        launch {
                            rotation.animateTo(
                                targetValue = rotation.value + 720f,
                                animationSpec = tween(
                                    durationMillis = 700,
                                    easing = FastOutSlowInEasing
                                )
                            )
                        }
                        launch {
                            scale.animateTo(1.25f, animationSpec = tween(200))
                            scale.animateTo(0.9f, animationSpec = tween(150))
                            scale.animateTo(1f, animationSpec = tween(150))
                        }

                        // Aggiorna il risultato a metà animazione
                        delay(350)
                        result = diceRange.random()

                        isRolling = false
                    }
                }
            },
            enabled = !isRolling,
            colors = ButtonDefaults.buttonColors(
                backgroundColor = Color(0xFF6C63FF),
                contentColor = Color.White,
                disabledBackgroundColor = Color(0xFF4A4580)
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .height(52.dp)
                .width(160.dp),
            elevation = ButtonDefaults.elevation(8.dp)
        ) {
            Text(
                text = if (isRolling) "..." else stringResource(R.string.roll),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}