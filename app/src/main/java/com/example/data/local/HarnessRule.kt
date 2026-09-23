package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "harness_rules")
data class HarnessRule(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val category: String, // "TRADING", "SAFETY_GATE", "ROUTER"
    val description: String,
    val haltRiskThreshold: Double, // e.g. 4.0
    val executionConfidenceThreshold: Double, // e.g. 0.85
    val isEnabled: Boolean = true
)
