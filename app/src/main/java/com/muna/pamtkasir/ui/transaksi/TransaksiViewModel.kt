package com.muna.pamtkasir.ui.transaksi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.muna.pamtkasir.SupabaseClientProvider
import com.muna.pamtkasir.model.*
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

sealed class TransaksiState {
    object Idle : TransaksiState()
    object Loading : TransaksiState()
    object Success : TransaksiState()
    data class Error(val message: String) : TransaksiState()
}

@Serializable
private data class TransactionInsert(
    @SerialName("user_id") val userId: String,
    @SerialName("customer_id") val customerId: String,
    @SerialName("kas_id") val kasId: String,
    val total: Double,
    @SerialName("paid_amount") val paidAmount: Double,
    @SerialName("change_amount") val changeAmount: Double
)

@Serializable
private data class TransactionItemInsert(
    @SerialName("transaction_id") val transactionId: String,
    @SerialName("product_id") val productId: String,
    val quantity: Double,
    val price: Double
)

@Serializable
private data class StockUpdate(val stock: Double)

@Serializable
private data class BalanceUpdate(val balance: Double)

@Serializable
private data class KasLogInsert(
    @SerialName("kas_id") val kasId: String,
    val amount: Double,
    val description: String
)

@Serializable
private data class InventoryLogInsert(
    @SerialName("product_id") val productId: String,
    val activity: String,
    @SerialName("quantity_change") val quantityChange: Double,
    @SerialName("stock_before") val stockBefore: Double,
    @SerialName("stock_after") val stockAfter: Double
)

class TransaksiViewModel : ViewModel() {

    // ── State ──────────────────────────────────────────────────────────
    private val _pelangganList = MutableStateFlow<List<Customer>>(emptyList())
    val pelangganList: StateFlow<List<Customer>> = _pelangganList

    private val _kasList = MutableStateFlow<List<Kas>>(emptyList())
    val kasList: StateFlow<List<Kas>> = _kasList

    private val _produkList = MutableStateFlow<List<Produk>>(emptyList())
    val produkList: StateFlow<List<Produk>> = _produkList

    private val _cart = MutableStateFlow<List<CartItem>>(emptyList())
    val cart: StateFlow<List<CartItem>> = _cart

    private val _transaksiState = MutableStateFlow<TransaksiState>(TransaksiState.Idle)
    val transaksiState: StateFlow<TransaksiState> = _transaksiState

    val totalHarga: Double get() = _cart.value.sumOf { it.subtotal }

    init {
        fetchAll()
    }

    fun fetchAll() {
        viewModelScope.launch {
            try {
                _pelangganList.value = SupabaseClientProvider.client.postgrest
                    .from("customers")
                    .select { filter { eq("is_active", true) } }
                    .decodeList<Customer>()

                _kasList.value = SupabaseClientProvider.client.postgrest
                    .from("kas")
                    .select { filter { eq("is_active", true) } }
                    .decodeList<Kas>()

                _produkList.value = SupabaseClientProvider.client.postgrest
                    .from("products")
                    .select()
                    .decodeList<Produk>()
            } catch (e: Exception) {
                _transaksiState.value = TransaksiState.Error(e.message ?: "Gagal memuat data")
            }
        }
    }

    // ── Keranjang ──────────────────────────────────────────────────────
    fun tambahKeKeranjang(produk: Produk, qty: Double = 1.0) {
        val current = _cart.value.toMutableList()
        val index = current.indexOfFirst { it.produk.id == produk.id }
        if (index >= 0) {
            val item = current[index]
            val newQty = item.quantity + qty
            if (newQty <= produk.stock) {
                current[index] = item.copy(quantity = newQty)
            }
        } else {
            if (produk.stock >= 1) {
                current.add(CartItem(produk, qty))
            }
        }
        _cart.value = current
    }

    fun kurangiDariKeranjang(produkId: String) {
        val current = _cart.value.toMutableList()
        val index = current.indexOfFirst { it.produk.id == produkId }
        if (index >= 0) {
            val item = current[index]
            if (item.quantity > 1) {
                current[index] = item.copy(quantity = item.quantity - 1)
            } else {
                current.removeAt(index)
            }
        }
        _cart.value = current
    }

    fun hapusDariKeranjang(produkId: String) {
        _cart.value = _cart.value.filter { it.produk.id != produkId }
    }

    fun clearCart() {
        _cart.value = emptyList()
    }

    // ── Proses Transaksi ───────────────────────────────────────────────
    fun prosesTransaksi(
        customerId: String,
        kasId: String,
        paidAmount: Double
    ) {
        viewModelScope.launch {
            _transaksiState.value = TransaksiState.Loading
            try {
                val userId = SupabaseClientProvider.client.auth
                    .currentUserOrNull()?.id
                    ?: run {
                        _transaksiState.value = TransaksiState.Error("User tidak login")
                        return@launch
                    }
                val total = totalHarga
                val change = paidAmount - total

                if (change < 0) {
                    _transaksiState.value = TransaksiState.Error("Uang bayar kurang")
                    return@launch
                }

                if (_cart.value.isEmpty()) {
                    _transaksiState.value = TransaksiState.Error("Keranjang kosong")
                    return@launch
                }

                // 1. Insert transaksi
                val transaksi = SupabaseClientProvider.client.postgrest
                    .from("transactions")
                    .insert(TransactionInsert(
                        userId       = userId,
                        customerId   = customerId,
                        kasId        = kasId,
                        total        = total,
                        paidAmount   = paidAmount,
                        changeAmount = change
                    )) { select() }
                    .decodeSingle<Transaction>()

                // 2. Insert transaction items + update stok + catat inventory log
                for (item in _cart.value) {
                    SupabaseClientProvider.client.postgrest
                        .from("transaction_items")
                        .insert(TransactionItemInsert(
                            transactionId = transaksi.id,
                            productId     = item.produk.id,
                            quantity      = item.quantity,
                            price         = item.produk.price
                        ))

                    val stockBefore = item.produk.stock
                    val stockAfter  = stockBefore - item.quantity

                    SupabaseClientProvider.client.postgrest
                        .from("products")
                        .update(StockUpdate(stock = stockAfter)) {
                            filter { eq("id", item.produk.id) }
                            select()
                        }

                    SupabaseClientProvider.client.postgrest
                        .from("inventory_logs")
                        .insert(InventoryLogInsert(
                            productId      = item.produk.id,
                            activity       = "terjual",
                            quantityChange = -item.quantity,
                            stockBefore    = stockBefore,
                            stockAfter     = stockAfter
                        ))
                }

                // 3. Update saldo kas
                val kas = _kasList.value.first { it.id == kasId }
                val newBalance = kas.balance + total

                SupabaseClientProvider.client.postgrest
                    .from("kas")
                    .update(BalanceUpdate(balance = newBalance)) {
                        filter { eq("id", kasId) }
                        select()
                    }

                // 4. Catat kas log
                SupabaseClientProvider.client.postgrest
                    .from("kas_logs")
                    .insert(KasLogInsert(
                        kasId       = kasId,
                        amount      = total,
                        description = "Penjualan transaksi #${transaksi.id.take(8)}"
                    ))

                clearCart()
                _transaksiState.value = TransaksiState.Success

            } catch (e: Exception) {
                _transaksiState.value = TransaksiState.Error(e.message ?: "Transaksi gagal")
            }
        }
    }

    fun resetState() {
        _transaksiState.value = TransaksiState.Idle
    }
}