package com.muna.pamtkasir.ui.pengeluaran

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.muna.pamtkasir.SupabaseClientProvider
import com.muna.pamtkasir.model.Expense
import com.muna.pamtkasir.model.Kas
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

sealed class PengeluaranState {
    object Loading : PengeluaranState()
    data class Success(val data: List<Expense>) : PengeluaranState()
    data class Error(val message: String) : PengeluaranState()
}

@Serializable
private data class KasLogInsert(
    @SerialName("kas_id") val kasId: String,
    val amount: Double,
    val description: String
)

class PengeluaranViewModel : ViewModel() {

    private val _pengeluaranState = MutableStateFlow<PengeluaranState>(PengeluaranState.Loading)
    val pengeluaranState: StateFlow<PengeluaranState> = _pengeluaranState

    private val _kasList = MutableStateFlow<List<Kas>>(emptyList())
    val kasList: StateFlow<List<Kas>> = _kasList

    private val _actionState = MutableStateFlow<String?>(null)
    val actionState: StateFlow<String?> = _actionState

    init {
        loadExpenses()
        loadKas()
    }

    fun loadExpenses() {
        viewModelScope.launch {
            _pengeluaranState.value = PengeluaranState.Loading
            try {
                val result = SupabaseClientProvider.client.postgrest
                    .from("expenses")
                    .select {
                        order("created_at", Order.DESCENDING)
                    }
                    .decodeList<Expense>()
                _pengeluaranState.value = PengeluaranState.Success(result)
            } catch (e: Exception) {
                _pengeluaranState.value = PengeluaranState.Error(e.message ?: "Gagal memuat data")
            }
        }
    }

    fun loadKas() {
        viewModelScope.launch {
            try {
                val result = SupabaseClientProvider.client.postgrest
                    .from("kas")
                    .select {
                        filter { eq("is_active", true) }
                    }
                    .decodeList<Kas>()
                _kasList.value = result
            } catch (e: Exception) {
                _actionState.value = "Gagal memuat kas: ${e.message}"
            }
        }
    }

    fun tambahPengeluaran(kasId: String, description: String, total: Double) {
        viewModelScope.launch {
            try {
                // 1. Insert pengeluaran
                SupabaseClientProvider.client.postgrest
                    .from("expenses")
                    .insert(Expense(
                        kas_id = kasId,
                        description = description,
                        total = total,
                        status = "active"
                    ))

                // 2. Ambil saldo langsung dari database
                val kasResult = SupabaseClientProvider.client.postgrest
                    .from("kas")
                    .select { filter { eq("id", kasId) } }
                    .decodeSingle<Kas>()

                // 3. Kurangi saldo
                val saldoBaru = kasResult.balance - total
                SupabaseClientProvider.client.postgrest
                    .from("kas")
                    .update({ set("balance", saldoBaru) }) {
                        filter { eq("id", kasId) }
                    }

                // 4. Insert ke kas_logs (minus karena pengeluaran)
                SupabaseClientProvider.client.postgrest
                    .from("kas_logs")
                    .insert(KasLogInsert(
                        kasId = kasId,
                        amount = -total,
                        description = "Pengeluaran: $description"
                    ))

                _actionState.value = "Pengeluaran berhasil dicatat!"
                loadExpenses()
                loadKas()
            } catch (e: Exception) {
                _actionState.value = "Gagal menyimpan: ${e.message}"
            }
        }
    }

    fun batalkanPengeluaran(expense: Expense) {
        viewModelScope.launch {
            try {
                // 1. Update status jadi canceled
                SupabaseClientProvider.client.postgrest
                    .from("expenses")
                    .update({ set("status", "canceled") }) {
                        filter { eq("id", expense.id!!) }
                    }

                // 2. Ambil saldo langsung dari database
                val kasResult = SupabaseClientProvider.client.postgrest
                    .from("kas")
                    .select { filter { eq("id", expense.kas_id) } }
                    .decodeSingle<Kas>()

                // 3. Kembalikan saldo
                val saldoBaru = kasResult.balance + expense.total
                SupabaseClientProvider.client.postgrest
                    .from("kas")
                    .update({ set("balance", saldoBaru) }) {
                        filter { eq("id", expense.kas_id) }
                    }

                // 4. Insert ke kas_logs (plus karena saldo dikembalikan)
                SupabaseClientProvider.client.postgrest
                    .from("kas_logs")
                    .insert(KasLogInsert(
                        kasId = expense.kas_id,
                        amount = expense.total,
                        description = "Pembatalan pengeluaran: ${expense.description}"
                    ))

                _actionState.value = "Pengeluaran berhasil dibatalkan!"
                loadExpenses()
                loadKas()
            } catch (e: Exception) {
                _actionState.value = "Gagal membatalkan: ${e.message}"
            }
        }
    }

    fun clearActionState() { _actionState.value = null }
}