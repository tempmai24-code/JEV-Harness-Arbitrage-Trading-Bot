package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import com.example.data.model.AgentActionPreset
import com.example.ui.MainViewModel
import com.example.ui.components.LatencyBenchmarkBadge
import com.example.ui.components.ProbabilityBar
import com.example.ui.components.ScoreRiskMeter
import com.example.ui.theme.*

@Composable
fun AgentGatingScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val presets = viewModel.agentPresets
    val selectedPreset by viewModel.selectedAgentPreset.collectAsState()
    val commandText by viewModel.customAgentCommand.collectAsState()
    val contextText by viewModel.customAgentContext.collectAsState()
    val gateResult by viewModel.toolGateResult.collectAsState()
    val isEvaluating by viewModel.isEvaluatingTool.collectAsState()

    var showSupervisorApproveDialog by remember { mutableStateOf(false) }
    var approvedByHuman by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (gateResult == null) {
            viewModel.evaluateToolGating()
        }
    }

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
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = JevCyanPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "AGENT HARNESS & AUTOMODE",
                        color = JevCyanPrimary,
                        fontWeight = FontWeight.Black,
                        fontSize = 17.sp,
                        letterSpacing = 1.sp
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Sub-second Tool-Call Gating & Model Routing with TypeSafe Jev",
                    color = JevTextSecondary,
                    fontSize = 11.sp
                )
            }
        }

        // --- Harness Flow Diagram Card ---
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = JevDarkSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, JevBorder)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "HARNESS MIDDLEWARE LOOP",
                        color = JevTextMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        StepNode(number = "1", label = "Agent Loop", sub = "Proposes Tool")
                        Icon(imageVector = Icons.Default.ArrowForward, contentDescription = null, tint = JevBorderActive, modifier = Modifier.size(14.dp))
                        StepNode(number = "2", label = "Jev Engine", sub = "<120ms Guard")
                        Icon(imageVector = Icons.Default.ArrowForward, contentDescription = null, tint = JevBorderActive, modifier = Modifier.size(14.dp))
                        StepNode(number = "3", label = "Gate Check", sub = "Auto or Halt")
                    }
                }
            }
        }

        // --- Presets Selector ---
        item {
            Text(
                text = "SELECT PROPOSED ACTION PRESET",
                color = JevTextMuted,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(presets) { preset ->
                    val isSelected = preset.id == selectedPreset.id
                    val chipColor = if (preset.isInherentlyDestructive) JevCoralDanger else JevCyanPrimary

                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .border(
                                width = 1.dp,
                                color = if (isSelected) chipColor else JevBorder,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable {
                                approvedByHuman = false
                                viewModel.selectAgentPreset(preset)
                            }
                            .testTag("preset_${preset.id}"),
                        color = if (isSelected) chipColor.copy(alpha = 0.15f) else JevDarkSurface
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(chipColor)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = preset.toolName,
                                color = if (isSelected) chipColor else JevTextPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // --- Action Command & Context Editor ---
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = JevDarkSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, JevBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "TOOL CALL COMMAND / ARGS",
                        color = JevTextMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = commandText,
                        onValueChange = { viewModel.updateAgentCommand(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("command_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = JevCyanPrimary,
                            unfocusedBorderColor = JevBorder,
                            focusedTextColor = JevTextPrimary,
                            unfocusedTextColor = JevTextPrimary,
                            focusedContainerColor = Color(0xFF060910),
                            unfocusedContainerColor = Color(0xFF060910)
                        ),
                        textStyle = LocalTextStyle.current.copy(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp
                        ),
                        singleLine = false,
                        maxLines = 3
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "AGENT GOAL / CONTEXT",
                        color = JevTextMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = contextText,
                        onValueChange = { viewModel.updateAgentContext(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("context_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = JevCyanPrimary,
                            unfocusedBorderColor = JevBorder,
                            focusedTextColor = JevTextPrimary,
                            unfocusedTextColor = JevTextPrimary,
                            focusedContainerColor = Color(0xFF060910),
                            unfocusedContainerColor = Color(0xFF060910)
                        ),
                        textStyle = LocalTextStyle.current.copy(fontSize = 12.sp),
                        singleLine = false,
                        maxLines = 2
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            approvedByHuman = false
                            viewModel.evaluateToolGating()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = JevCyanPrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("evaluate_tool_button")
                    ) {
                        if (isEvaluating) {
                            CircularProgressIndicator(
                                color = Color.Black,
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        } else {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                        Text(
                            text = "INTERCEPT & EVALUATE WITH JEV",
                            color = Color.Black,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // --- Gating Evaluation Result ---
        item {
            gateResult?.let { result ->
                val cardBorder = if (result.isHalted && !approvedByHuman) JevCoralDanger else JevEmeraldSuccess

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, cardBorder, RoundedCornerShape(16.dp)),
                    color = JevDarkSurface
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "JEV HARNESS GATE DECISION",
                                color = JevTextMuted,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                            LatencyBenchmarkBadge(latencyMs = result.latencyMs)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Primitives
                        ProbabilityBar(
                            label = "Question: [noul] is_destructive",
                            probability = result.isDestructiveProb,
                            activeColor = if (result.isDestructiveProb > 0.8) JevCoralDanger else JevCyanPrimary
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        ScoreRiskMeter(
                            score = result.riskScore,
                            maxScore = 5.0
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Question: [choice] routing_target",
                                color = JevTextSecondary,
                                fontSize = 12.sp
                            )
                            Surface(
                                color = JevPurpleAccent.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = result.routingModel,
                                    color = JevPurpleAccent,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Gate Outcome Banner
                        if (result.isHalted && !approvedByHuman) {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                color = Color(0xFF381014),
                                shape = RoundedCornerShape(10.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, JevCoralDanger.copy(alpha = 0.5f))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(imageVector = Icons.Default.Cancel, contentDescription = null, tint = JevCoralDanger, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "AUTOMODE HALTED: HIGH DESTRUCTIVE RISK",
                                            color = JevCoralDanger,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = result.haltReason,
                                        color = Color(0xFFFFB4AB),
                                        fontSize = 11.sp
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(
                                        onClick = { showSupervisorApproveDialog = true },
                                        colors = ButtonDefaults.buttonColors(containerColor = JevCoralDanger),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        modifier = Modifier.testTag("supervisor_review_button")
                                    ) {
                                        Text("Supervisor Review & Override", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        } else {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                color = Color(0xFF042F20),
                                shape = RoundedCornerShape(10.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, JevEmeraldSuccess.copy(alpha = 0.5f))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = JevEmeraldSuccess, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (approvedByHuman) "SUPERVISOR OVERRIDE GRANTED" else "EXECUTION ALLOWED (AUTOMODE SAFE)",
                                            color = JevEmeraldSuccess,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = if (approvedByHuman)
                                            "Supervisor confirmed tool execution in sandbox. Command dispatched safely."
                                        else
                                            result.haltReason,
                                        color = Color(0xFF8CF4FF),
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showSupervisorApproveDialog) {
        AlertDialog(
            onDismissRequest = { showSupervisorApproveDialog = false },
            confirmButton = {
                Button(
                    onClick = {
                        approvedByHuman = true
                        showSupervisorApproveDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = JevEmeraldSuccess)
                ) {
                    Text("Approve & Execute", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSupervisorApproveDialog = false }) {
                    Text("Cancel / Keep Halted", color = JevCoralDanger)
                }
            },
            title = {
                Text(
                    text = "Supervisor Approval Needed",
                    color = JevCyanPrimary,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "The Jev Harness intercepted this call because operational risk is rated hazardous. Do you wish to override the gate and allow the agent to execute this command?",
                    color = JevTextSecondary,
                    fontSize = 12.sp
                )
            },
            containerColor = JevDarkSurfaceElevated
        )
    }
}

@Composable
fun StepNode(number: String, label: String, sub: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(JevDarkSurfaceElevated)
                .border(1.dp, JevCyanPrimary, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number,
                color = JevCyanPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = label, color = JevTextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Text(text = sub, color = JevTextMuted, fontSize = 9.sp)
    }
}
