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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.HarnessAuditLog
import com.example.ui.MainViewModel
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AuditLogsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val logs by viewModel.auditLogs.collectAsState()
    val filterCategory by viewModel.logFilterCategory.collectAsState()
    var showConfirmClearDialog by remember { mutableStateOf(false) }

    val filteredLogs = remember(logs, filterCategory) {
        if (filterCategory == "ALL") logs else logs.filter { it.category == filterCategory }
    }

    val timeFormat = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(JevDarkBg)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // --- Header Title ---
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = JevCyanPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "HARNESS AUDIT TRAIL",
                            color = JevCyanPrimary,
                            fontWeight = FontWeight.Black,
                            fontSize = 17.sp,
                            letterSpacing = 1.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Persistent Room Database execution history & latency records",
                        color = JevTextSecondary,
                        fontSize = 11.sp
                    )
                }

                if (logs.isNotEmpty()) {
                    IconButton(
                        onClick = { showConfirmClearDialog = true },
                        modifier = Modifier.testTag("clear_logs_button")
                    ) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Clear Logs", tint = JevCoralDanger)
                    }
                }
            }
        }

        // --- Filter Chips ---
        item {
            val categories = listOf("ALL", "TRADING_SIGNAL", "TOOL_GATE", "PLAYGROUND_EVAL")
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(categories) { cat ->
                    val isSelected = filterCategory == cat
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .border(
                                1.dp,
                                if (isSelected) JevCyanPrimary else JevBorder,
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { viewModel.setLogCategoryFilter(cat) },
                        color = if (isSelected) JevCyanPrimary.copy(alpha = 0.15f) else JevDarkSurface
                    ) {
                        Text(
                            text = when (cat) {
                                "ALL" -> "All (${logs.size})"
                                "TRADING_SIGNAL" -> "Trading"
                                "TOOL_GATE" -> "Tool Gating"
                                else -> "Playground"
                            },
                            color = if (isSelected) JevCyanPrimary else JevTextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        // --- Logs List ---
        if (filteredLogs.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(JevDarkSurface),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No audit logs recorded for this category yet.",
                        color = JevTextMuted,
                        fontSize = 12.sp
                    )
                }
            }
        } else {
            items(filteredLogs) { log ->
                AuditLogRowItem(log = log, timeFmt = timeFormat)
            }
        }
    }

    if (showConfirmClearDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmClearDialog = false },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAuditLogs()
                        showConfirmClearDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = JevCoralDanger)
                ) {
                    Text("Clear All Logs", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmClearDialog = false }) {
                    Text("Cancel", color = JevTextSecondary)
                }
            },
            title = { Text("Clear Audit Trail?", color = JevCyanPrimary) },
            text = { Text("This will permanently wipe all local Room database audit logs.", color = JevTextSecondary) },
            containerColor = JevDarkSurfaceElevated
        )
    }
}

@Composable
fun AuditLogRowItem(
    log: HarnessAuditLog,
    timeFmt: SimpleDateFormat
) {
    val isHalted = log.decision.contains("HALT") || log.isHighRisk
    val statusColor = when {
        isHalted -> JevCoralDanger
        log.decision.contains("EXECUTED") || log.decision.contains("SUCCESS") -> JevEmeraldSuccess
        else -> JevCyanPrimary
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, JevBorder, RoundedCornerShape(12.dp)),
        color = JevDarkSurface
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = statusColor.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = log.decision.replace("_", " "),
                            color = statusColor,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = log.title,
                        color = JevTextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${log.latencyMs}ms",
                        color = JevCyanPrimary,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = timeFmt.format(Date(log.timestamp)),
                        color = JevTextMuted,
                        fontSize = 10.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = log.decisionDetails,
                color = JevTextSecondary,
                fontSize = 11.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF060910),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    text = log.stateSnippet,
                    color = JevTextMuted,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(6.dp)
                )
            }
        }
    }
}
