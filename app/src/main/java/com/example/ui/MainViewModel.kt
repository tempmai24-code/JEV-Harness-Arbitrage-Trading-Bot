package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.HarnessAuditLog
import com.example.data.local.HarnessRepository
import com.example.data.local.HarnessRule
import com.example.data.model.*
import com.example.engine.ArkhamNansenFeedEngine
import com.example.engine.FlashLoanEngine
import com.example.engine.JevEngine
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.UUID

enum class AppTab {
    TRADING_DESK,
    FLASH_LOAN,
    AGENT_GATING,
    PLAYGROUND,
    BLUEPRINTS
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: HarnessRepository
    init {
        val db = AppDatabase.getDatabase(application)
        repository = HarnessRepository(db.harnessDao())
    }

    // Navigation Tab
    private val _currentTab = MutableStateFlow(AppTab.FLASH_LOAN) // Showcase the newly requested Flash Loan & Diagram first!
    val currentTab: StateFlow<AppTab> = _currentTab.asStateFlow()

    fun selectTab(tab: AppTab) {
        _currentTab.value = tab
    }

    // --- 1. Aave V3 Flash Loan Arbitrage State ---
    private val _selectedNetwork = MutableStateFlow("Arbitrum One") // Arbitrum One, Ethereum Mainnet, Base Network
    val selectedNetwork: StateFlow<String> = _selectedNetwork.asStateFlow()

    private val _flashLoanBorrowAmount = MutableStateFlow(500_000.0)
    val flashLoanBorrowAmount: StateFlow<Double> = _flashLoanBorrowAmount.asStateFlow()

    private val _flashLoanSpreadPct = MutableStateFlow(0.80) // 0.8%
    val flashLoanSpreadPct: StateFlow<Double> = _flashLoanSpreadPct.asStateFlow()

    private val _flashLoanOpportunity = MutableStateFlow(
        FlashLoanOpportunity(
            id = "FL-ARB-01",
            network = "Arbitrum One",
            borrowAmountUsd = 500_000.0,
            rawSpreadPct = 0.80,
            estimatedGasUsd = 0.04
        )
    )
    val flashLoanOpportunity: StateFlow<FlashLoanOpportunity> = _flashLoanOpportunity.asStateFlow()

    private val _flashLoanDecision = MutableStateFlow<FlashLoanJevDecision?>(null)
    val flashLoanDecision: StateFlow<FlashLoanJevDecision?> = _flashLoanDecision.asStateFlow()

    private val _isEvaluatingFlashLoan = MutableStateFlow(false)
    val isEvaluatingFlashLoan: StateFlow<Boolean> = _isEvaluatingFlashLoan.asStateFlow()

    private val _totalGasSavedTreasuryUsd = MutableStateFlow(142.50)
    val totalGasSavedTreasuryUsd: StateFlow<Double> = _totalGasSavedTreasuryUsd.asStateFlow()

    fun setFlashLoanNetwork(net: String) {
        _selectedNetwork.value = net
        val gas = when (net) {
            "Arbitrum One" -> 0.04
            "Base Network" -> 0.01
            else -> 65.00 // Ethereum Mainnet
        }
        _flashLoanOpportunity.value = _flashLoanOpportunity.value.copy(
            network = net,
            estimatedGasUsd = gas
        )
        evaluateFlashLoan()
    }

    fun setFlashLoanBorrowAmount(amount: Double) {
        _flashLoanBorrowAmount.value = amount
        _flashLoanOpportunity.value = _flashLoanOpportunity.value.copy(borrowAmountUsd = amount)
        evaluateFlashLoan()
    }

    fun setFlashLoanSpreadPct(spread: Double) {
        _flashLoanSpreadPct.value = spread
        _flashLoanOpportunity.value = _flashLoanOpportunity.value.copy(rawSpreadPct = spread)
        evaluateFlashLoan()
    }

