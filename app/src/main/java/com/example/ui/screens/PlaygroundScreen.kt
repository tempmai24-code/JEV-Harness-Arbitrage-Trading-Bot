package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.JevQuestionConfig
import com.example.data.model.QuestionType
import com.example.ui.MainViewModel
import com.example.ui.components.JsonCodeViewer
import com.example.ui.components.LatencyBenchmarkBadge
import com.example.ui.components.ProbabilityBar
import com.example.ui.components.ScoreRiskMeter
import com.example.ui.theme.*

@Composable
fun PlaygroundScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val stateText by viewModel.playgroundState.collectAsState()
    val questions by viewModel.playgroundQuestions.collectAsState()
    val result by viewModel.playgroundResult.collectAsState()
    val isRunning by viewModel.isPlaygroundRunning.collectAsState()
    val showRawJson by viewModel.showRawJson.collectAsState()

    var showAddQuestionDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(JevDarkBg)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- Header Title ---
        item {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Terminal,
                        contentDescription = null,
                        tint = JevCyanPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "JEV API WORKBENCH",
                        color = JevCyanPrimary,
                        fontWeight = FontWeight.Black,
                        fontSize = 17.sp,
                        letterSpacing = 1.sp
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Configure Unstructured State & Query 3 Primitives (Noul, Choice, Score)",
                    color = JevTextSecondary,
                    fontSize = 11.sp
                )
            }
        }

        // --- Benchmark Comparison Banner ---
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = JevDarkSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, JevBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "LATENCY ADVANTAGE", color = JevTextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        Text(text = "⚡ ~110ms", color = JevCyanPrimary, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                        Text(text = "vs ~1,600ms LLMs (15x faster)", color = JevTextSecondary, fontSize = 10.sp)
                    }
                    Divider(modifier = Modifier.height(36.dp).width(1.dp), color = JevBorder)
                    Column {
                        Text(text = "COST EFFICIENCY", color = JevTextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        Text(text = "$0.042 / 1M", color = JevEmeraldSuccess, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                        Text(text = "Outputs 100% free (400x cheaper)", color = JevTextSecondary, fontSize = 10.sp)
                    }
                }
            }
        }

        // --- Unstructured State Input ---
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = JevDarkSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, JevBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "UNSTRUCTURED STATE (CONTEXT)",
                            color = JevTextMuted,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Row {
                            Text(
                                text = "Load: ",
                                color = JevTextMuted,
                                fontSize = 10.sp
                            )
                            Text(
                                text = "Crypto",
                                color = JevCyanPrimary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .clickable {
                                        viewModel.updatePlaygroundState(
                                            "Token: \$ALPHA. Nansen Smart Money 24h Net Flow: +\$1,200,000. Arkham alert: 3 known Wintermute wallets just withdrew \$400k of \$ALPHA to a private DEX wallet. Current DEX pool Liquidity: \$3,000,000. 5-min price momentum: +14%."
                                        )
                                    }
                                    .padding(horizontal = 4.dp)
                            )
                            Text(text = "|", color = JevTextMuted, fontSize = 10.sp)
                            Text(
                                text = "Agent Bash",
                                color = JevCyanPrimary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .clickable {
                                        viewModel.updatePlaygroundState(
                                            "Agent is trying to execute: 'rm -rf /var/log/nginx' on the production server to clear space."
                                        )
                                    }
                                    .padding(horizontal = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = stateText,
                        onValueChange = { viewModel.updatePlaygroundState(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("playground_state_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = JevCyanPrimary,
                            unfocusedBorderColor = JevBorder,
                            focusedTextColor = JevTextPrimary,
                            unfocusedTextColor = JevTextPrimary,
                            focusedContainerColor = Color(0xFF060910),
                            unfocusedContainerColor = Color(0xFF060910)
                        ),
                        textStyle = LocalTextStyle.current.copy(
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 16.sp
                        ),
                        singleLine = false,
                        maxLines = 4
                    )
                }
            }
        }

        // --- Configured Questions Section ---
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TARGET QUESTIONS (${questions.size})",
                    color = JevTextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                TextButton(
                    onClick = { showAddQuestionDialog = true },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = JevCyanPrimary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Primitive", color = JevCyanPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        items(questions) { question ->
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = JevDarkSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, JevBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = when (question.type) {
                                    QuestionType.NOUL -> JevCyanPrimary.copy(alpha = 0.2f)
                                    QuestionType.CHOICE -> JevPurpleAccent.copy(alpha = 0.2f)
                                    QuestionType.SCORE -> JevGoldAccent.copy(alpha = 0.2f)
                                },
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = question.type.name.lowercase(),
                                    color = when (question.type) {
                                        QuestionType.NOUL -> JevCyanPrimary
                                        QuestionType.CHOICE -> JevPurpleAccent
                                        QuestionType.SCORE -> JevGoldAccent
                                    },
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = question.key,
                                color = JevTextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = question.instructions,
                            color = JevTextSecondary,
                            fontSize = 11.sp
                        )
                        if (question.options.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Options: ${question.options.joinToString(", ")}",
                                color = JevPurpleAccent,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    IconButton(
                        onClick = { viewModel.removePlaygroundQuestion(question.key) },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Remove", tint = JevTextMuted, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }

        // --- Run Button ---
        item {
            Button(
                onClick = { viewModel.runPlaygroundEvaluation() },
                colors = ButtonDefaults.buttonColors(containerColor = JevCyanPrimary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("run_evaluation_button")
            ) {
                if (isRunning) {
                    CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                } else {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                }
                Text(
                    text = "EVALUATE CONSTRAINED STATE",
                    color = Color.Black,
                    fontWeight = FontWeight.Black,
                    fontSize = 12.sp,
                    letterSpacing = 0.5.sp
                )
            }
        }

        // --- Results Section ---
        item {
            result?.let { eval ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = JevDarkSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, JevCyanPrimary.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "STRUCTURED JEV OUTPUT",
                                color = JevCyanPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                            LatencyBenchmarkBadge(latencyMs = eval.latencyMs)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Toggle between visual and raw JSON
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Tokens: ${eval.inputTokens} | Cost: \$${String.format("%.6f", eval.costUsd)}",
                                color = JevTextMuted,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "Raw JSON", color = JevTextSecondary, fontSize = 10.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Switch(
                                    checked = showRawJson,
                                    onCheckedChange = { viewModel.toggleRawJson(it) },
                                    modifier = Modifier.height(24.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        if (showRawJson) {
                            val rawJsonSnippet = buildString {
                                append("{\n")
                                append("  \"model\": \"${eval.model}\",\n")
                                append("  \"latency_ms\": ${eval.latencyMs},\n")
                                append("  \"results\": {\n")
                                eval.results.entries.forEachIndexed { idx, entry ->
                                    val isLast = idx == eval.results.size - 1
                                    append("    \"${entry.key}\": ")
                                    when (entry.value.type) {
                                        QuestionType.NOUL -> {
                                            val t = entry.value.booleanProbs["true"] ?: 0.5
                                            val f = entry.value.booleanProbs["false"] ?: 0.5
                                            append("{\n      \"true\": $t,\n      \"false\": $f\n    }")
                                        }
                                        QuestionType.CHOICE -> {
                                            append("{\n      \"selected\": \"${entry.value.bestChoice}\",\n      \"probs\": ${entry.value.choiceProbs}\n    }")
                                        }
                                        QuestionType.SCORE -> {
                                            append("{\n      \"score\": ${entry.value.score},\n      \"max\": ${entry.value.scoreMax}\n    }")
                                        }
                                    }
                                    if (!isLast) append(",")
                                    append("\n")
                                }
                                append("  }\n")
                                append("}")
                            }
                            JsonCodeViewer(codeJson = rawJsonSnippet)
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                for ((key, value) in eval.results) {
                                    when (value.type) {
                                        QuestionType.NOUL -> {
                                            val trueP = value.booleanProbs["true"] ?: 0.5
                                            ProbabilityBar(label = "$key [noul]", probability = trueP)
                                        }
                                        QuestionType.CHOICE -> {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(text = "$key [choice]", color = JevTextSecondary, fontSize = 12.sp)
                                                Surface(
                                                    color = JevPurpleAccent.copy(alpha = 0.2f),
                                                    shape = RoundedCornerShape(4.dp)
                                                ) {
                                                    Text(
                                                        text = value.bestChoice,
                                                        color = JevPurpleAccent,
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                        }
                                        QuestionType.SCORE -> {
                                            ScoreRiskMeter(score = value.score, maxScore = value.scoreMax)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddQuestionDialog) {
        var keyInput by remember { mutableStateOf("") }
        var typeInput by remember { mutableStateOf(QuestionType.NOUL) }
        var instructionsInput by remember { mutableStateOf("") }
        var optionsInput by remember { mutableStateOf("accumulation, distribution, neutral") }

        AlertDialog(
            onDismissRequest = { showAddQuestionDialog = false },
            confirmButton = {
                Button(
                    onClick = {
                        if (keyInput.isNotBlank()) {
                            val opts = if (typeInput == QuestionType.CHOICE) {
                                optionsInput.split(",").map { it.trim() }.filter { it.isNotBlank() }
                            } else emptyList()

                            viewModel.addPlaygroundQuestion(
                                JevQuestionConfig(
                                    key = keyInput.trim().replace(" ", "_"),
                                    type = typeInput,
                                    instructions = instructionsInput.ifBlank { "Evaluate $keyInput from state" },
                                    options = opts
                                )
                            )
                            showAddQuestionDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = JevCyanPrimary)
                ) {
                    Text("Add Question", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddQuestionDialog = false }) {
                    Text("Cancel", color = JevTextSecondary)
                }
            },
            title = {
                Text("Add Jev Primitive Question", color = JevCyanPrimary, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = keyInput,
                        onValueChange = { keyInput = it },
                        label = { Text("Key (e.g. is_rug_risk)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        QuestionType.values().forEach { t ->
                            FilterChip(
                                selected = typeInput == t,
                                onClick = { typeInput = t },
                                label = { Text(t.name.lowercase(), fontSize = 11.sp) }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = instructionsInput,
                        onValueChange = { instructionsInput = it },
                        label = { Text("Instructions") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (typeInput == QuestionType.CHOICE) {
                        OutlinedTextField(
                            value = optionsInput,
                            onValueChange = { optionsInput = it },
                            label = { Text("Options (comma separated)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            containerColor = JevDarkSurfaceElevated
        )
    }
}
