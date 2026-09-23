package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ExecutionGateDecision
import com.example.data.model.TradingSignal
import com.example.ui.MainViewModel
import com.example.ui.components.LatencyBenchmarkBadge
import com.example.ui.components.ProbabilityBar
import com.example.ui.components.ScoreRiskMeter
import com.example.ui.theme.*

@Composable
fun TradingDeskScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val isLiveFeed by viewModel.isLiveFeedActive.collectAsState()
    val signals by viewModel.tradingSignals.collectAsState()
    val activeSignal by viewModel.selectedSignal.collectAsState()
    val isEvaluating by viewModel.isEvaluatingFeed.collectAsState()
    val totalExecutions by viewModel.totalExecutions.collectAsState()
    val totalHalted by viewModel.totalHaltedRisks.collectAsState()
    val totalSavedUsd by viewModel.totalSavedUsd.collectAsState()

    var showPromptStateModal by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(JevDarkBg)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- Header Status Bar ---
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "JEV TRADING HARNESS",
                            color = JevCyanPrimary,
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (isLiveFeed) JevEmeraldSuccess else JevTextMuted)
                        )
                    }
                    Text(
                        text = "Arkham Intel + Nansen Smart Money -> Jev System 1 (<300ms)",
                        color = JevTextSecondary,
                        fontSize = 11.sp
                    )
                }

                FilledTonalButton(
                    onClick = { viewModel.toggleLiveFeed() },
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = if (isLiveFeed) JevDarkSurfaceElevated else JevDarkSurfaceVariant,
                        contentColor = if (isLiveFeed) JevEmeraldSuccess else JevTextMuted
                    ),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("feed_toggle_button")
                ) {
                    Icon(
                        imageVector = if (isLiveFeed) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Toggle Stream",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isLiveFeed) "STREAMING" else "PAUSED",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // --- Metrics Banner ---
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = JevDarkSurface,
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, JevBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp, horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "SWAP TRADES", color = JevTextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = "$totalExecutions",
                            color = JevEmeraldSuccess,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Divider(
                        modifier = Modifier
                            .height(30.dp)
                            .width(1.dp),
                        color = JevBorder
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "RUGS HALTED", color = JevTextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = "$totalHalted",
                            color = JevCoralDanger,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Divider(
                        modifier = Modifier
                            .height(30.dp)
                            .width(1.dp),
                        color = JevBorder
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "AVG LATENCY", color = JevTextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = "118ms",
                            color = JevCyanPrimary,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        // --- Fast Action Simulator Buttons ---
        item {
            Text(
                text = "SIMULATE FEED INJECTION",
                color = JevTextMuted,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    Button(
                        onClick = { viewModel.injectFeedEvent(tokenOverride = "ALPHA", forceHighRisk = false) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF003847)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("inject_alpha_button")
                    ) {
                        Icon(imageVector = Icons.Default.TrendingUp, contentDescription = null, tint = JevCyanPrimary, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Whale Pump (\$ALPHA)", color = JevCyanPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
                item {
                    Button(
                        onClick = { viewModel.injectFeedEvent(forceHighRisk = true) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF381014)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("inject_rug_button")
                    ) {
                        Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = JevCoralDanger, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Rug Pull Risk Test", color = JevCoralDanger, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
                item {
                    Button(
                        onClick = { viewModel.injectFeedEvent(tokenOverride = "SOL", forceHighRisk = false) },
                        colors = ButtonDefaults.buttonColors(containerColor = JevDarkSurfaceElevated),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(imageVector = Icons.Default.FlashOn, contentDescription = null, tint = JevGoldAccent, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Random Alert", color = JevTextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // --- Active Evaluated Signal Card ---
        item {
            activeSignal?.let { signal ->
                ActiveSignalCard(
                    signal = signal,
                    onInspectState = { showPromptStateModal = true }
                )
            } ?: Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(JevDarkSurface),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = JevCyanPrimary, modifier = Modifier.size(28.dp))
            }
        }

        // --- Live Feed Pipeline Stream List ---
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "LIVE INFERENCE FEED",
                    color = JevTextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${signals.size} Signals",
                    color = JevTextMuted,
                    fontSize = 11.sp
                )
            }
        }

        items(signals) { signal ->
            SignalRowItem(
                signal = signal,
                isSelected = signal.id == activeSignal?.id,
                onClick = { viewModel.selectSignal(signal) }
            )
        }
    }

    // Modal to view raw constructed state prompt
    if (showPromptStateModal && activeSignal != null) {
        AlertDialog(
            onDismissRequest = { showPromptStateModal = false },
            confirmButton = {
                TextButton(onClick = { showPromptStateModal = false }) {
                    Text("CLOSE", color = JevCyanPrimary)
                }
            },
            title = {
                Text(
                    text = "Constructed State Payload",
                    color = JevCyanPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = "This unstructured state was compiled from Arkham Intelligence on-chain entity tracking & Nansen Smart Money metrics, then fed into Jev's System 1 model:",
                        color = JevTextSecondary,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        color = Color(0xFF060910),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, JevBorder)
                    ) {
                        Text(
                            text = activeSignal!!.statePrompt,
                            color = JevCyanPrimary,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            },
            containerColor = JevDarkSurfaceElevated
        )
    }
}

@Composable
fun ActiveSignalCard(
    signal: TradingSignal,
    onInspectState: () -> Unit
) {
    val borderColor = when (signal.gateDecision) {
        ExecutionGateDecision.AUTO_EXECUTE_SWAP -> JevEmeraldSuccess
        ExecutionGateDecision.RISK_HALTED_GUARDRAIL -> JevCoralDanger
        ExecutionGateDecision.NEUTRAL_NO_ACTION -> JevBorder
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, borderColor, RoundedCornerShape(16.dp)),
        color = JevDarkSurface
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Top Bar: Token, Status, Latency
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = JevDarkSurfaceElevated,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = signal.tokenSymbol,
                            color = JevCyanPrimary,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "SIGNAL EVALUATION",
                        color = JevTextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                LatencyBenchmarkBadge(latencyMs = signal.latencyMs)
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Jev 3-Primitive Evaluator Outputs
            Text(
                text = "JEV SYSTEM 1 OUTPUTS",
                color = JevTextMuted,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))

            // 1. Primitive: is_insider_pump (Noul / Boolean)
            ProbabilityBar(
                label = "Primitive 1: [noul] is_insider_pump",
                probability = signal.isInsiderPumpProb,
                activeColor = if (signal.isInsiderPumpProb > 0.8) JevEmeraldSuccess else JevCyanPrimary
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 2. Primitive: market_regime (Choice / Categorical)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Primitive 2: [choice] market_regime",
                    color = JevTextSecondary,
                    fontSize = 12.sp
                )
                Surface(
                    color = when (signal.marketRegime) {
                        "accumulation" -> JevEmeraldSuccess.copy(alpha = 0.2f)
                        "distribution" -> JevCoralDanger.copy(alpha = 0.2f)
                        else -> JevDarkSurfaceVariant
                    },
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = signal.marketRegime.uppercase(),
                        color = when (signal.marketRegime) {
                            "accumulation" -> JevEmeraldSuccess
                            "distribution" -> JevCoralDanger
                            else -> JevTextPrimary
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 3. Primitive: risk_rating (Score / Numerical)
            ScoreRiskMeter(
                score = signal.riskRating,
                maxScore = 5.0
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Deterministic Code Gate Decision Banner
            when (signal.gateDecision) {
                ExecutionGateDecision.AUTO_EXECUTE_SWAP -> {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFF042F20),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, JevEmeraldSuccess.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = JevEmeraldSuccess, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "FAST-PATH DEX TRADE TRIGGERED (<300ms)",
                                    color = JevEmeraldSuccess,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                            signal.swapOutcome?.let { swap ->
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Platform: ${swap.dexPlatform} | Swap: \$${String.format("%,.0f", swap.executionAmountUsd)} USDC -> ${signal.tokenSymbol}",
                                    color = JevTextPrimary,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = "Slippage: ${swap.simulatedSlippagePct}% | PnL: +${swap.simulatedPnlPct}% | Tx: ${swap.txHash.take(14)}...",
                                    color = JevEmeraldSuccess,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
                ExecutionGateDecision.RISK_HALTED_GUARDRAIL -> {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFF381014),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, JevCoralDanger.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Default.Shield, contentDescription = null, tint = JevCoralDanger, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "GUARDRAIL HALTED ORDER (PRE-TRADE RISK)",
                                    color = JevCoralDanger,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                                Text(
                                    text = "Jev flagged high dump/rug risk (${String.format("%.2f", signal.riskRating)}/5.0). Order blocked before exchange routing.",
                                    color = Color(0xFFFFB4AB),
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
                ExecutionGateDecision.NEUTRAL_NO_ACTION -> {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = JevDarkSurfaceVariant,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = "Signal below threshold: Neutral regime / insufficient accumulation momentum. Capital preserved.",
                            color = JevTextSecondary,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Inspect State Prompt Link
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onInspectState() },
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Inspect raw state context",
                    color = JevCyanPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = JevCyanPrimary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun SignalRowItem(
    signal: TradingSignal,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val statusColor = when (signal.gateDecision) {
        ExecutionGateDecision.AUTO_EXECUTE_SWAP -> JevEmeraldSuccess
        ExecutionGateDecision.RISK_HALTED_GUARDRAIL -> JevCoralDanger
        ExecutionGateDecision.NEUTRAL_NO_ACTION -> JevTextMuted
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .border(
                width = if (isSelected) 1.dp else 0.dp,
                color = if (isSelected) JevCyanPrimary else Color.Transparent,
                shape = RoundedCornerShape(10.dp)
            )
            .clickable { onClick() },
        color = if (isSelected) JevDarkSurfaceElevated else JevDarkSurface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(statusColor)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = signal.tokenSymbol,
                        color = JevTextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "Pump: ${String.format("%.0f", signal.isInsiderPumpProb * 100)}% | Risk: ${String.format("%.1f", signal.riskRating)}",
                        color = JevTextSecondary,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = statusColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = when (signal.gateDecision) {
                            ExecutionGateDecision.AUTO_EXECUTE_SWAP -> "EXECUTED"
                            ExecutionGateDecision.RISK_HALTED_GUARDRAIL -> "HALTED"
                            ExecutionGateDecision.NEUTRAL_NO_ACTION -> "NEUTRAL"
                        },
                        color = statusColor,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${signal.latencyMs}ms",
                    color = JevCyanPrimary,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}
