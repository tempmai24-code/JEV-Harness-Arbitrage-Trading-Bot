package com.example.data.local

import kotlinx.coroutines.flow.Flow

class HarnessRepository(private val dao: HarnessDao) {
    val allAuditLogs: Flow<List<HarnessAuditLog>> = dao.getAllAuditLogs()
    val allRules: Flow<List<HarnessRule>> = dao.getAllRules()

    fun getLogsByCategory(category: String): Flow<List<HarnessAuditLog>> = dao.getAuditLogsByCategory(category)

    suspend fun logExecution(log: HarnessAuditLog): Long = dao.insertAuditLog(log)

    suspend fun clearLogs() = dao.clearAllAuditLogs()

    suspend fun deleteLog(id: Int) = dao.deleteAuditLog(id)

    suspend fun insertRule(rule: HarnessRule): Long = dao.insertRule(rule)

    suspend fun updateRule(rule: HarnessRule) = dao.updateRule(rule)

    suspend fun deleteRule(id: Int) = dao.deleteRule(id)
}
