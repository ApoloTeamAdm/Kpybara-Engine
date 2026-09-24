package com.example.sync

import android.util.Log
import com.example.data.local.dao.SyncQueueDao
import com.example.data.local.entity.SyncQueueEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

data class SyncReport(
    val patchesCount: Int,
    val totalPayloadBytes: Long,
    val savedBandwidthRatio: Double,
    val timestampEpoch: Long = System.currentTimeMillis(),
    val message: String
)

class SmartSyncManager(
    private val syncQueueDao: SyncQueueDao,
    private val connectivityObserver: NetworkConnectivityObserver,
    private val scope: CoroutineScope
) {
    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _lastSyncReport = MutableStateFlow<SyncReport?>(null)
    val lastSyncReport: StateFlow<SyncReport?> = _lastSyncReport.asStateFlow()

    val pendingPatches: Flow<List<SyncQueueEntity>> = syncQueueDao.getAllSyncItems()

    init {
        // Observa transições de conectividade para disparo automático de sincronização inteligente
        scope.launch {
            connectivityObserver.observe().collect { status ->
                if (status == ConnectivityStatus.ONLINE) {
                    Log.d("SmartSyncManager", "Conexão restaurada. Disparando sincronização de patches delta...")
                    syncPendingItems()
                }
            }
        }
    }

    /**
     * Enfileira uma ação de progresso como um RFC 6902 Delta Patch.
     */
    suspend fun recordProgressDelta(
        courseId: String,
        action: String,
        patchOperations: JSONArray
    ) = withContext(Dispatchers.IO) {
        val patchId = "patch_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}"
        val item = SyncQueueEntity(
            patch_id = patchId,
            course_id = courseId,
            action = action,
            delta_patch_json = patchOperations.toString(),
            status = "PENDING",
            created_at_epoch = System.currentTimeMillis()
        )
        syncQueueDao.enqueuePatch(item)

        // Se estiver online agora, tenta enviar de imediato
        if (connectivityObserver.isConnected()) {
            syncPendingItems()
        }
    }

    /**
     * Sincroniza todos os patches pendentes com os repositórios federados.
     */
    suspend fun syncPendingItems(): SyncReport = withContext(Dispatchers.IO) {
        if (_isSyncing.value) {
            return@withContext _lastSyncReport.value ?: SyncReport(0, 0L, 0.0, message = "Sincronização já em execução.")
        }

        val pending = syncQueueDao.getPendingSyncItemsList()
        if (pending.isEmpty()) {
            val report = SyncReport(
                patchesCount = 0,
                totalPayloadBytes = 0L,
                savedBandwidthRatio = 0.0,
                message = "Tudo em dia! Nenhum dado pendente de sincronização."
            )
            _lastSyncReport.value = report
            return@withContext report
        }

        _isSyncing.value = true

        try {
            var totalBytes = 0L
            val aggregatedPatches = JSONArray()

            pending.forEach { entity ->
                syncQueueDao.updateStatus(entity.patch_id, "SYNCING")
                val itemArr = JSONArray(entity.delta_patch_json)
                for (i in 0 until itemArr.length()) {
                    aggregatedPatches.put(itemArr.getJSONObject(i))
                }
                totalBytes += entity.delta_patch_json.toByteArray().size
            }

            // Simula envio otimizado pela rede federada (RFC 6902)
            // Em vez de enviar o curso inteiro (ex: 2.5 MB), envia apenas ~350 bytes de patch delta!
            val rawFullCourseSize = 2_500_000L
            val savedRatio = (1.0 - (totalBytes.toDouble() / rawFullCourseSize.toDouble())).coerceIn(0.0, 0.999)

            // Simula latência de rede realista
            kotlinx.coroutines.delay(800)

            // Marca todos como SYNCED
            val now = System.currentTimeMillis()
            pending.forEach { entity ->
                syncQueueDao.markSynced(entity.patch_id, now)
            }

            val report = SyncReport(
                patchesCount = pending.size,
                totalPayloadBytes = totalBytes,
                savedBandwidthRatio = savedRatio,
                timestampEpoch = now,
                message = "Sincronização federada concluída: ${pending.size} patch(es) enviados (${totalBytes} bytes). Economia de ${(savedRatio * 100).toInt()}% de dados via RFC 6902!"
            )

            _lastSyncReport.value = report
            report
        } catch (e: Exception) {
            Log.e("SmartSyncManager", "Erro na sincronização: ${e.message}")
            pending.forEach { entity ->
                syncQueueDao.updateStatus(entity.patch_id, "FAILED")
            }
            val errReport = SyncReport(
                patchesCount = pending.size,
                totalPayloadBytes = 0L,
                savedBandwidthRatio = 0.0,
                message = "Falha ao sincronizar: ${e.message}"
            )
            _lastSyncReport.value = errReport
            errReport
        } finally {
            _isSyncing.value = false
        }
    }

    suspend fun clearHistory() = withContext(Dispatchers.IO) {
        syncQueueDao.clearSynced()
    }
}
