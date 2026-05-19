package com.muna.pamtkasir.ui.inventorylog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.muna.pamtkasir.SupabaseClientProvider
import com.muna.pamtkasir.model.InventoryLog
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class InventoryLogState {
    object Loading : InventoryLogState()
    data class Success(val data: List<InventoryLog>) : InventoryLogState()
    data class Error(val message: String) : InventoryLogState()
}

class InventoryLogViewModel : ViewModel() {

    private val _logState = MutableStateFlow<InventoryLogState>(InventoryLogState.Loading)
    val logState: StateFlow<InventoryLogState> = _logState

    fun fetchLog(productId: String) {
        viewModelScope.launch {
            _logState.value = InventoryLogState.Loading
            try {
                val result = SupabaseClientProvider.client.postgrest
                    .from("inventory_logs")
                    .select {
                        filter { eq("product_id", productId) }
                        order("created_at", Order.DESCENDING)
                    }
                    .decodeList<InventoryLog>()
                _logState.value = InventoryLogState.Success(result)
            } catch (e: Exception) {
                _logState.value = InventoryLogState.Error(e.message ?: "Gagal memuat log")
            }
        }
    }
}