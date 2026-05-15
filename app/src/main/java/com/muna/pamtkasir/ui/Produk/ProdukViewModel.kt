package com.muna.pamtkasir.ui.produk

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.muna.pamtkasir.SupabaseClientProvider
import com.muna.pamtkasir.model.InventoryLog
import com.muna.pamtkasir.model.Produk
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

sealed class ProdukState {
    object Loading : ProdukState()
    data class Success(val data: List<Produk>) : ProdukState()
    data class Error(val message: String) : ProdukState()
}

sealed class InventoryLogState {
    object Loading : InventoryLogState()
    data class Success(val data: List<InventoryLog>) : InventoryLogState()
    data class Error(val message: String) : InventoryLogState()
}

@Serializable
private data class ProdukInsert(
    val name: String,
    val price: Double,
    val stock: Double,
    @SerialName("user_id") val userId: String,
    @SerialName("is_active") val isActive: Boolean = true
)

@Serializable
private data class ProdukUpdate(
    val name: String,
    val price: Double,
    val stock: Double
)

@Serializable
private data class InventoryLogInsert(
    @SerialName("product_id") val productId: String,
    val activity: String,
    @SerialName("quantity_change") val quantityChange: Double,
    @SerialName("stock_before") val stockBefore: Double,
    @SerialName("stock_after") val stockAfter: Double
)

class ProdukViewModel : ViewModel() {

    private val _produkState = MutableStateFlow<ProdukState>(ProdukState.Loading)
    val produkState: StateFlow<ProdukState> = _produkState

    private val _logState = MutableStateFlow<InventoryLogState>(InventoryLogState.Loading)
    val logState: StateFlow<InventoryLogState> = _logState

    private val _actionState = MutableStateFlow<String?>(null)
    val actionState: StateFlow<String?> = _actionState

    init { fetchProduk() }

    fun fetchProduk() {
        viewModelScope.launch {
            _produkState.value = ProdukState.Loading
            try {
                val result = SupabaseClientProvider.client.postgrest
                    .from("products")
                    .select()
                    .decodeList<Produk>()
                _produkState.value = ProdukState.Success(result)
            } catch (e: Exception) {
                _produkState.value = ProdukState.Error(e.message ?: "Gagal memuat produk")
            }
        }
    }

    fun fetchLog(productId: String) {
        viewModelScope.launch {
            _logState.value = InventoryLogState.Loading
            try {
                val result = SupabaseClientProvider.client.postgrest
                    .from("inventory_logs")
                    .select {
                        filter { eq("product_id", productId) }
                        order("created_at", io.github.jan.supabase.postgrest.query.Order.DESCENDING)
                    }
                    .decodeList<InventoryLog>()
                _logState.value = InventoryLogState.Success(result)
            } catch (e: Exception) {
                _logState.value = InventoryLogState.Error(e.message ?: "Gagal memuat log")
            }
        }
    }

    fun addProduk(name: String, price: Double, stock: Double, userId: String) {
        viewModelScope.launch {
            try {
                val inserted = SupabaseClientProvider.client.postgrest
                    .from("products")
                    .insert(ProdukInsert(name = name, price = price, stock = stock, userId = userId))
                    { select() }
                    .decodeSingle<Produk>()

                // Catat log stok awal
                if (stock > 0) {
                    SupabaseClientProvider.client.postgrest
                        .from("inventory_logs")
                        .insert(InventoryLogInsert(
                            productId      = inserted.id,
                            activity       = "tambah",
                            quantityChange = stock,
                            stockBefore    = 0.0,
                            stockAfter     = stock
                        ))
                }

                _actionState.value = "Produk berhasil ditambahkan"
                fetchProduk()
            } catch (e: Exception) {
                _actionState.value = "Gagal: ${e.message}"
            }
        }
    }

    fun editProduk(id: String, name: String, price: Double, stock: Double) {
        viewModelScope.launch {
            try {
                // Ambil stok lama dulu
                val produkLama = SupabaseClientProvider.client.postgrest
                    .from("products")
                    .select { filter { eq("id", id) } }
                    .decodeSingle<Produk>()

                SupabaseClientProvider.client.postgrest
                    .from("products")
                    .update(ProdukUpdate(name = name, price = price, stock = stock)) {
                        filter { eq("id", id) }
                        select()
                    }

                // Catat log kalau stok berubah
                val selisih = stock - produkLama.stock
                if (selisih != 0.0) {
                    SupabaseClientProvider.client.postgrest
                        .from("inventory_logs")
                        .insert(InventoryLogInsert(
                            productId      = id,
                            activity       = if (selisih > 0) "tambah" else "kurang",
                            quantityChange = selisih,
                            stockBefore    = produkLama.stock,
                            stockAfter     = stock
                        ))
                }

                _actionState.value = "Produk berhasil diubah"
                fetchProduk()
            } catch (e: Exception) {
                _actionState.value = "Gagal: ${e.message}"
            }
        }
    }

    fun clearActionState() { _actionState.value = null }
}