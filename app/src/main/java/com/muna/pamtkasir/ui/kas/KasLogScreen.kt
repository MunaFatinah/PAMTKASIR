package com.muna.pamtkasir.ui.kas

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
import com.muna.pamtkasir.model.Kas
import com.muna.pamtkasir.model.KasLog
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

private val BgGreen     = Color(0xFF66B499)
private val HeaderGreen = Color(0xFF1A6651)
private val CardBg      = Color(0xFFF6F6F6)
private val ErrorRed    = Color(0xFFE24B4A)

@Composable
fun KasLogScreen(
    kas: Kas,
    onNavigateBack: () -> Unit
) {
    val viewModel: KasViewModel = viewModel()
    val logState by viewModel.logState.collectAsState()

    LaunchedEffect(kas.id) {
        viewModel.fetchLog(kas.id)
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
                    text       = "Log Kas",
                    color      = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize   = 16.sp
                )
                Text(
                    text     = kas.name,
                    color    = Color.White.copy(alpha = 0.7f),
                    fontSize = 11.sp
                )
            }
        }

        // ── Info Saldo ─────────────────────────────────────────────────
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
                Icons.Outlined.AccountBalance,
                contentDescription = null,
                tint     = Color.White,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text     = "Saldo Saat Ini",
                    color    = Color.White.copy(alpha = 0.7f),
                    fontSize = 11.sp
                )
                Text(
                    text       = formatRupiahLog(kas.balance),
                    color      = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize   = 18.sp
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.White.copy(alpha = 0.2f))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text       = if (kas.isActive) "Aktif" else "Nonaktif",
                    color      = Color.White,
                    fontSize   = 10.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // ── List Log ───────────────────────────────────────────────────
        when (val state = logState) {
            is KasLogState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color.White)
                }
            }

            is KasLogState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Outlined.Warning,
                            contentDescription = null,
                            tint     = ErrorRed,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text     = state.message,
                            color    = Color.White,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            is KasLogState.Success -> {
                if (state.data.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Outlined.History,
                                contentDescription = null,
                                tint     = Color.White.copy(alpha = 0.5f),
                                modifier = Modifier.size(56.dp)
                            )
                            Spacer(Modifier.height(10.dp))
                            Text(
                                text     = "Belum ada transaksi",
                                color    = Color.White.copy(alpha = 0.7f),
                                fontSize = 14.sp
                            )
                            Text(
                                text     = "Transaksi manual akan muncul di sini",
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
private fun LogCard(log: KasLog) {
    val isTambah = log.amount >= 0

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .shadow(3.dp, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .fillMaxWidth()
            .background(CardBg)
            .padding(14.dp)
    ) {
        // Ikon +/-
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
                text       = log.description,
                fontWeight = FontWeight.SemiBold,
                fontSize   = 12.sp,
                color      = Color.Black
            )
            Spacer(Modifier.height(3.dp))
            Text(
                text     = formatTanggalLog(log.createdAt),
                fontSize = 10.sp,
                color    = Color.Gray
            )
        }

        Text(
            text       = "${if (isTambah) "+" else ""}${formatRupiahLog(log.amount)}",
            fontWeight = FontWeight.Bold,
            fontSize   = 13.sp,
            color      = if (isTambah) HeaderGreen else ErrorRed
        )
    }
}

// ── Helper ─────────────────────────────────────────────────────────────────────
private fun formatRupiahLog(amount: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    return format.format(amount).replace("Rp", "Rp ").replace(",00", "")
}

private fun formatTanggalLog(raw: String): String {
    return try {
        val input  = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale("id", "ID"))
        val output = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))
        val date   = input.parse(raw)
        output.format(date!!)
    } catch (e: Exception) {
        try {
            // fallback format tanpa timezone offset
            val input  = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale("id", "ID"))
            val output = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))
            val date   = input.parse(raw)
            output.format(date!!)
        } catch (e2: Exception) {
            raw
        }
    }
}