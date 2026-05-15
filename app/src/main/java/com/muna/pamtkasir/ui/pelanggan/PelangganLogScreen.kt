package com.muna.pamtkasir.ui.pelanggan

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
import com.muna.pamtkasir.model.Customer
import com.muna.pamtkasir.model.CustomerLog
import java.text.SimpleDateFormat
import java.util.Locale

private val BgGreen     = Color(0xFF66B499)
private val HeaderGreen = Color(0xFF1A6651)
private val CardBg      = Color(0xFFF6F6F6)
private val ErrorRed    = Color(0xFFE24B4A)

@Composable
fun PelangganLogScreen(
    customer: Customer,
    onNavigateBack: () -> Unit
) {
    val viewModel: PelangganViewModel = viewModel()
    val logState by viewModel.logState.collectAsState()

    LaunchedEffect(customer.id) {
        viewModel.fetchLog(customer.id)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgGreen)
    ) {
        // ── Top Bar ────────────────────────────────────────────────────
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
                    text       = "Log Aktivitas",
                    color      = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize   = 16.sp
                )
                Text(
                    text     = customer.name,
                    color    = Color.White.copy(alpha = 0.7f),
                    fontSize = 11.sp
                )
            }
        }

        // ── Info Pelanggan ─────────────────────────────────────────────
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
                Icons.Outlined.Person,
                contentDescription = null,
                tint     = Color.White,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text     = customer.name,
                    color    = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Text(
                    text     = customer.phone.ifBlank { "Tidak ada nomor" },
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
                    text       = if (customer.isActive) "Aktif" else "Nonaktif",
                    color      = Color.White,
                    fontSize   = 10.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // ── List Log ───────────────────────────────────────────────────
        when (val state = logState) {
            is PelangganLogState.Loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color.White)
                }
            }

            is PelangganLogState.Error -> {
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

            is PelangganLogState.Success -> {
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
                                "Belum ada aktivitas",
                                color    = Color.White.copy(alpha = 0.7f),
                                fontSize = 14.sp
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
                            LogCard(log = log)
                        }
                    }
                }
            }
        }
    }
}

// ── Log Card ──────────────────────────────────────────────────────────────────
@Composable
private fun LogCard(log: CustomerLog) {
    val isCreate = log.activityType == "create"

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
                    if (isCreate) HeaderGreen.copy(alpha = 0.12f)
                    else Color(0xFF2196F3).copy(alpha = 0.12f)
                )
        ) {
            Icon(
                imageVector = if (isCreate) Icons.Outlined.PersonAdd else Icons.Outlined.Edit,
                contentDescription = null,
                tint     = if (isCreate) HeaderGreen else Color(0xFF2196F3),
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text       = if (isCreate) "Pelanggan Terdaftar" else "Data Diperbarui",
                fontWeight = FontWeight.SemiBold,
                fontSize   = 12.sp,
                color      = Color.Black
            )
            Spacer(Modifier.height(3.dp))
            Text(
                text     = formatTanggal(log.createdAt),
                fontSize = 10.sp,
                color    = Color.Gray
            )
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(
                    if (isCreate) HeaderGreen.copy(alpha = 0.10f)
                    else Color(0xFF2196F3).copy(alpha = 0.10f)
                )
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text       = log.activityType,
                color      = if (isCreate) HeaderGreen else Color(0xFF2196F3),
                fontSize   = 9.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

// ── Helper ─────────────────────────────────────────────────────────────────────
private fun formatTanggal(raw: String): String {
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