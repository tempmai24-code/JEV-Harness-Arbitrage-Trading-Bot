package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.MainViewModel
import com.example.ui.components.LatencyBenchmarkBadge
import com.example.ui.components.ProbabilityBar
import com.example.ui.components.ScoreRiskMeter
import com.example.ui.theme.*

@Composable
fun FlashLoanScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val opportunity by viewModel.flashLoanOpportunity.collectAsState()
    val decision by viewModel.flashLoanDecision.collectAsState()
    val isEvaluating by viewModel.isEvaluatingFlashLoan.collectAsState()
    val selectedNet by viewModel.selectedNetwork.collectAsState()
    val borrowAmount by viewModel.flashLoanBorrowAmount.collectAsState()
    val spreadPct by viewModel.flashLoanSpreadPct.collectAsState()
    val gasSavedTreasury by viewModel.totalGasSavedTreasuryUsd.collectAsState()

    var showArchitectureDetail by remember { mutableStateOf(false) }

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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.FlashOn,
                                contentDescription = null,
                                tint = JevGoldAccent,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "AAVE V3 FLASH LOAN GATE",
                                color = JevGoldAccent,
                                fontWeight = FontWeight.Black,
                                fontSize = 17.sp,
                                letterSpacing = 1.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Arbitrum One + Alchemy WebSockets + Jev Sub-200ms Pre-Flight Shield",
                            color = JevTextSecondary,
                            fontSize = 11.sp
                        )
                    }

                    Surface(
                        color = JevEmeraldSuccess.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            horizontalAlignment = Alignment.End
                        ) {
                            Text(text = "GAS SAVED", color = JevEmeraldSuccess, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            Text(
                                text = "$${String.format("%.2f", gasSavedTreasury)}",
                                color = JevEmeraldSuccess,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }

        // --- Visual Architecture Infographic Card ---
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, JevCyanPrimary.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                    .clickable { showArchitectureDetail = !showArchitectureDetail },
                color = JevDarkSurface
            ) {
                Column {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(170.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_arbitrage_architecture),
                            contentDescription = "Flash Loan Architecture Diagram",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(8.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color.Black.copy(alpha = 0.75f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "⚡ Sub-Second Pipeline Diagram (Arbitrum + Jev + Aave V3)",
                                color = JevCyanPrimary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    AnimatedVisibility(visible = showArchitectureDetail) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "The 3 Pillars of Sub-Second Execution:",
                                color = JevTextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "1. Senses: Alchemy WebSockets listening to Uniswap V3 swap events.\n" +
                                        "2. Brain: TypeSafe Jev System One classifying revert risk in <200ms.\n" +
                                        "3. Muscle: Web3 Python Harness calling Aave V3 flashLoanSimple() on Arbitrum FCFS Sequencer.",
                                color = JevTextSecondary,
                                fontSize = 11.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
        }

        // --- Network Selector Tabs ---
        item {
            Column {
                Text(
                    text = "TARGET BLOCKCHAIN NETWORK",
                    color = JevTextMuted,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Arbitrum One", "Ethereum Mainnet", "Base Network").forEach { net ->
                        val isSelected = selectedNet == net
                        val isRecommended = net == "Arbitrum One"
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .border(
                                    1.dp,
                                    if (isSelected) (if (isRecommended) JevCyanPrimary else JevTextSecondary) else JevBorder,
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable { viewModel.setFlashLoanNetwork(net) },
                            color = if (isSelected) JevCyanPrimary.copy(alpha = 0.15f) else JevDarkSurface
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = if (isRecommended) "🦅 Arbitrum" else (if (net.contains("Mainnet")) "🔹 Mainnet" else "🔵 Base"),
                                    color = if (isSelected) JevTextPrimary else JevTextMuted,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (isRecommended) "0.25s Block | \$0.04" else (if (net.contains("Mainnet")) "12s Block | \$65.0" else "2.0s Block | \$0.01"),
                                    color = if (isRecommended) JevCyanPrimary else JevTextMuted,
                                    fontSize = 9.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- Flash Loan Math Calculator ---
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = JevDarkSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, JevBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "AAVE V3 ARBITRAGE EQUATION",
                        color = JevTextMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    // Borrow Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Borrow Amount:", color = JevTextSecondary, fontSize = 12.sp)
                        Text(
                            text = "$${String.format("%,.0f", borrowAmount)} USDC",
                            color = JevCyanPrimary,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 13.sp
                        )
                    }
                    Slider(
                        value = borrowAmount.toFloat(),
                        onValueChange = { viewModel.setFlashLoanBorrowAmount(it.toDouble()) },
                        valueRange = 100_000f..3_000_000f,
                        steps = 29,
                        colors = SliderDefaults.colors(
                            thumbColor = JevCyanPrimary,
                            activeTrackColor = JevCyanPrimary,
                            inactiveTrackColor = JevBorder
                        )
                    )

                    // Spread Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Price Spread Gap:", color = JevTextSecondary, fontSize = 12.sp)
                        Text(
                            text = "${String.format("%.2f", spreadPct)}%",
                            color = JevGoldAccent,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 13.sp
                        )
                    }
                    Slider(
                        value = spreadPct.toFloat(),
                        onValueChange = { viewModel.setFlashLoanSpreadPct(it.toDouble()) },
                        valueRange = 0.10f..2.00f,
                        steps = 19,
                        colors = SliderDefaults.colors(
                            thumbColor = JevGoldAccent,
                            activeTrackColor = JevGoldAccent,
                            inactiveTrackColor = JevBorder
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Breakdown Lines
                    Surface(
                        color = Color(0xFF060910),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(text = "Gross Revenue (${opportunity.buyDex} -> ${opportunity.sellDex}):", color = JevTextSecondary, fontSize = 11.sp)
                                Text(text = "+$${String.format("%,.2f", opportunity.grossRevenueUsd)}", color = JevEmeraldSuccess, fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(text = "Aave V3 Fee (Fixed 0.05%):", color = JevTextSecondary, fontSize = 11.sp)
                                Text(text = "-$${String.format("%,.2f", opportunity.aaveFeeUsd)}", color = JevCoralDanger, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(text = "DEX Swap Fees (0.3%):", color = JevTextSecondary, fontSize = 11.sp)
                                Text(text = "-$${String.format("%,.2f", opportunity.dexSwapFeesUsd)}", color = JevCoralDanger, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(text = "Network Gas (${opportunity.network}):", color = JevTextSecondary, fontSize = 11.sp)
                                Text(text = "-$${String.format("%.2f", opportunity.estimatedGasUsd)}", color = if (opportunity.network == "Ethereum Mainnet") JevCoralDanger else JevCyanPrimary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                            }
                            Divider(modifier = Modifier.padding(vertical = 4.dp), color = JevBorder)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(text = "ESTIMATED NET PROFIT:", color = JevTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
                                Text(
                                    text = "${if (opportunity.isMathematicallyProfitable) "+" else ""}$${String.format("%,.2f", opportunity.netProfitUsd)}",
                                    color = if (opportunity.isMathematicallyProfitable) JevEmeraldSuccess else JevCoralDanger,
                                    fontSize = 13.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- Fast Action Simulation Buttons ---
        item {
            Text(
                text = "SIMULATE MEMPOOL CONDITIONS",
                color = JevTextMuted,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { viewModel.evaluateFlashLoan(forceMevTrap = false, forceFeeExhaustion = false) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF003847)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("scan_clean_block_button"),
                    contentPadding = PaddingValues(vertical = 6.dp)
                ) {
                    Text("Clean Block (Safe)", color = JevCyanPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { viewModel.evaluateFlashLoan(forceMevTrap = true, forceFeeExhaustion = false) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF381014)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("simulate_mev_trap_button"),
                    contentPadding = PaddingValues(vertical = 6.dp)
                ) {
                    Text("MEV Sandwich Trap", color = JevCoralDanger, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { viewModel.evaluateFlashLoan(forceMevTrap = false, forceFeeExhaustion = true) },
                    colors = ButtonDefaults.buttonColors(containerColor = JevDarkSurfaceElevated),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(vertical = 6.dp)
                ) {
                    Text("Fee Squeeze", color = JevGoldAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // --- Jev Pre-Flight Revert Gate Decision ---
        item {
            decision?.let { dec ->
                val isBlocked = dec.gateDecision.startsWith("BLOCKED")
                val gateColor = if (isBlocked) JevCoralDanger else JevEmeraldSuccess

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, gateColor, RoundedCornerShape(16.dp)),
                    color = JevDarkSurface
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "JEV PRE-FLIGHT REVERT SHIELD",
                                color = JevTextMuted,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                            LatencyBenchmarkBadge(latencyMs = dec.latencyMs)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Primitives
                        ProbabilityBar(
                            label = "Question: [noul] will_aave_callback_revert",
                            probability = dec.willRevertProb,
                            activeColor = if (dec.willRevertProb > 0.12) JevCoralDanger else JevCyanPrimary
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Question: [choice] profit_margin_viability",
                                color = JevTextSecondary,
                                fontSize = 12.sp
                            )
                            Surface(
                                color = when (dec.profitMarginCategory) {
                                    "highly_profitable" -> JevEmeraldSuccess.copy(alpha = 0.2f)
                                    else -> JevCoralDanger.copy(alpha = 0.2f)
                                },
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = dec.profitMarginCategory.replace("_", " ").uppercase(),
                                    color = if (dec.profitMarginCategory == "highly_profitable") JevEmeraldSuccess else JevCoralDanger,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        ScoreRiskMeter(
                            score = dec.blockSafetyScore,
                            maxScore = 5.0
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Gate Outcome
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = if (isBlocked) Color(0xFF381014) else Color(0xFF042F20),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, gateColor.copy(alpha = 0.5f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (isBlocked) Icons.Default.Shield else Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = gateColor,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isBlocked) "REVERT SHIELD: CONTRACT CALL BLOCKED" else "CLEARANCE GRANTED: EXECUTING FLASH LOAN",
                                        color = gateColor,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 11.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = dec.decisionExplanation,
                                    color = if (isBlocked) Color(0xFFFFB4AB) else Color(0xFF8CF4FF),
                                    fontSize = 11.sp
                                )
                                if (dec.gasSavedUsd > 0) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "💰 Saved $${String.format("%.2f", dec.gasSavedUsd)} in reverted gas fees!",
                                        color = JevEmeraldSuccess,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- Sub-Second Lifecycle (0ms - 550ms) ---
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = JevDarkSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, JevBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "SUB-SECOND ARBITRUM EXECUTION LIFECYCLE",
                        color = JevTextMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    TimelineStep(time = "0ms", title = "Uniswap V3 Swap", desc = "Whale swaps token creating 0.8% gap vs Arkham Spot.")
                    TimelineStep(time = "50ms", title = "Alchemy WebSocket Push", desc = "Event log intercepted and streamed to Python script.")
                    TimelineStep(time = "70ms", title = "Gross Math Calculation", desc = "Verifies $500k borrow covers 0.05% Aave fee.")
                    TimelineStep(time = "80ms", title = "Jev API Dispatch", desc = "Packages raw state into System 1 query.")
                    TimelineStep(time = "260ms", title = "Jev Classification (<200ms)", desc = "Returns revert risk 0.04 and safety 4.7/5.0.")
                    TimelineStep(time = "280ms", title = "Web3 Sign Transaction", desc = "Signed via private key on safe path.")
                    TimelineStep(time = "300ms", title = "Arbitrum FCFS Sequencer", desc = "Dispatched into sub-second 250ms block window.")
                    TimelineStep(time = "550ms", title = "Block Finality & Net Profit", desc = "Borrows, swaps, repays Aave, deposits net yield.", isLast = true)
                }
            }
        }
    }
}

@Composable
fun TimelineStep(time: String, title: String, desc: String, isLast: Boolean = false) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(50.dp)) {
            Text(text = time, color = JevCyanPrimary, fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
            if (!isLast) {
                Box(modifier = Modifier.width(1.dp).height(24.dp).background(JevBorder))
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(text = title, color = JevTextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Text(text = desc, color = JevTextSecondary, fontSize = 10.sp)
        }
    }
}
