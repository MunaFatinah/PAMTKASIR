package com.muna.pamtkasir.ui.pelanggan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.muna.pamtkasir.SupabaseClientProvider
import com.muna.pamtkasir.model.Customer
import com.muna.pamtkasir.model.CustomerLog
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

sealed class PelangganState {
    object Loading : PelangganState()
    data class Success(val data: List<Customer>) : PelangganState()
    data class Error(val message: String) : PelangganState()
}

sealed class PelangganLogState {
    object Loading : PelangganLogState()
    data class Success(val data: List<CustomerLog>) : PelangganLogState()
    data class Error(val message: String) : PelangganLogState()
}

@Serializable
private data class CustomerInsert(
    val name: String,
    val phone: String,
    @SerialName("user_id") val userId: String,
    @SerialName("is_active") val isActive: Boolean = true
)

@Serializable
private data class CustomerUpdate(
    val name: String,
    val phone: String
)

@Serializable
private data class ActiveUpdate(
    @SerialName("is_active") val isActive: Boolean
)

@Serializable
private data class CustomerLogInsert(
    @SerialName("customer_id") val customerId: String,
    @SerialName("activity_type") val activityType: String
)

class PelangganViewModel : ViewModel() {

    private val _pelangganState = MutableStateFlow<PelangganState>(PelangganState.Loading)
    val pelangganState: StateFlow<PelangganState> = _pelangganState

    private val _logState = MutableStateFlow<PelangganLogState>(PelangganLogState.Loading)
    val logState: StateFlow<PelangganLogState> = _logState

    private val _actionState = MutableStateFlow<String?>(null)
    val actionState: StateFlow<String?> = _actionState

    init { fetchPelanggan() }

    fun fetchPelanggan() {
        viewModelScope.launch {
            _pelangganState.value = PelangganState.Loading
            try {
                val result = SupabaseClientProvider.client.postgrest
                    .from("customers")
                    .select()
                    .decodeList<Customer>()
                _pelangganState.value = PelangganState.Success(result)
            } catch (e: Exception) {
                _pelangganState.value = PelangganState.Error(e.message ?: "Gagal memuat pelanggan")
            }
        }
    }

    fun fetchLog(customerId: String) {
        viewModelScope.launch {
            _logState.value = PelangganLogState.Loading
            try {
                val result = SupabaseClientProvider.client.postgrest
                    .from("customer_logs")
                    .select {
                        filter { eq("customer_id", customerId) }
                        order("created_at", Order.DESCENDING)
                    }
                    .decodeList<CustomerLog>()
                _logState.value = PelangganLogState.Success(result)
            } catch (e: Exception) {
                _logState.value = PelangganLogState.Error(e.message ?: "Gagal memuat log")
            }
        }
    }

    fun addPelanggan(name: String, phone: String, userId: String) {
        viewModelScope.launch {
            try {
                val customer = SupabaseClientProvider.client.postgrest
                    .from("customers")
                    .insert(CustomerInsert(name = name, phone = phone, userId = userId)) {
                        select()
                    }
                    .decodeSingle<Customer>()

                SupabaseClientProvider.client.postgrest
                    .from("customer_logs")
                    .insert(CustomerLogInsert(customerId = customer.id, activityType = "create"))

                _actionState.value = "Pelanggan berhasil ditambahkan"
                fetchPelanggan()
            } catch (e: Exception) {
                _actionState.value = "Gagal: ${e.message}"
            }
        }
    }

    fun editPelanggan(id: String, name: String, phone: String) {
        viewModelScope.launch {
            try {
                SupabaseClientProvider.client.postgrest
                    .from("customers")
                    .update(CustomerUpdate(name = name, phone = phone)) {
                        filter { eq("id", id) }
                        select()
                    }

                SupabaseClientProvider.client.postgrest
                    .from("customer_logs")
                    .insert(CustomerLogInsert(customerId = id, activityType = "update"))

                _actionState.value = "Data pelanggan berhasil diubah"
                fetchPelanggan()
            } catch (e: Exception) {
                _actionState.value = "Gagal: ${e.message}"
            }
        }
    }

    fun toggleAktif(id: String, currentStatus: Boolean) {
        viewModelScope.launch {
            try {
                SupabaseClientProvider.client.postgrest
                    .from("customers")
                    .update(ActiveUpdate(isActive = !currentStatus)) {
                        filter { eq("id", id) }
                        select()
                    }

                SupabaseClientProvider.client.postgrest
                    .from("customer_logs")
                    .insert(CustomerLogInsert(customerId = id, activityType = "update"))

                _actionState.value = if (currentStatus) "Pelanggan dinonaktifkan" else "Pelanggan diaktifkan"
                fetchPelanggan()
            } catch (e: Exception) {
                _actionState.value = "Gagal: ${e.message}"
            }
        }
    }

    fun clearActionState() { _actionState.value = null }
}