package com.example.diceroller

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ── App-wide color helpers ────────────────────────────────────────────────────

fun bgColor(dark: Boolean)      = if (dark) Color(0xFF1A1A2E) else Color(0xFFF0F0FF)
fun surfaceColor(dark: Boolean) = if (dark) Color(0xFF16213E) else Color(0xFFFFFFFF)
fun textColor(dark: Boolean)    = if (dark) Color(0xFFE0E0FF) else Color(0xFF1A1A2E)
fun subtextColor(dark: Boolean) = if (dark) Color(0xFF8888AA) else Color(0xFF666688)

val accentColor   = Color(0xFF6C63FF)
val accentDisabled = Color(0xFF4A4580)
val chartBarColor = Color(0xFF00EE88)

// ── Main Screen ───────────────────────────────────────────────────────────────

@Composable
fun MainScreen(
    viewModel: DiceViewModel,
    onNavigateToSettings: () -> Unit
) {
    val dark = viewModel.isDarkTheme
    var selectedDice by remember { mutableStateOf("D6") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor(dark))
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ── Top bar ──────────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "🎲 Dice Roller",
                color = textColor(dark),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onNavigateToSettings) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Impostazioni",
                    tint = textColor(dark)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // ── Dice selector ────────────────────────────────────────────────────
        DiceTypeDropdownMain(
            selected = selectedDice,
            onSelectedChange = { selectedDice = it },
            dark = dark
        )

        Spacer(modifier = Modifier.height(32.dp))

        // ── Dice image + Roll button ──────────────────────────────────────────
        DiceWithButtonAndImageMain(
            diceType = selectedDice,
            dark = dark,
            onRoll = { value -> viewModel.recordRoll(selectedDice, value) }
        )

        Spacer(modifier = Modifier.height(32.dp))

        // ── Statistics chart ─────────────────────────────────────────────────
        DiceHistorySection(
            history = viewModel.historyFor(selectedDice),
            diceType = selectedDice,
            dark = dark
        )

        Spacer(modifier = Modifier.height(28.dp))
    }
}

