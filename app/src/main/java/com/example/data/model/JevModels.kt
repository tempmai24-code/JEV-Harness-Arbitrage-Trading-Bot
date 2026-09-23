package com.example.data.model

enum class QuestionType {
    NOUL,   // Yes/No Boolean probability distribution
    CHOICE, // Selecting categorical option from predefined list
    SCORE   // Rating a confidence or risk value on a numerical scale
}

data class JevQuestionConfig(
    val key: String,
    val type: QuestionType,
    val instructions: String,
    val options: List<String> = emptyList(), // For CHOICE
    val scoreMin: Double = 1.0,
    val scoreMax: Double = 5.0
)

data class JevResultValue(
    val type: QuestionType,
    val booleanProbs: Map<String, Double> = emptyMap(), // e.g. "true" -> 0.985, "false" -> 0.015
    val choiceProbs: Map<String, Double> = emptyMap(),  // e.g. "accumulation" -> 0.82
    val bestChoice: String = "",
    val score: Double = 0.0,
    val scoreMax: Double = 5.0
)

data class JevEvaluationResult(
    val model: String = "jev-latest",
    val state: String,
    val results: Map<String, JevResultValue>,
    val latencyMs: Long,
    val inputTokens: Int,
    val costUsd: Double, // Based on $0.042 per million input tokens
    val timestamp: Long = System.currentTimeMillis()
)