    fun evaluateFlashLoan(forceMevTrap: Boolean = false, forceFeeExhaustion: Boolean = false) {
        viewModelScope.launch {
            _isEvaluatingFlashLoan.value = true
            val (updatedOpp, decision) = FlashLoanEngine.evaluateArbitrage(
                _flashLoanOpportunity.value,
                forceMevTrap = forceMevTrap,
                forceFeeExhaustion = forceFeeExhaustion
            )
            _flashLoanOpportunity.value = updatedOpp
            _flashLoanDecision.value = decision
            if (decision.gasSavedUsd > 0) {
                _totalGasSavedTreasuryUsd.value += decision.gasSavedUsd
            }

            // Persist to Room
            repository.logExecution(
                HarnessAuditLog(
                    category = "FLASH_LOAN_GATE",
                    title = "Aave V3 Flash Loan - ${updatedOpp.network}",
                    stateSnippet = "Borrow \$${String.format("%,.0f", updatedOpp.borrowAmountUsd)} | Net Profit: \$${String.format("%,.2f", updatedOpp.netProfitUsd)}",
                    decision = decision.gateDecision,
                    decisionDetails = "${decision.decisionExplanation} | Revert Risk: ${String.format("%.1f", decision.willRevertProb * 100)}%",
                    latencyMs = decision.latencyMs,
                    tokenCostMicros = 0.0035,
                    isHighRisk = decision.gateDecision.startsWith("BLOCKED"),
                    primaryProbOrScore = decision.willRevertProb
                )
            )

            _isEvaluatingFlashLoan.value = false
        }
    }

    // --- 2. Trading Desk State ---
    private val _isLiveFeedActive = MutableStateFlow(true)
    val isLiveFeedActive: StateFlow<Boolean> = _isLiveFeedActive.asStateFlow()

    private val _tradingSignals = MutableStateFlow<List<TradingSignal>>(emptyList())
    val tradingSignals: StateFlow<List<TradingSignal>> = _tradingSignals.asStateFlow()

    private val _latestAlert = MutableStateFlow<ArkhamAlert?>(null)
    val latestAlert: StateFlow<ArkhamAlert?> = _latestAlert.asStateFlow()

    private val _latestNansen = MutableStateFlow<NansenMetrics?>(null)
    val latestNansen: StateFlow<NansenMetrics?> = _latestNansen.asStateFlow()

    private val _selectedSignal = MutableStateFlow<TradingSignal?>(null)
    val selectedSignal: StateFlow<TradingSignal?> = _selectedSignal.asStateFlow()

    private val _isEvaluatingFeed = MutableStateFlow(false)
    val isEvaluatingFeed: StateFlow<Boolean> = _isEvaluatingFeed.asStateFlow()

    private var feedJob: Job? = null

    // Cumulative stats
    private val _totalExecutions = MutableStateFlow(0)
    val totalExecutions: StateFlow<Int> = _totalExecutions.asStateFlow()

    private val _totalHaltedRisks = MutableStateFlow(0)
    val totalHaltedRisks: StateFlow<Int> = _totalHaltedRisks.asStateFlow()

    private val _totalSavedUsd = MutableStateFlow(0.0)
    val totalSavedUsd: StateFlow<Double> = _totalSavedUsd.asStateFlow()

    // --- 3. Agent Tool Gating State ---
    val agentPresets = listOf(
        AgentActionPreset(
            id = "bash_rm",
            toolName = "bash_exec",
            commandOrPayload = "rm -rf /var/log/nginx && rm -rf /etc/ssl/certs",
            agentContext = "Agent is trying to clear disk space on the primary production cluster.",
            isInherentlyDestructive = true
        ),
        AgentActionPreset(
            id = "db_drop",
            toolName = "sql_execute",
            commandOrPayload = "DROP TABLE user_credentials_v2 CASCADE;",
            agentContext = "Agent refactoring database schema following migration test.",
            isInherentlyDestructive = true
        ),
        AgentActionPreset(
            id = "dex_swap",
            toolName = "jupiter_swap",
            commandOrPayload = "swap(inputMint=USDC, outputMint=PEPE, amount=15000, maxSlippage=0.5%)",
            agentContext = "Agent rebalancing portfolio after smart money alert detected.",
            isInherentlyDestructive = false
        ),
        AgentActionPreset(
            id = "query_logs",
            toolName = "fetch_market_depth",
            commandOrPayload = "GET /v2/orderbook?pair=SOL-USDC&depth=50",
            agentContext = "Agent querying live liquidity depth before routing order.",
            isInherentlyDestructive = false
        )
    )