// ── Dice type dropdown ────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun DiceTypeDropdownMain(
    selected: String,
    onSelectedChange: (String) -> Unit,
    dark: Boolean,
    modifier: Modifier = Modifier
) {
    val options = DiceRepository.DICE_TYPES
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        Button(
            onClick = { expanded = true },
            colors = ButtonDefaults.buttonColors(
                backgroundColor = accentColor,
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
            options.forEach { opt ->
                DropdownMenuItem(onClick = {
                    onSelectedChange(opt)
                    expanded = false
                }) {
                    Text(opt, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

// ── Dice image + animation + roll button ──────────────────────────────────────

@Composable
fun DiceWithButtonAndImageMain(
    diceType: String,
    dark: Boolean,
    onRoll: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val diceRange = DiceViewModel.diceRange(diceType)

    // Reset result display when dice type changes
    var result    by remember(diceType) { mutableStateOf(1) }
    var isRolling by remember { mutableStateOf(false) }

    val rotation = remember { Animatable(0f) }
    val scale    = remember { Animatable(1f) }
    val scope    = rememberCoroutineScope()

    val imageResource = imageForDice(diceType, result)

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Dice face card
        Box(
            modifier = Modifier
                .size(200.dp)
                .shadow(12.dp, RoundedCornerShape(24.dp))
                .clip(RoundedCornerShape(24.dp))
                .background(surfaceColor(dark)),
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.foundation.Image(
                painter = painterResource(id = imageResource),
                contentDescription = "$diceType - $result",
                modifier = Modifier
                    .size(150.dp)
                    .graphicsLayer {
                        rotationZ = rotation.value
                        scaleX    = scale.value
                        scaleY    = scale.value
                    }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Result badge
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(accentColor),
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

        // Roll button
        Button(
            onClick = {
                if (!isRolling) {
                    scope.launch {
                        isRolling = true

                        val rotJob = launch {
                            rotation.animateTo(
                                rotation.value + 720f,
                                animationSpec = tween(700, easing = FastOutSlowInEasing)
                            )
                            rotation.snapTo(rotation.value % 360f)
                        }
                        val scaleJob = launch {
                            scale.animateTo(1.25f, animationSpec = tween(200))
                            scale.animateTo(0.9f,  animationSpec = tween(150))
                            scale.animateTo(1f,    animationSpec = tween(150))
                        }

                        delay(350)
                        result = diceRange.random()
                        onRoll(result)

                        rotJob.join()
                        scaleJob.join()
                        isRolling = false
                    }
                }
            },
            enabled = !isRolling,
            colors = ButtonDefaults.buttonColors(
                backgroundColor        = accentColor,
                contentColor           = Color.White,
                disabledBackgroundColor = accentDisabled
            ),
            shape    = RoundedCornerShape(16.dp),
            modifier = Modifier
                .height(52.dp)
                .width(160.dp),
            elevation = ButtonDefaults.elevation(8.dp)
        ) {
            Text(
                text     = if (isRolling) "..." else stringResource(R.string.roll),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// ── History / chart section ───────────────────────────────────────────────────

@Composable
fun DiceHistorySection(
    history: Map<Int, Int>,
    diceType: String,
    dark: Boolean
) {
    var expanded by remember { mutableStateOf(true) }
    val totalRolls = history.values.sum()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        // Collapsible header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(
                    if (expanded) RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
                    else RoundedCornerShape(12.dp)
                )
                .background(surfaceColor(dark))
                .clickable { expanded = !expanded }
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text       = "📊 Statistiche $diceType",
                color      = textColor(dark),
                fontWeight = FontWeight.SemiBold,
                fontSize   = 15.sp,
                modifier   = Modifier.weight(1f)
            )
            Text(
                text     = "$totalRolls tiri  ${if (expanded) "▲" else "▼"}",
                color    = subtextColor(dark),
                fontSize = 13.sp
            )
        }

        if (expanded) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp))
                    .background(surfaceColor(dark))
                    .padding(horizontal = 12.dp, vertical = 16.dp)
            ) {
                if (history.isEmpty()) {
                    Text(
                        text     = "Nessun tiro ancora. Inizia a lanciare! 🎲",
                        color    = subtextColor(dark),
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        modifier  = Modifier.fillMaxWidth()
                    )
                } else {
                    DiceBarChart(
                        history   = history,
                        diceType  = diceType,
                        dark      = dark
                    )
                }
            }
        }
    }
}

@Composable
fun DiceBarChart(
    history: Map<Int, Int>,
    diceType: String,
    dark: Boolean
) {
    val maxFace  = DiceViewModel.diceMaxFace(diceType)
    val maxCount = history.values.maxOrNull() ?: 1

    Column(modifier = Modifier.fillMaxWidth()) {
        // Bar chart drawn via Canvas
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp)
        ) {
            val w      = size.width
            val h      = size.height
            val slotW  = w / maxFace
            val barW   = slotW * 0.55f
            val axisY  = h - 2.dp.toPx()
            val radius = minOf(barW / 2f, 6.dp.toPx())

            // Axis line
            drawLine(
                color       = if (dark) Color(0xFF334466) else Color(0xFFCCCCEE),
                start       = Offset(0f, axisY),
                end         = Offset(w, axisY),
                strokeWidth = 1.5f
            )

            (1..maxFace).forEach { face ->
                val count = history[face] ?: 0
                val barH  = (count.toFloat() / maxCount) * (h - 10.dp.toPx())
                val x     = (face - 1) * slotW + (slotW - barW) / 2f

                if (barH > 0f) {
                    drawRoundRect(
                        color        = chartBarColor,
                        topLeft      = Offset(x, axisY - barH),
                        size         = Size(barW, barH),
                        cornerRadius = CornerRadius(radius, radius)
                    )
                }
            }
        }

        // X-axis labels
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp)
        ) {
            val fontSize = if (maxFace <= 12) 10.sp else 8.sp
            (1..maxFace).forEach { face ->
                Text(
                    text      = "$face",
                    color     = subtextColor(dark),
                    fontSize  = fontSize,
                    textAlign = TextAlign.Center,
                    modifier  = Modifier.weight(1f)
                )
            }
        }
    }
}

// ── Image resource mapper (preserved from original) ───────────────────────────

fun imageForDice(diceType: String, result: Int): Int = when (diceType) {
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