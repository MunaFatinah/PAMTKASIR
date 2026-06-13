package com.muna.pamtkasir.ui.laporan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.muna.pamtkasir.SupabaseClientProvider
import com.muna.pamtkasir.model.Expense
import com.muna.pamtkasir.model.ProfitLoss
import com.muna.pamtkasir.model.Transaction
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LaporanViewModel : ViewModel() {

    private val _laporan = MutableStateFlow(ProfitLoss())

    val laporan: StateFlow<ProfitLoss> = _laporan

    fun loadLaporan() {

        viewModelScope.launch {

            try {

                val transaksi =
                    SupabaseClientProvider.client.postgrest
                        .from("transactions")
                        .select()
                        .decodeList<Transaction>()

                val pengeluaran =
                    SupabaseClientProvider.client.postgrest
                        .from("expenses")
                        .select()
                        .decodeList<Expense>()

                val totalPenjualan =
                    transaksi.sumOf { it.total }

                val totalPengeluaran =
                    pengeluaran.sumOf { it.total }

                _laporan.value =
                    ProfitLoss(
                        totalPenjualan = totalPenjualan,
                        totalPengeluaran = totalPengeluaran,
                        labaRugi = totalPenjualan - totalPengeluaran
                    )

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}