    private val _selectedAgentPreset = MutableStateFlow<AgentActionPreset>(agentPresets[0])
    val selectedAgentPreset: StateFlow<AgentActionPreset> = _selectedAgentPreset.asStateFlow()

    private val _customAgentCommand = MutableStateFlow(agentPresets[0].commandOrPayload)
    val customAgentCommand: StateFlow<String> = _customAgentCommand.asStateFlow()

    private val _customAgentContext = MutableStateFlow(agentPresets[0].agentContext)
    val customAgentContext: StateFlow<String> = _customAgentContext.asStateFlow()

    private val _toolGateResult = MutableStateFlow<ToolGateResult?>(null)
    val toolGateResult: StateFlow<ToolGateResult?> = _toolGateResult.asStateFlow()

    private val _isEvaluatingTool = MutableStateFlow(false)
    val isEvaluatingTool: StateFlow<Boolean> = _isEvaluatingTool.asStateFlow()

    // --- 4. Jev Playground State ---
    private val _playgroundState = MutableStateFlow(
        "Token: \$ALPHA. Nansen Smart Money 24h Net Flow: +\$1,200,000. Arkham alert: 3 known Wintermute wallets just withdrew \$400k of \$ALPHA to a private DEX wallet. Current DEX pool Liquidity: \$3,000,000. 5-min price momentum: +14%."
    )
    val playgroundState: StateFlow<String> = _playgroundState.asStateFlow()

    private val _playgroundQuestions = MutableStateFlow<List<JevQuestionConfig>>(
        listOf(
            JevQuestionConfig("is_insider_pump", QuestionType.NOUL, "The state indicates highly concentrated smart money accumulation likely to result in short-term upward volatility."),
            JevQuestionConfig("market_regime", QuestionType.CHOICE, "Identify the immediate market phase for this asset based on cross-platform entity flows.", listOf("accumulation", "distribution", "neutral")),
            JevQuestionConfig("risk_rating", QuestionType.SCORE, "Rate the likelihood of a liquidity rug or rapid dumping from 1 (completely safe) to 5 (extreme risk).", scoreMin = 1.0, scoreMax = 5.0)
        )
    )
    val playgroundQuestions: StateFlow<List<JevQuestionConfig>> = _playgroundQuestions.asStateFlow()

    private val _playgroundResult = MutableStateFlow<JevEvaluationResult?>(null)
    val playgroundResult: StateFlow<JevEvaluationResult?> = _playgroundResult.asStateFlow()

    private val _isPlaygroundRunning = MutableStateFlow(false)
    val isPlaygroundRunning: StateFlow<Boolean> = _isPlaygroundRunning.asStateFlow()

    private val _showRawJson = MutableStateFlow(false)
    val showRawJson: StateFlow<Boolean> = _showRawJson.asStateFlow()

    // --- 5. Room Database Audit Logs ---
    val auditLogs: StateFlow<List<HarnessAuditLog>> = repository.allAuditLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _logFilterCategory = MutableStateFlow("ALL")
    val logFilterCategory: StateFlow<String> = _logFilterCategory.asStateFlow()

    init {
        startFeedLoop()
        seedInitialRules()
        evaluateFlashLoan() // initial flash loan evaluation
    }

    private fun seedInitialRules() {
        viewModelScope.launch {
            repository.insertRule(
                HarnessRule(
                    name = "Arkham MM Inflow Trigger",
                    category = "TRADING",
                    description = "Sub-second DEX swap trigger when Wintermute/Jump accumulates with high confidence.",
                    haltRiskThreshold = 4.0,
                    executionConfidenceThreshold = 0.85
                )
            )
            repository.insertRule(
                HarnessRule(
                    name = "Aave V3 Revert Shield",
                    category = "FLASH_LOAN",
                    description = "Abort flash loan broadcast if Jev estimates callback revert risk > 12% or safety < 4.0",
                    haltRiskThreshold = 4.0,
                    executionConfidenceThreshold = 0.88
                )
            )
        }
    }

