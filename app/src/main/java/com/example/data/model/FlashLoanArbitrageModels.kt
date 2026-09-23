package com.example.data.model

data class FlashLoanOpportunity(
    val id: String,
    val tokenSymbol: String = "USDC",
    val network: String = "Arbitrum One", // "Arbitrum One", "Ethereum Mainnet", "Base Network"
    val borrowAmountUsd: Double = 500_000.0,
    val aavePremiumFeeRate: Double = 0.0005, // 0.05%
    val rawSpreadPct: Double = 0.80, // 0.8%
    val buyDex: String = "Uniswap V3",
    val sellDex: String = "SushiSwap / Arkham Spot",
    val dexSwapFeeRate: Double = 0.003, // 0.3%
    val estimatedGasUsd: Double = 0.04, // Arbitrum ~0.04 vs Mainnet ~65.0
    val mempoolSignals: String = "Normal mempool, 1 arbitrageur observed in block stream.",
    val arkhamEntityAlert: String = "Jump Crypto rebalanced pool reserves 2 minutes ago."
) {
    val aaveFeeUsd: Double
        get() = borrowAmountUsd * aavePremiumFeeRate

    val grossRevenueUsd: Double
        get() = borrowAmountUsd * (rawSpreadPct / 100.0)

    val dexSwapFeesUsd: Double
        get() = borrowAmountUsd * dexSwapFeeRate

    val totalCostsUsd: Double
        get() = aaveFeeUsd + dexSwapFeesUsd + estimatedGasUsd

    val netProfitUsd: Double
        get() = grossRevenueUsd - totalCostsUsd

    val isMathematicallyProfitable: Boolean
        get() = netProfitUsd > 0
}

data class FlashLoanJevDecision(
    val opportunityId: String,
    val willRevertProb: Double,
    val profitMarginCategory: String, // "highly_profitable", "fee_exhausted_negative", "revert_loss_only"
    val blockSafetyScore: Double,     // 1.0 to 5.0
    val gateDecision: String,          // "APPROVED_EXECUTE", "BLOCKED_REVERT_RISK", "BLOCKED_FEE_NEGATIVE"
    val decisionExplanation: String,
    val latencyMs: Long,
    val gasSavedUsd: Double,
    val timestamp: Long = System.currentTimeMillis()
)
