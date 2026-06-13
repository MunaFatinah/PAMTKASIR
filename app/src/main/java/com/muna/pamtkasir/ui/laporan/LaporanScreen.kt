package com.muna.pamtkasir.ui.laporan

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun LaporanScreen(
    viewModel: LaporanViewModel = viewModel()
) {

    val laporan by viewModel.laporan.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadLaporan()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Text(
            text = "Laporan Laba / Rugi",
            fontSize = 24.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Total Penjualan : Rp ${laporan.totalPenjualan}"
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Total Pengeluaran : Rp ${laporan.totalPengeluaran}"
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Laba / Rugi : Rp ${laporan.labaRugi}"
        )
    }
}