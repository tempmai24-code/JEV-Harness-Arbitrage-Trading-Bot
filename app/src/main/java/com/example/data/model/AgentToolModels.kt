package com.example.data.model

data class AgentActionPreset(
    val id: String,
    val toolName: String,
    val commandOrPayload: String,
    val agentContext: String,
    val isInherentlyDestructive: Boolean
)

data class ToolGateResult(
    val id: String,
    val toolName: String,
    val stateText: String,
    val isDestructiveProb: Double,
    val riskScore: Double,
    val routingModel: String, // "Fast-Path (Small Model)", "Frontier-Reasoner (Claude/GPT-4)", "Supervisor Alert"
    val isHalted: Boolean,
    val haltReason: String,
    val latencyMs: Long,
    val timestamp: Long = System.currentTimeMillis()
)