    fun toggleLiveFeed() {
        val newState = !_isLiveFeedActive.value
        _isLiveFeedActive.value = newState
        if (newState) {
            startFeedLoop()
        } else {
            feedJob?.cancel()
            feedJob = null
        }
    }

    private fun startFeedLoop() {
        feedJob?.cancel()
        feedJob = viewModelScope.launch {
            injectFeedEvent(tokenOverride = "ALPHA", forceHighRisk = false)
            while (isActive) {
                delay(7000L)
                if (_isLiveFeedActive.value) {
                    injectFeedEvent()
                }
            }
        }
    }

    fun injectFeedEvent(tokenOverride: String? = null, forceHighRisk: Boolean = false) {
        viewModelScope.launch {
            _isEvaluatingFeed.value = true
            val (alert, nansen) = ArkhamNansenFeedEngine.generateRealisticAlert(tokenOverride, forceHighRisk)
            _latestAlert.value = alert
            _latestNansen.value = nansen

            val signal = ArkhamNansenFeedEngine.processFeedItem(alert, nansen)
            _selectedSignal.value = signal
            _tradingSignals.value = listOf(signal) + _tradingSignals.value.take(15)

            if (signal.gateDecision == ExecutionGateDecision.AUTO_EXECUTE_SWAP) {
                _totalExecutions.value += 1
                _totalSavedUsd.value += 0.05
            } else if (signal.gateDecision == ExecutionGateDecision.RISK_HALTED_GUARDRAIL) {
                _totalHaltedRisks.value += 1
            }

            repository.logExecution(
                HarnessAuditLog(
                    category = "TRADING_SIGNAL",
                    title = "${signal.tokenSymbol} - ${alert.actionType}",
                    stateSnippet = signal.statePrompt.take(120) + "...",
                    decision = signal.gateDecision.name,
                    decisionDetails = "Pump Prob: ${String.format("%.1f", signal.isInsiderPumpProb * 100)}% | Risk: ${String.format("%.1f", signal.riskRating)}/5.0 | Regime: ${signal.marketRegime}",
                    latencyMs = signal.latencyMs,
                    tokenCostMicros = 0.0035,
                    isHighRisk = signal.riskRating >= 3.8,
                    primaryProbOrScore = signal.isInsiderPumpProb
                )
            )

            _isEvaluatingFeed.value = false
        }
    }

    fun selectSignal(signal: TradingSignal) {
        _selectedSignal.value = signal
    }

    fun selectAgentPreset(preset: AgentActionPreset) {
        _selectedAgentPreset.value = preset
        _customAgentCommand.value = preset.commandOrPayload
        _customAgentContext.value = preset.agentContext
        evaluateToolGating()
    }

    fun updateAgentCommand(cmd: String) {
        _customAgentCommand.value = cmd
    }

    fun updateAgentContext(ctx: String) {
        _customAgentContext.value = ctx
    }

