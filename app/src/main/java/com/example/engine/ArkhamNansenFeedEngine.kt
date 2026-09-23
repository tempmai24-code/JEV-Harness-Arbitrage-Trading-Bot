package com.example.engine

import com.example.data.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID
import kotlin.random.Random

object ArkhamNansenFeedEngine {

    private val availableTokens = listOf(
        Pair("ALPHA", "Alpha Venture DAO"),
        Pair("SOL", "Solana"),
        Pair("ETH", "Ethereum"),
        Pair("PEPE", "Pepe"),
        Pair("PENDLE", "Pendle Finance"),
        Pair("RENDER", "Render Network"),
        Pair("SUI", "Sui Network"),
        Pair("AI16Z", "ai16z Agent Fund")
    )

    private val knownEntities = listOf(
        Pair("Wintermute", "Market Maker"),
        Pair("Jump Crypto", "Tier-1 Trading"),
        Pair("DWF Labs", "Market Maker"),
        Pair("FalconX", "Institutional Broker"),
        Pair("Vitalik.eth", "Whale / Founder"),
        Pair("Galaxy Digital", "Asset Manager"),
        Pair("0x8e5...Whale", "Smart Money Whale")
    )

    fun generateRealisticAlert(tokenOverride: String? = null, forceHighRisk: Boolean = false): Pair<ArkhamAlert, NansenMetrics> {
        val (token, _) = if (tokenOverride != null) {
            Pair(tokenOverride, "")
        } else {
            availableTokens.random()
        }

        val (entity, entityCat) = knownEntities.random()
        val isAccumulation = if (forceHighRisk) false else (Random.nextDouble() > 0.3)

        val action = if (forceHighRisk) {
            "Suspicious Liquidity Removal"
        } else if (isAccumulation) {
            "Withdrawal to DEX LP Wallet"
        } else {
            "Deposit to Binance Cold Wallet"
        }

        val amount = if (forceHighRisk) {
            Random.nextDouble(150_000.0, 900_000.0)
        } else {
            Random.nextDouble(250_000.0, 3_500_000.0)
        }

        val arkhamAlert = ArkhamAlert(
            id = UUID.randomUUID().toString().take(8),
            entityName = entity,
            entityCategory = entityCat,
            actionType = action,
            tokenSymbol = "$$token",
            amountUsd = amount,
            sourceWallet = if (isAccumulation) "Exchange (Binance/Coinbase)" else "0x${UUID.randomUUID().toString().replace("-", "").take(10)}...",
            destinationWallet = if (isAccumulation) "Private DEX Multisig" else "Exchange Deposit Wallet"
        )

        val netFlow = if (forceHighRisk) {
            -Random.nextDouble(400_000.0, 1_800_000.0)
        } else if (isAccumulation) {
            Random.nextDouble(650_000.0, 4_200_000.0)
        } else {
            -Random.nextDouble(200_000.0, 1_100_000.0)
        }

        val liquidity = if (forceHighRisk) {
            Random.nextDouble(45_000.0, 280_000.0) // Dangerously thin liquidity
        } else {
            Random.nextDouble(1_800_000.0, 12_500_000.0)
        }

        val momentum = if (forceHighRisk) {
            -Random.nextDouble(8.0, 24.0)
        } else if (isAccumulation) {
            Random.nextDouble(5.0, 22.0)
        } else {
            Random.nextDouble(-3.0, 3.0)
        }

        val nansen = NansenMetrics(
            tokenSymbol = "$$token",
            smartMoneyNetFlow24hUsd = netFlow,
            dexLiquidityUsd = liquidity,
            priceMomentum5mPct = momentum,
            smartMoneyHoldersCount = Random.nextInt(18, 95),
            exchangeReserveChangePct = if (isAccumulation) -Random.nextDouble(2.0, 8.5) else Random.nextDouble(1.0, 7.0)
        )

        return Pair(arkhamAlert, nansen)
    }

