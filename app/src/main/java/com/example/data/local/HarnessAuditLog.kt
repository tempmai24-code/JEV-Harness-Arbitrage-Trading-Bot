package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "harness_audit_logs")
data class HarnessAuditLog(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val category: String, // "TRADING_SIGNAL", "TOOL_GATE", "MODEL_ROUTE", "PLAYGROUND_EVAL"
    val title: String,
    val stateSnippet: String,
    val decision: String, // "AUTO_EXECUTED", "HALTED_GUARDRAIL", "ROUTED_FAST", "ROUTED_FRONTIER"
    val decisionDetails: String,
    val latencyMs: Long,
    val tokenCostMicros: Double, // Cost in micro-dollars
    val isHighRisk: Boolean,
    val primaryProbOrScore: Double
)