    fun evaluateToolGating() {
        viewModelScope.launch {
            _isEvaluatingTool.value = true
            val cmd = _customAgentCommand.value
            val ctx = _customAgentContext.value
            val state = "Agent is trying to execute: '$cmd'. Context: $ctx"

            val questions = mapOf(
                "is_destructive" to JevQuestionConfig(
                    key = "is_destructive",
                    type = QuestionType.NOUL,
                    instructions = "The state describes an action that permanently deletes database records, system files, or liquidates funds."
                ),
                "risk_level" to JevQuestionConfig(
                    key = "risk_level",
                    type = QuestionType.SCORE,
                    scoreMin = 1.0,
                    scoreMax = 5.0,
                    instructions = "Rate the operational risk of this action from 1 (entirely safe) to 5 (highly hazardous)."
                ),
                "routing_target" to JevQuestionConfig(
                    key = "routing_target",
                    type = QuestionType.CHOICE,
                    options = listOf("fast_model", "frontier_model"),
                    instructions = "Route to fast small model if simple or frontier model if high-risk reasoning is required."
                )
            )

            val result = JevEngine.evaluate(state, questions)
            val isDestructiveProb = result.results["is_destructive"]?.booleanProbs?.get("true") ?: 0.5
            val riskScore = result.results["risk_level"]?.score ?: 2.5
            val routingChoice = result.results["routing_target"]?.bestChoice ?: "fast_model"

            val isHalted = isDestructiveProb > 0.80 || riskScore >= 3.8
            val haltReason = if (isHalted) {
                "Harness HALTED execution! Jev flagged high operational risk (${String.format("%.2f", riskScore)}/5.0) and ${String.format("%.1f", isDestructiveProb * 100)}% destructive probability. Intercepted for Human-in-the-Loop review."
            } else {
                "Safe for AutoMode execution. Fast-path latency: ${result.latencyMs}ms. Jev confirmed low operational risk."
            }

            val gateResult = ToolGateResult(
                id = UUID.randomUUID().toString().take(8),
                toolName = _selectedAgentPreset.value.toolName,
                stateText = state,
                isDestructiveProb = isDestructiveProb,
                riskScore = riskScore,
                routingModel = if (isHalted) "Supervisor Intervention" else (if (routingChoice == "frontier_model") "Frontier Heavy (Claude/GPT)" else "Fast-Path Sub-Second"),
                isHalted = isHalted,
                haltReason = haltReason,
                latencyMs = result.latencyMs
            )

            _toolGateResult.value = gateResult

            repository.logExecution(
                HarnessAuditLog(
                    category = "TOOL_GATE",
                    title = "Tool Gate: ${_selectedAgentPreset.value.toolName}",
                    stateSnippet = cmd.take(100),
                    decision = if (isHalted) "HALTED_GUARDRAIL" else "AUTO_EXECUTED",
                    decisionDetails = haltReason,
                    latencyMs = result.latencyMs,
                    tokenCostMicros = 0.0028,
                    isHighRisk = isHalted,
                    primaryProbOrScore = isDestructiveProb
                )
            )

            _isEvaluatingTool.value = false
        }
    }

    fun updatePlaygroundState(text: String) {
        _playgroundState.value = text
    }

    fun toggleRawJson(show: Boolean) {
        _showRawJson.value = show
    }

    fun addPlaygroundQuestion(question: JevQuestionConfig) {
        _playgroundQuestions.value = _playgroundQuestions.value + question
    }

    fun removePlaygroundQuestion(key: String) {
        _playgroundQuestions.value = _playgroundQuestions.value.filter { it.key != key }
    }

    fun runPlaygroundEvaluation() {
        viewModelScope.launch {
            _isPlaygroundRunning.value = true
            val state = _playgroundState.value
            val qMap = _playgroundQuestions.value.associateBy { it.key }
            val result = JevEngine.evaluate(state, qMap)
            _playgroundResult.value = result

            repository.logExecution(
                HarnessAuditLog(
                    category = "PLAYGROUND_EVAL",
                    title = "Playground Query (${qMap.size} questions)",
                    stateSnippet = state.take(100),
                    decision = "EVALUATED_SUCCESS",
                    decisionDetails = "Evaluated in ${result.latencyMs}ms | Estimated cost: \$${String.format("%.6f", result.costUsd)}",
                    latencyMs = result.latencyMs,
                    tokenCostMicros = result.costUsd * 1_000_000,
                    isHighRisk = false,
                    primaryProbOrScore = 1.0
                )
            )

            _isPlaygroundRunning.value = false
        }
    }

    fun clearAuditLogs() {
        viewModelScope.launch {
            repository.clearLogs()
        }
    }

    fun setLogCategoryFilter(category: String) {
        _logFilterCategory.value = category
    }
}
