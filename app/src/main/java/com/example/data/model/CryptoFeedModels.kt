package com.example.data.model

data class ArkhamAlert(
    val id: String,
    val entityName: String,
    val entityCategory: String, // "Market Maker", "Tier-1 VC", "Whale", "Exchange"
    val actionType: String,     // "DEX Accumulation", "Exchange Withdrawal", "Large Inflow"
    val tokenSymbol: String,
    val amountUsd: Double,
    val sourceWallet: String,
    val destinationWallet: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class NansenMetrics(
    val tokenSymbol: String,
    val smartMoneyNetFlow24hUsd: Double,
    val dexLiquidityUsd: Double,
    val priceMomentum5mPct: Double,
    val smartMoneyHoldersCount: Int,
    val exchangeReserveChangePct: Double
)

enum class ExecutionGateDecision {
    AUTO_EXECUTE_SWAP,      // Jev signals insider pump > 0.85 & risk < 3.0 -> triggers DEX swap in <300ms
    RISK_HALTED_GUARDRAIL,  // Jev signals risk_rating >= 4.0 or high rug likelihood -> halts order
    NEUTRAL_NO_ACTION       // Sub-threshold signal -> ignored to preserve capital
}

data class SwapOutcome(
    val dexPlatform: String, // e.g. "Jupiter (Solana)", "Hyperliquid Perps"
    val tokenIn: String,
    val tokenOut: String,
    val executionAmountUsd: Double,
    val simulatedSlippagePct: Double,
    val simulatedPnlPct: Double,
    val txHash: String
)

data class TradingSignal(
    val id: String,
    val tokenSymbol: String,
    val statePrompt: String,
    val isInsiderPumpProb: Double,
    val marketRegime: String,
    val marketRegimeConfidence: Double,
    val riskRating: Double,
    val gateDecision: ExecutionGateDecision,
    val latencyMs: Long,
    val timestamp: Long = System.currentTimeMillis(),
    val swapOutcome: SwapOutcome? = null
)
