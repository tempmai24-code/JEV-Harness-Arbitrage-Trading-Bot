package com.example.engine

import com.example.data.model.*
import kotlinx.coroutines.delay
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

object JevEngine {

    /**
     * Core evaluation method simulating TypeSafe AI Jev's System One model.
     * Evaluates state context against structured questions (noul, choice, score) in milliseconds.
     */
    suspend fun evaluate(
        state: String,
        questions: Map<String, JevQuestionConfig>,
        simulatedNetworkLagMs: Long = 95L
    ): JevEvaluationResult {
        val startTime = System.currentTimeMillis()
        
        // Simulating sub-second non-autoregressive forward pass (70 - 150ms)
        val jitter = Random.nextLong(15, 65)
        val totalWait = simulatedNetworkLagMs + jitter
        delay(totalWait)

        val resultsMap = mutableMapOf<String, JevResultValue>()

        for ((key, config) in questions) {
            when (config.type) {
                QuestionType.NOUL -> {
                    val trueProb = calculateNoulProbability(state, key, config.instructions)
                    val falseProb = 1.0 - trueProb
                    resultsMap[key] = JevResultValue(
                        type = QuestionType.NOUL,
                        booleanProbs = mapOf(
                            "true" to (Math.round(trueProb * 1000.0) / 1000.0),
                            "false" to (Math.round(falseProb * 1000.0) / 1000.0)
                        )
                    )
                }
                QuestionType.CHOICE -> {
                    val choiceDistribution = calculateChoiceDistribution(state, key, config.options, config.instructions)
                    val best = choiceDistribution.maxByOrNull { it.value }?.key ?: (config.options.firstOrNull() ?: "neutral")
                    resultsMap[key] = JevResultValue(
                        type = QuestionType.CHOICE,
                        choiceProbs = choiceDistribution,
                        bestChoice = best
                    )
                }
                QuestionType.SCORE -> {
                    val rawScore = calculateScoreValue(state, key, config.instructions, config.scoreMin, config.scoreMax)
                    val roundedScore = Math.round(rawScore * 100.0) / 100.0
                    resultsMap[key] = JevResultValue(
                        type = QuestionType.SCORE,
                        score = roundedScore,
                        scoreMax = config.scoreMax
                    )
                }
            }
        }

        val elapsed = System.currentTimeMillis() - startTime
        // Token cost based on $0.042 / 1,000,000 tokens
        val estimatedTokens = (state.length / 4) + (questions.size * 25)
        val costUsd = (estimatedTokens.toDouble() / 1_000_000.0) * 0.042

        return JevEvaluationResult(
            model = "jev-latest",
            state = state,
            results = resultsMap,
            latencyMs = elapsed,
            inputTokens = estimatedTokens,
            costUsd = costUsd
        )
    }

    private fun calculateNoulProbability(state: String, key: String, instructions: String): Double {
        val s = state.lowercase()
        val inst = instructions.lowercase()
        var p = 0.50

        if (key.contains("destructive") || inst.contains("deletes") || inst.contains("destructive")) {
            if (s.contains("rm -rf") || s.contains("drop database") || s.contains("delete_account") || s.contains("truncate")) {
                p = 0.985
            } else if (s.contains("update") || s.contains("kill -9") || s.contains("shutdown")) {
                p = 0.72
            } else if (s.contains("select") || s.contains("get") || s.contains("ls ") || s.contains("cat ") || s.contains("query")) {
                p = 0.015
            } else {
                p = 0.35
            }
        } else if (key.contains("insider_pump") || inst.contains("smart money accumulation")) {
            val netFlowPos = s.contains("net flow: +") || s.contains("withdrew") || s.contains("accumulation")
            val mmPresent = s.contains("wintermute") || s.contains("jump") || s.contains("dwf") || s.contains("smart money")
            val highMomentum = s.contains("+1") || s.contains("+2") || s.contains("+3")

            p = when {
                netFlowPos && mmPresent && highMomentum -> 0.945
                netFlowPos && mmPresent -> 0.865
                netFlowPos -> 0.680
                s.contains("dump") || s.contains("distribution") -> 0.08
                else -> 0.42
            }
        } else if (key.contains("safe") || inst.contains("safe")) {
            p = if (s.contains("rm ") || s.contains("exploit") || s.contains("rug")) 0.05 else 0.92
        } else {
            // General state classification
            p = if (s.contains("true") || s.contains("yes") || s.contains("confirm")) 0.88 else 0.45
        }
        return min(0.999, max(0.001, p + (Random.nextDouble(-0.02, 0.02))))
    }

    private fun calculateChoiceDistribution(
        state: String,
        key: String,
        options: List<String>,
        instructions: String
    ): Map<String, Double> {
        val s = state.lowercase()
        if (options.isEmpty()) return mapOf("neutral" to 1.0)

        val weights = mutableMapOf<String, Double>()
        for (opt in options) {
            weights[opt] = 1.0
        }

        if (options.contains("accumulation") || options.contains("distribution")) {
            if (s.contains("withdraw") || s.contains("net flow: +") || s.contains("wintermute") || s.contains("buying")) {
                weights["accumulation"] = 8.5
                weights["neutral"] = 1.2
                weights["distribution"] = 0.3
            } else if (s.contains("deposit to exchange") || s.contains("dump") || s.contains("selling") || s.contains("net flow: -")) {
                weights["distribution"] = 8.8
                weights["neutral"] = 1.0
                weights["accumulation"] = 0.2
            } else {
                weights["neutral"] = 6.0
                weights["accumulation"] = 2.0
                weights["distribution"] = 2.0
            }
        } else if (options.contains("fast_model") || options.contains("frontier_model")) {
            if (s.contains("math") || s.contains("prove") || s.contains("architecture") || s.contains("reasoning") || s.contains("complex")) {
                weights["frontier_model"] = 9.0
                weights["fast_model"] = 1.0
            } else {
                weights["fast_model"] = 8.5
                weights["frontier_model"] = 1.5
            }
        }

        val totalWeight = weights.values.sum()
        val dist = mutableMapOf<String, Double>()
        for ((opt, weight) in weights) {
            val prob = weight / totalWeight
            dist[opt] = Math.round(prob * 1000.0) / 1000.0
        }
        return dist
    }

    private fun calculateScoreValue(
        state: String,
        key: String,
        instructions: String,
        scoreMin: Double,
        scoreMax: Double
    ): Double {
        val s = state.lowercase()
        var normalized = 0.3 // 0 to 1

        if (key.contains("risk") || instructions.contains("risk") || instructions.contains("rug")) {
            if (s.contains("rm -rf") || s.contains("production server") || s.contains("rug") || s.contains("exploit")) {
                normalized = 0.97
            } else if (s.contains("liquidity: $1") || s.contains("liquidity: $2") || s.contains("low liquidity") || s.contains("kill")) {
                normalized = 0.78
            } else if (s.contains("delete") || s.contains("high slippage") || s.contains("unverified")) {
                normalized = 0.65
            } else if (s.contains("liquidity: $3,000,000") || s.contains("wintermute") || s.contains("jupiter")) {
                normalized = 0.22
            } else if (s.contains("select") || s.contains("get") || s.contains("safe")) {
                normalized = 0.08
            }
        } else {
            normalized = 0.50
        }

        val range = scoreMax - scoreMin
        val finalScore = scoreMin + (normalized * range) + Random.nextDouble(-0.15, 0.15)
        return min(scoreMax, max(scoreMin, finalScore))
    }
}
