package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface HarnessDao {
    @Query("SELECT * FROM harness_audit_logs ORDER BY timestamp DESC")
    fun getAllAuditLogs(): Flow<List<HarnessAuditLog>>

    @Query("SELECT * FROM harness_audit_logs WHERE category = :category ORDER BY timestamp DESC")
    fun getAuditLogsByCategory(category: String): Flow<List<HarnessAuditLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditLog(log: HarnessAuditLog): Long

    @Query("DELETE FROM harness_audit_logs WHERE id = :id")
    suspend fun deleteAuditLog(id: Int)

    @Query("DELETE FROM harness_audit_logs")
    suspend fun clearAllAuditLogs()

    @Query("SELECT * FROM harness_rules ORDER BY id ASC")
    fun getAllRules(): Flow<List<HarnessRule>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRule(rule: HarnessRule): Long

    @Update
    suspend fun updateRule(rule: HarnessRule)

    @Query("DELETE FROM harness_rules WHERE id = :id")
    suspend fun deleteRule(id: Int)
}
