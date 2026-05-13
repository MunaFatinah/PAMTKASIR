package com.muna.pamtkasir.ui.kas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.muna.pamtkasir.SupabaseClientProvider
import com.muna.pamtkasir.model.Kas
import com.muna.pamtkasir.model.KasLog
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

sealed class KasState {
    object Loading : KasState()
    data class Success(val data: List<Kas>) : KasState()
    data class Error(val message: String) : KasState()
}

sealed class KasLogState {
    object Loading : KasLogState()
    data class Success(val data: List<KasLog>) : KasLogState()
    data class Error(val message: String) : KasLogState()
}

@Serializable
private data class KasInsert(
    val name: String,
    val balance: Double,
    @SerialName("user_id") val userId: String,
    @SerialName("is_active") val isActive: Boolean = true
)

@Serializable
private data class KasLogInsert(
    @SerialName("kas_id") val kasId: String,
    val amount: Double,
    val description: String
)

@Serializable
private data class BalanceUpdate(
    val balance: Double
)

@Serializable
private data class NameUpdate(
    val name: String
)

@Serializable
private data class ActiveUpdate(
    @SerialName("is_active") val isActive: Boolean
)

class KasViewModel : ViewModel() {

    private val _kasState    = MutableStateFlow<KasState>(KasState.Loading)
    val kasState: StateFlow<KasState> = _kasState

    private val _logState    = MutableStateFlow<KasLogState>(KasLogState.Loading)
    val logState: StateFlow<KasLogState> = _logState

    private val _actionState = MutableStateFlow<String?>(null)
    val actionState: StateFlow<String?> = _actionState

    init { fetchKas() }

    fun fetchKas() {
        viewModelScope.launch {
            _kasState.value = KasState.Loading
            try {
                val result = SupabaseClientProvider.client.postgrest
                    .from("kas")
                    .select()
                    .decodeList<Kas>()
                _kasState.value = KasState.Success(result)
            } catch (e: Exception) {
                _kasState.value = KasState.Error(e.message ?: "Gagal memuat kas")
            }
        }
    }

    fun fetchLog(kasId: String) {
        viewModelScope.launch {
            _logState.value = KasLogState.Loading
            try {
                val result = SupabaseClientProvider.client.postgrest
                    .from("kas_logs")
                    .select {
                        filter { eq("kas_id", kasId) }
                        order("created_at", Order.DESCENDING)
                    }
                    .decodeList<KasLog>()
                _logState.value = KasLogState.Success(result)
            } catch (e: Exception) {
                _logState.value = KasLogState.Error(e.message ?: "Gagal memuat log")
            }
        }
    }

    fun addKas(name: String, saldoAwal: Double, userId: String) {
        viewModelScope.launch {
            try {
                val kasResult = SupabaseClientProvider.client.postgrest
                    .from("kas")
                    .insert(KasInsert(name = name, balance = saldoAwal, userId = userId)) {
                        select()
                    }
                    .decodeSingle<Kas>()

                if (saldoAwal > 0) {
                    SupabaseClientProvider.client.postgrest
                        .from("kas_logs")
                        .insert(KasLogInsert(
                            kasId       = kasResult.id,
                            amount      = saldoAwal,
                            description = "Saldo awal kas"
                        ))
                }

                _actionState.value = "Kas berhasil ditambahkan"
                fetchKas()
            } catch (e: Exception) {
                _actionState.value = "Gagal: ${e.message}"
            }
        }
    }

    fun editNamaKas(id: String, name: String) {
        viewModelScope.launch {
            try {
                SupabaseClientProvider.client.postgrest
                    .from("kas")
                    .update(NameUpdate(name = name)) {
                        filter { eq("id", id) }
                        select()
                    }
                _actionState.value = "Nama kas berhasil diubah"
                fetchKas()
            } catch (e: Exception) {
                _actionState.value = "Gagal: ${e.message}"
            }
        }
    }

    fun toggleAktif(id: String, currentStatus: Boolean) {
        viewModelScope.launch {
            try {
                SupabaseClientProvider.client.postgrest
                    .from("kas")
                    .update(ActiveUpdate(isActive = !currentStatus)) {
                        filter { eq("id", id) }
                        select()
                    }
                _actionState.value = if (currentStatus) "Kas dinonaktifkan" else "Kas diaktifkan"
                fetchKas()
            } catch (e: Exception) {
                _actionState.value = "Gagal: ${e.message}"
            }
        }
    }

    fun transaksiManual(kas: Kas, amount: Double, deskripsi: String, isTambah: Boolean) {
        viewModelScope.launch {
            try {
                val finalAmount = if (isTambah) amount else -amount
                val newBalance  = kas.balance + finalAmount

                if (newBalance < 0) {
                    _actionState.value = "Saldo tidak mencukupi"
                    return@launch
                }

                SupabaseClientProvider.client.postgrest
                    .from("kas")
                    .update(BalanceUpdate(balance = newBalance)) {
                        filter { eq("id", kas.id) }
                        select()
                    }

                SupabaseClientProvider.client.postgrest
                    .from("kas_logs")
                    .insert(KasLogInsert(
                        kasId       = kas.id,
                        amount      = finalAmount,
                        description = deskripsi
                    ))

                _actionState.value = if (isTambah) "Saldo berhasil ditambahkan"
                else "Saldo berhasil dikurangi"
                fetchKas()
            } catch (e: Exception) {
                _actionState.value = "Gagal: ${e.message}"
            }
        }
    }

    fun clearActionState() { _actionState.value = null }
}