    fun buildJevStatePrompt(alert: ArkhamAlert, nansen: NansenMetrics): String {
        val flowSign = if (nansen.smartMoneyNetFlow24hUsd >= 0) "+$" else "-$"
        val flowAbs = String.format("%,.0f", kotlin.math.abs(nansen.smartMoneyNetFlow24hUsd))
        val momentumSign = if (nansen.priceMomentum5mPct >= 0) "+${String.format("%.1f", nansen.priceMomentum5mPct)}%" else "${String.format("%.1f", nansen.priceMomentum5mPct)}%"
        val liquidityFmt = String.format("%,.0f", nansen.dexLiquidityUsd)
        val alertAmtFmt = String.format("%,.0f", alert.amountUsd)

        return "Token: ${alert.tokenSymbol}. Nansen Smart Money 24h Net Flow: $flowSign$flowAbs. " +
                "Arkham alert: Known ${alert.entityName} (${alert.entityCategory}) performed '${alert.actionType}' of $$alertAmtFmt to ${alert.destinationWallet}. " +
                "Current DEX pool Liquidity: $$liquidityFmt. 5-min price momentum: $momentumSign."
    }

    suspend fun processFeedItem(
        alert: ArkhamAlert,
        nansen: NansenMetrics,
        riskHaltThreshold: Double = 4.0,
        pumpExecThreshold: Double = 0.85
    ): TradingSignal {
        val state = buildJevStatePrompt(alert, nansen)

        // 3 Standard Jev Questions for Algorithmic Crypto Trading:
        val questions = mapOf(
            "is_insider_pump" to JevQuestionConfig(
                key = "is_insider_pump",
                type = QuestionType.NOUL,
                instructions = "The state indicates highly concentrated smart money accumulation likely to result in short-term upward volatility."
            ),
            "market_regime" to JevQuestionConfig(
                key = "market_regime",
                type = QuestionType.CHOICE,
                options = listOf("accumulation", "distribution", "neutral"),
                instructions = "Identify the immediate market phase for this asset based on cross-platform entity flows."
            ),
            "risk_rating" to JevQuestionConfig(
                key = "risk_rating",
                type = QuestionType.SCORE,
                scoreMin = 1.0,
                scoreMax = 5.0,
                instructions = "Rate the likelihood of a liquidity rug or rapid dumping from 1 (completely safe) to 5 (extreme risk)."
            )
        )

        val jevResult = JevEngine.evaluate(state, questions)

        val insiderPumpProb = jevResult.results["is_insider_pump"]?.booleanProbs?.get("true") ?: 0.50
        val marketRegime = jevResult.results["market_regime"]?.bestChoice ?: "neutral"
        val regimeConf = jevResult.results["market_regime"]?.choiceProbs?.get(marketRegime) ?: 0.60
        val riskRating = jevResult.results["risk_rating"]?.score ?: 2.5

        // Deterministic Code Gate Evaluation
        val decision: ExecutionGateDecision
        val outcome: SwapOutcome?

        if (riskRating >= riskHaltThreshold || nansen.dexLiquidityUsd < 100_000) {
            decision = ExecutionGateDecision.RISK_HALTED_GUARDRAIL
            outcome = null
        } else if (insiderPumpProb >= pumpExecThreshold && marketRegime == "accumulation") {
            decision = ExecutionGateDecision.AUTO_EXECUTE_SWAP
            val swapAmount = if (insiderPumpProb > 0.90) 25_000.0 else 10_000.0
            val slippage = Random.nextDouble(0.08, 0.42)
            val pnl = Random.nextDouble(2.4, 9.8) - slippage
            val dex = if (alert.tokenSymbol.contains("SOL") || alert.tokenSymbol.contains("ALPHA")) "Jupiter (Solana)" else "Hyperliquid (L1 DEX)"
            outcome = SwapOutcome(
                dexPlatform = dex,
                tokenIn = "USDC",
                tokenOut = alert.tokenSymbol,
                executionAmountUsd = swapAmount,
                simulatedSlippagePct = Math.round(slippage * 100.0) / 100.0,
                simulatedPnlPct = Math.round(pnl * 100.0) / 100.0,
                txHash = "5K" + UUID.randomUUID().toString().replace("-", "").take(20)
            )
        } else {
            decision = ExecutionGateDecision.NEUTRAL_NO_ACTION
            outcome = null
        }

        return TradingSignal(
            id = UUID.randomUUID().toString().take(8),
            tokenSymbol = alert.tokenSymbol,
            statePrompt = state,
            isInsiderPumpProb = insiderPumpProb,
            marketRegime = marketRegime,
            marketRegimeConfidence = regimeConf,
            riskRating = riskRating,
            gateDecision = decision,
            latencyMs = jevResult.latencyMs,
            swapOutcome = outcome
        )
    }
}
