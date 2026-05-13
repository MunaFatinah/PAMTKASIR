package com.muna.pamtkasir.ui.produk

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.muna.pamtkasir.SupabaseClientProvider
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

@Serializable
private data class ProdukInsert(
    val name: String,
    val price: Double,
    val stock: Double, // Double biar cocok sama tipe float8 di Supabase
    @SerialName("user_id") val userId: String,
    @SerialName("is_active") val isActive: Boolean = true
)

@Serializable
private data class ProdukUpdate(
    val name: String,
    val price: Double,
    val stock: Double // sama, Double
)

class ProdukViewModel : ViewModel() {

    private val _produkState = MutableStateFlow<ProdukState>(ProdukState.Loading)
    val produkState: StateFlow<ProdukState> = _produkState

    private val _actionState = MutableStateFlow<String?>(null)
    val actionState: StateFlow<String?> = _actionState

    init { fetchProduk() }

    fun fetchProduk() {
        viewModelScope.launch {
            _produkState.value = ProdukState.Loading
            try {
                val result = SupabaseClientProvider.client.postgrest
                    .from("products") // nama tabel di Supabase adalah "products"
                    .select()
                    .decodeList<Produk>()
                _produkState.value = ProdukState.Success(result)
            } catch (e: Exception) {
                _produkState.value = ProdukState.Error(e.message ?: "Gagal memuat produk")
            }
        }
    }

    // Tambah userId sebagai parameter karena dibutuhkan saat insert
    fun addProduk(name: String, price: Double, stock: Double, userId: String) {
        viewModelScope.launch {
            try {
                SupabaseClientProvider.client.postgrest
                    .from("products")
                    .insert(ProdukInsert(
                        name   = name,
                        price  = price,
                        stock  = stock,
                        userId = userId
                    )) { select() }
                _actionState.value = "Produk berhasil ditambahkan"
                fetchProduk()
            } catch (e: Exception) {
                _actionState.value = "Gagal: ${e.message}"
            }
        }
    }

    // stock pakai Double
    fun editProduk(id: String, name: String, price: Double, stock: Double) {
        viewModelScope.launch {
            try {
                SupabaseClientProvider.client.postgrest
                    .from("products")
                    .update(ProdukUpdate(name = name, price = price, stock = stock)) {
                        filter { eq("id", id) }
                        select()
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