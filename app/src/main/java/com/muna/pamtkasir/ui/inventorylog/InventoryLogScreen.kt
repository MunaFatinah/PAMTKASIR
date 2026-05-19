package com.muna.pamtkasir.ui.inventorylog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.muna.pamtkasir.model.InventoryLog
import com.muna.pamtkasir.model.Produk
import java.text.SimpleDateFormat
import java.util.Locale

private val BgGreen     = Color(0xFF66B499)
private val HeaderGreen = Color(0xFF1A6651)
private val CardBg      = Color(0xFFF6F6F6)
private val ErrorRed    = Color(0xFFE24B4A)

@Composable
fun InventoryLogScreen(
    produk: Produk,
    onNavigateBack: () -> Unit,
    viewModel: InventoryLogViewModel = viewModel()
) {
    val logState by viewModel.logState.collectAsState()

    LaunchedEffect(produk.id) {
        viewModel.fetchLog(produk.id)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgGreen)
    ) {
        // Top Bar
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .background(HeaderGreen)
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    Icons.Outlined.ArrowBack,
                    contentDescription = "Kembali",
                    tint = Color.White
                )
            }
            Spacer(Modifier.width(4.dp))
            Column {
                Text(
                    text       = "Log Inventori",
                    color      = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize   = 16.sp
                )
                Text(
                    text     = produk.name,
                    color    = Color.White.copy(alpha = 0.7f),
                    fontSize = 11.sp
                )
            }
        }

        // Info Produk
        Row(
            modifier = Modifier
                .padding(horizontal = 20.dp, vertical = 14.dp)
                .shadow(4.dp, RoundedCornerShape(12.dp))
                .clip(RoundedCornerShape(12.dp))
                .fillMaxWidth()
                .background(HeaderGreen)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Outlined.ShoppingBag,
                contentDescription = null,
                tint     = Color.White,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = produk.name,
                    color      = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize   = 15.sp
                )
                Text(
                    text     = "Stok saat ini: ${produk.stock.toInt()}",
                    color    = Color.White.copy(alpha = 0.7f),
                    fontSize = 11.sp
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.White.copy(alpha = 0.2f))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text       = if (produk.isActive) "Aktif" else "Nonaktif",
                    color      = Color.White,
                    fontSize   = 10.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // List Log
        when (val state = logState) {
            is InventoryLogState.Loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color.White)
                }
            }

            is InventoryLogState.Error -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Outlined.Warning,
                            contentDescription = null,
                            tint     = ErrorRed,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(state.message, color = Color.White, fontSize = 13.sp)
                    }
                }
            }

            is InventoryLogState.Success -> {
                if (state.data.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Outlined.History,
                                contentDescription = null,
                                tint     = Color.White.copy(alpha = 0.5f),
                                modifier = Modifier.size(56.dp)
                            )
                            Spacer(Modifier.height(10.dp))
                            Text(
                                "Belum ada riwayat stok",
                                color    = Color.White.copy(alpha = 0.7f),
                                fontSize = 14.sp
                            )
                            Text(
                                "Log akan muncul saat stok berubah",
                                color    = Color.White.copy(alpha = 0.5f),
                                fontSize = 12.sp
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(
                            start  = 20.dp,
                            end    = 20.dp,
                            top    = 4.dp,
                            bottom = 24.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(state.data) { log ->
                            InventoryLogCard(log = log)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InventoryLogCard(log: InventoryLog) {
    val isTambah = log.quantityChange >= 0

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .shadow(3.dp, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .fillMaxWidth()
            .background(CardBg)
            .padding(14.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(
                    if (isTambah) HeaderGreen.copy(alpha = 0.12f)
                    else ErrorRed.copy(alpha = 0.12f)
                )
        ) {
            Icon(
                imageVector = if (isTambah) Icons.Outlined.ArrowUpward
                else Icons.Outlined.ArrowDownward,
                contentDescription = null,
                tint     = if (isTambah) HeaderGreen else ErrorRed,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text       = log.activity,
                fontWeight = FontWeight.SemiBold,
                fontSize   = 12.sp,
                color      = Color.Black
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text     = "${log.stockBefore.toInt()} → ${log.stockAfter.toInt()}",
                fontSize = 11.sp,
                color    = Color.Gray
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text     = formatTanggalInv(log.createdAt),
                fontSize = 10.sp,
                color    = Color.Gray
            )
        }

        Text(
            text       = "${if (isTambah) "+" else ""}${log.quantityChange.toInt()}",
            fontWeight = FontWeight.Bold,
            fontSize   = 14.sp,
            color      = if (isTambah) HeaderGreen else ErrorRed
        )
    }
}

private fun formatTanggalInv(raw: String): String {
    return try {
        val input  = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale("id", "ID"))
        val output = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))
        output.format(input.parse(raw)!!)
    } catch (e: Exception) {
        try {
            val input  = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale("id", "ID"))
            val output = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))
            output.format(input.parse(raw)!!)
        } catch (e2: Exception) { raw }
    }
}