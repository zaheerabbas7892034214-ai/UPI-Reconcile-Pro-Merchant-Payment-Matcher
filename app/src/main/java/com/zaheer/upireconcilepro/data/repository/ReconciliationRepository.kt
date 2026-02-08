package com.zaheer.upireconcilepro.data.repository

import android.util.Log
import com.zaheer.upireconcilepro.data.database.dao.MatchedItemDao
import com.zaheer.upireconcilepro.data.database.dao.ReconciliationSessionDao
import com.zaheer.upireconcilepro.data.database.dao.UnmatchedItemDao
import com.zaheer.upireconcilepro.data.database.entity.MatchedItemEntity
import com.zaheer.upireconcilepro.data.database.entity.ReconciliationSessionEntity
import com.zaheer.upireconcilepro.data.database.entity.UnmatchedItemEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class ReconciliationRepository(
    private val sessionDao: ReconciliationSessionDao,
    private val matchedItemDao: MatchedItemDao,
    private val unmatchedItemDao: UnmatchedItemDao
) {
    companion object {
        private const val TAG = "ReconciliationRepository"
    }

    sealed class Result<out T> {
        data class Success<T>(val data: T) : Result<T>()
        data class Error(val message: String) : Result<Nothing>()
    }

    fun getAllSessions(): Flow<List<ReconciliationSessionEntity>> {
        return sessionDao.getAll()
    }

    suspend fun getSessionById(sessionId: Long): Result<ReconciliationSessionEntity> {
        return withContext(Dispatchers.IO) {
            try {
                val session = sessionDao.getById(sessionId)
                if (session != null) {
                    Result.Success(session)
                } else {
                    Result.Error("Session not found")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error getting session by ID: $sessionId", e)
                Result.Error("Failed to load session: ${e.message}")
            }
        }
    }

    suspend fun createSession(session: ReconciliationSessionEntity): Result<Long> {
        return withContext(Dispatchers.IO) {
            try {
                val sessionId = sessionDao.insert(session)
                Log.d(TAG, "Created session with ID: $sessionId")
                Result.Success(sessionId)
            } catch (e: Exception) {
                Log.e(TAG, "Error creating session", e)
                Result.Error("Failed to create session: ${e.message}")
            }
        }
    }

    suspend fun deleteSession(sessionId: Long): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                sessionDao.delete(sessionId)
                matchedItemDao.deleteForSession(sessionId)
                unmatchedItemDao.deleteForSession(sessionId)
                Log.d(TAG, "Deleted session: $sessionId")
                Result.Success(Unit)
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting session: $sessionId", e)
                Result.Error("Failed to delete session: ${e.message}")
            }
        }
    }

    fun getMatchedItems(sessionId: Long): Flow<List<MatchedItemEntity>> {
        return matchedItemDao.getAllForSession(sessionId)
    }

    fun getUnmatchedItems(sessionId: Long): Flow<List<UnmatchedItemEntity>> {
        return unmatchedItemDao.getAllForSession(sessionId)
    }

    fun getUnmatchedItemsByType(sessionId: Long, type: String): Flow<List<UnmatchedItemEntity>> {
        return unmatchedItemDao.getByType(sessionId, type)
    }

    suspend fun saveMatchedItems(items: List<MatchedItemEntity>): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                matchedItemDao.insert(items)
                Log.d(TAG, "Saved ${items.size} matched items")
                Result.Success(Unit)
            } catch (e: Exception) {
                Log.e(TAG, "Error saving matched items", e)
                Result.Error("Failed to save matched items: ${e.message}")
            }
        }
    }

    suspend fun saveUnmatchedItems(items: List<UnmatchedItemEntity>): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                unmatchedItemDao.insert(items)
                Log.d(TAG, "Saved ${items.size} unmatched items")
                Result.Success(Unit)
            } catch (e: Exception) {
                Log.e(TAG, "Error saving unmatched items", e)
                Result.Error("Failed to save unmatched items: ${e.message}")
            }
        }
    }

    suspend fun deleteMatchedItems(sessionId: Long): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                matchedItemDao.deleteForSession(sessionId)
                Log.d(TAG, "Deleted matched items for session: $sessionId")
                Result.Success(Unit)
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting matched items for session: $sessionId", e)
                Result.Error("Failed to delete matched items: ${e.message}")
            }
        }
    }

    suspend fun deleteUnmatchedItems(sessionId: Long): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                unmatchedItemDao.deleteForSession(sessionId)
                Log.d(TAG, "Deleted unmatched items for session: $sessionId")
                Result.Success(Unit)
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting unmatched items for session: $sessionId", e)
                Result.Error("Failed to delete unmatched items: ${e.message}")
            }
        }
    }

    suspend fun getSessionCount(): Result<Int> {
        return withContext(Dispatchers.IO) {
            try {
                val sessions = sessionDao.getAll().first()
                Result.Success(sessions.size)
            } catch (e: Exception) {
                Log.e(TAG, "Error getting session count", e)
                Result.Error("Failed to get session count: ${e.message}")
            }
        }
    }
}
