package com.example.diceroller

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.*

// ── Settings Screen ───────────────────────────────────────────────────────────

@Composable
fun SettingsScreen(
    viewModel: DiceViewModel,
    onNavigateBack: () -> Unit
) {
    val dark = viewModel.isDarkTheme

    // Simulate local state
    var simCount         by remember { mutableStateOf("") }
    var simDice          by remember { mutableStateOf("D6") }
    var simDiceExpanded  by remember { mutableStateOf(false) }

    // Modal state
    var showDeleteOneDialog by remember { mutableStateOf(false) }

    // "Cancella una memoria" modal
    if (showDeleteOneDialog) {
        DeleteOneDiceDialog(
            dark      = dark,
            onDismiss = { showDeleteOneDialog = false },
            onConfirm = { dice ->
                viewModel.clearOne(dice)
                showDeleteOneDialog = false
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor(dark))
            .verticalScroll(rememberScrollState())
    ) {
        // ── Top bar ──────────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector     = Icons.Default.ArrowBack,
                    contentDescription = "Indietro",
                    tint            = textColor(dark)
                )
            }
            Text(
                text       = "Impostazioni",
                color      = textColor(dark),
                fontSize   = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // ── 1. Memory toggle ─────────────────────────────────────────────────
        SettingsCard(dark = dark) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text       = "Abilita memoria",
                        color      = textColor(dark),
                        fontWeight = FontWeight.SemiBold,
                        fontSize   = 16.sp
                    )
                    Text(
                        text     = "Memorizza i tiri anche alla chiusura dell'app",
                        color    = subtextColor(dark),
                        fontSize = 12.sp
                    )
                }
                Switch(
                    checked         = viewModel.isMemoryEnabled,
                    onCheckedChange = { viewModel.toggleMemory(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = accentColor,
                        checkedTrackColor = accentColor.copy(alpha = 0.5f)
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // ── 2. Theme ─────────────────────────────────────────────────────────
        SettingsCard(dark = dark) {
            Text(
                text       = "Tema",
                color      = textColor(dark),
                fontWeight = FontWeight.SemiBold,
                fontSize   = 16.sp
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ThemeOptionButton(
                    label    = "Scuro",
                    selected = dark,
                    dark     = dark,
                    modifier = Modifier.weight(1f),
                    onClick  = { viewModel.toggleTheme(true) }
                )
                ThemeOptionButton(
                    label    = "Chiaro",
                    selected = !dark,
                    dark     = dark,
                    modifier = Modifier.weight(1f),
                    onClick  = { viewModel.toggleTheme(false) }
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // ── 3. Simulate ──────────────────────────────────────────────────────
        SettingsCard(dark = dark) {
            Text(
                text       = "Simula",
                color      = textColor(dark),
                fontWeight = FontWeight.SemiBold,
                fontSize   = 16.sp
            )
            Text(
                text     = "Genera tiri casuali per costruire statistiche più accurate",
                color    = subtextColor(dark),
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment     = Alignment.CenterVertically
            ) {
                // Number of simulated rolls
                OutlinedTextField(
                    value          = simCount,
                    onValueChange  = { simCount = it.filter(Char::isDigit).take(6) },
                    placeholder    = {
                        Text("Quantità", color = subtextColor(dark), fontSize = 14.sp)
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine      = true,
                    modifier        = Modifier.weight(1f),
                    colors          = TextFieldDefaults.outlinedTextFieldColors(
                        textColor            = textColor(dark),
                        cursorColor          = accentColor,
                        focusedBorderColor   = accentColor,
                        unfocusedBorderColor = subtextColor(dark),
                        backgroundColor      = Color.Transparent
                    )
                )

                // Dice type picker
                Box {
                    Button(
                        onClick = { simDiceExpanded = true },
                        colors  = ButtonDefaults.buttonColors(
                            backgroundColor = if (dark) Color(0xFF252545) else Color(0xFFE0E0FF),
                            contentColor    = textColor(dark)
                        ),
                        shape     = RoundedCornerShape(8.dp),
                        elevation = ButtonDefaults.elevation(2.dp)
                    ) {
                        Text(simDice, fontWeight = FontWeight.Bold)
                    }
                    DropdownMenu(
                        expanded          = simDiceExpanded,
                        onDismissRequest  = { simDiceExpanded = false }
                    ) {
                        DiceRepository.DICE_TYPES.forEach { opt ->
                            DropdownMenuItem(onClick = {
                                simDice         = opt
                                simDiceExpanded = false
                            }) {
                                Text(opt)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Run simulation button
            val canSimulate = (simCount.toIntOrNull() ?: 0) > 0
            Button(
                onClick = {
                    val n = simCount.toIntOrNull() ?: 0
                    if (n > 0) {
                        viewModel.simulate(simDice, n)
                        simCount = ""
                    }
                },
                enabled  = canSimulate,
                colors   = ButtonDefaults.buttonColors(
                    backgroundColor         = accentColor,
                    contentColor            = Color.White,
                    disabledBackgroundColor = accentDisabled,
                    disabledContentColor    = Color.White.copy(alpha = 0.5f)
                ),
                shape    = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text       = "▶  Avvia simulazione",
                    fontWeight = FontWeight.SemiBold,
                    fontSize   = 15.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ── 4. Danger zone ───────────────────────────────────────────────────
        Column(
            modifier              = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalArrangement   = Arrangement.spacedBy(12.dp)
        ) {
            DangerButton(
                text     = "Cancella la memoria",
                subtitle = "Cancella tutta la memoria dei tiri",
                enabled  = viewModel.isMemoryEnabled,
                onClick  = { viewModel.clearAll() }
            )
            DangerButton(
                text     = "Cancella una memoria",
                subtitle = "Cancella la memoria di un dado",
                enabled  = viewModel.isMemoryEnabled,
                onClick  = { showDeleteOneDialog = true }
            )
        }

        Spacer(modifier = Modifier.height(36.dp))
    }
}

// ── Reusable card container ───────────────────────────────────────────────────

@Composable
fun SettingsCard(dark: Boolean, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(surfaceColor(dark))
            .padding(16.dp),
        content = content
    )
}

// ── Theme selector button ─────────────────────────────────────────────────────

@Composable
fun ThemeOptionButton(
    label: String,
    selected: Boolean,
    dark: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val bg = if (selected) accentColor else if (dark) Color(0xFF252545) else Color(0xFFE0E0FF)
    val fg = if (selected) Color.White else textColor(dark)

    Button(
        onClick   = onClick,
        modifier  = modifier,
        colors    = ButtonDefaults.buttonColors(backgroundColor = bg, contentColor = fg),
        shape     = RoundedCornerShape(10.dp),
        elevation = ButtonDefaults.elevation(if (selected) 6.dp else 2.dp)
    ) {
        Text(
            text       = label,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

// ── Red danger button ─────────────────────────────────────────────────────────

@Composable
fun DangerButton(
    text: String,
    subtitle: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val bg = if (enabled) Color(0xFFBB1122) else Color(0xFF553333)
    val fg = if (enabled) Color.White else Color(0xFF997777)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(bg)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Text(text, color = fg, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Text(subtitle, color = fg.copy(alpha = 0.75f), fontSize = 12.sp)
    }
}

// ── "Delete one dice memory" modal ────────────────────────────────────────────

@Composable
fun DeleteOneDiceDialog(
    dark: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest  = onDismiss,
        backgroundColor   = surfaceColor(dark),
        title = {
            Text(
                text       = "Cancella memoria dado",
                color      = textColor(dark),
                fontWeight = FontWeight.Bold,
                fontSize   = 18.sp
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text     = "Seleziona il dado di cui eliminare la memoria:",
                    color    = subtextColor(dark),
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                DiceRepository.DICE_TYPES.forEach { dt ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (dark) Color(0xFF1A1A2E) else Color(0xFFEEEEFF))
                            .clickable { onConfirm(dt) }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text       = dt,
                            color      = textColor(dark),
                            fontWeight = FontWeight.SemiBold,
                            fontSize   = 15.sp,
                            modifier   = Modifier.weight(1f)
                        )
                        Text(
                            text     = "✕",
                            color    = Color(0xFFCC4444),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annulla", color = accentColor, fontWeight = FontWeight.SemiBold)
            }
        }
    )
}