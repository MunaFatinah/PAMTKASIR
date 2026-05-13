package com.muna.pamtkasir.ui.kas

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.muna.pamtkasir.R
import com.muna.pamtkasir.SupabaseClientProvider
import com.muna.pamtkasir.model.Kas
import io.github.jan.supabase.auth.auth

private val BgGreen     = Color(0xFF66B499)
private val HeaderGreen = Color(0xFF1A6651)
private val CardBg      = Color(0xFFF6F6F6)
private val ErrorRed    = Color(0xFFE24B4A)

@Composable
fun KasScreen(
    onNavigateToLog: (Kas) -> Unit = {}
) {
    val viewModel: KasViewModel = viewModel()
    val kasState    by viewModel.kasState.collectAsState()
    val actionState by viewModel.actionState.collectAsState()

    var showAddDialog       by remember { mutableStateOf(false) }
    var showEditDialog      by remember { mutableStateOf<Kas?>(null) }
    var showTransaksiDialog by remember { mutableStateOf<Kas?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(actionState) {
        actionState?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearActionState()
        }
    }

    Scaffold(
        snackbarHost   = { SnackbarHost(snackbarHostState) },
        containerColor = BgGreen
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(BgGreen)
        ) {

            // ── Top Bar ────────────────────────────────────────────────
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(HeaderGreen)
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter            = painterResource(id = R.drawable.logo_kasirku),
                        contentDescription = "Logo",
                        modifier = Modifier
                            .padding(end = 11.dp)
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                    Column {
                        Text(
                            text       = "KASIRKU",
                            color      = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize   = 16.sp
                        )
                        Text(
                            text     = "Manajemen Kas",
                            color    = Color.White.copy(alpha = 0.7f),
                            fontSize = 11.sp
                        )
                    }
                }
                IconButton(
                    onClick  = { showAddDialog = true },
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.15f))
                ) {
                    Icon(Icons.Outlined.Add, contentDescription = "Tambah Kas", tint = Color.White)
                }
            }

            // ── Content ────────────────────────────────────────────────
            when (val state = kasState) {

                is KasState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color.White)
                    }
                }

                is KasState.Error -> {
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
                            Spacer(Modifier.height(12.dp))
                            OutlinedButton(
                                onClick = { viewModel.fetchKas() },
                                border  = BorderStroke(1.dp, Color.White)
                            ) {
                                Text("Coba Lagi", color = Color.White)
                            }
                        }
                    }
                }

                is KasState.Success -> {
                    if (state.data.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Outlined.Inbox,
                                    contentDescription = null,
                                    tint     = Color.White.copy(alpha = 0.5f),
                                    modifier = Modifier.size(56.dp)
                                )
                                Spacer(Modifier.height(10.dp))
                                Text(
                                    "Belum ada kas",
                                    color    = Color.White.copy(alpha = 0.7f),
                                    fontSize = 14.sp
                                )
                                Text(
                                    "Tap + untuk menambah kas baru",
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
                                top    = 16.dp,
                                bottom = 24.dp
                            ),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(state.data) { kas ->
                                KasCard(
                                    kas         = kas,
                                    onLihatLog  = { onNavigateToLog(kas) },
                                    onEdit      = { showEditDialog = kas },
                                    onToggle    = { viewModel.toggleAktif(kas.id, kas.isActive) },
                                    onTransaksi = { showTransaksiDialog = kas }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // ── Dialog Tambah Kas ──────────────────────────────────────────────
    if (showAddDialog) {
        AddKasDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, saldo ->
                val userId = SupabaseClientProvider.client.auth.currentUserOrNull()?.id ?: ""
                viewModel.addKas(name, saldo, userId)
                showAddDialog = false
            }
        )
    }

    // ── Dialog Edit Nama ───────────────────────────────────────────────
    showEditDialog?.let { kas ->
        EditNamaDialog(
            namaAwal  = kas.name,
            onDismiss = { showEditDialog = null },
            onConfirm = { newName ->
                viewModel.editNamaKas(kas.id, newName)
                showEditDialog = null
            }
        )
    }

    // ── Dialog Transaksi ───────────────────────────────────────────────
    showTransaksiDialog?.let { kas ->
        TransaksiDialog(
            kas       = kas,
            onDismiss = { showTransaksiDialog = null },
            onConfirm = { amount, deskripsi, isTambah ->
                viewModel.transaksiManual(kas, amount, deskripsi, isTambah)
                showTransaksiDialog = null
            }
        )
    }
}

// ── Kas Card ───────────────────────────────────────────────────────────────────
@Composable
private fun KasCard(
    kas         : Kas,
    onLihatLog  : () -> Unit,
    onEdit      : () -> Unit,
    onToggle    : () -> Unit,
    onTransaksi : () -> Unit
) {
    Column(
        modifier = Modifier
            .shadow(3.dp, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .fillMaxWidth()
            .background(CardBg)
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier          = Modifier.fillMaxWidth()
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(HeaderGreen.copy(alpha = 0.12f))
            ) {
                Icon(
                    Icons.Outlined.AccountBalanceWallet,
                    contentDescription = null,
                    tint     = HeaderGreen,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = kas.name,
                    fontWeight = FontWeight.SemiBold,
                    fontSize   = 14.sp,
                    color      = Color.Black
                )
                Text(
                    text       = formatRupiah(kas.balance),
                    fontSize   = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color      = HeaderGreen
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        if (kas.isActive) HeaderGreen.copy(alpha = 0.12f)
                        else Color.Gray.copy(alpha = 0.12f)
                    )
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text       = if (kas.isActive) "Aktif" else "Nonaktif",
                    color      = if (kas.isActive) HeaderGreen else Color.Gray,
                    fontSize   = 10.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(Modifier.height(12.dp))
        HorizontalDivider(color = Color.Black.copy(alpha = 0.06f))
        Spacer(Modifier.height(10.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier              = Modifier.fillMaxWidth()
        ) {
            KasActionButton(
                icon     = Icons.Outlined.History,
                label    = "Log",
                modifier = Modifier.weight(1f),
                onClick  = onLihatLog
            )
            KasActionButton(
                icon     = Icons.Outlined.Edit,
                label    = "Edit",
                modifier = Modifier.weight(1f),
                onClick  = onEdit
            )
            KasActionButton(
                icon     = if (kas.isActive) Icons.Outlined.ToggleOff else Icons.Outlined.ToggleOn,
                label    = if (kas.isActive) "Nonaktif" else "Aktifkan",
                modifier = Modifier.weight(1f),
                onClick  = onToggle
            )
            KasActionButton(
                icon     = Icons.Outlined.SwapVert,
                label    = "Transaksi",
                modifier = Modifier.weight(1f),
                onClick  = onTransaksi
            )
        }
    }
}

@Composable
private fun KasActionButton(
    icon     : androidx.compose.ui.graphics.vector.ImageVector,
    label    : String,
    modifier : Modifier = Modifier,
    onClick  : () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .background(HeaderGreen.copy(alpha = 0.07f))
            .padding(vertical = 8.dp)
    ) {
        Icon(icon, contentDescription = label, tint = HeaderGreen, modifier = Modifier.size(18.dp))
        Spacer(Modifier.height(2.dp))
        Text(label, fontSize = 9.sp, color = HeaderGreen, fontWeight = FontWeight.Medium)
    }
}

// ── Dialog Tambah Kas ──────────────────────────────────────────────────────────
@Composable
private fun AddKasDialog(
    onDismiss : () -> Unit,
    onConfirm : (String, Double) -> Unit
) {
    var nama  by remember { mutableStateOf("") }
    var saldo by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = CardBg,
        title = { Text("Tambah Kas", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
        text  = {
            Column {
                OutlinedTextField(
                    value         = nama,
                    onValueChange = { nama = it },
                    label         = { Text("Nama Kas") },
                    singleLine    = true,
                    modifier      = Modifier.fillMaxWidth(),
                    shape         = RoundedCornerShape(10.dp),
                    colors        = kasDialogFieldColors()
                )
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value           = saldo,
                    onValueChange   = { saldo = it.filter { c -> c.isDigit() || c == '.' } },
                    label           = { Text("Saldo Awal") },
                    singleLine      = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier        = Modifier.fillMaxWidth(),
                    shape           = RoundedCornerShape(10.dp),
                    colors          = kasDialogFieldColors()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val s = saldo.toDoubleOrNull() ?: 0.0
                    if (nama.isNotBlank()) onConfirm(nama, s)
                },
                colors = ButtonDefaults.buttonColors(containerColor = HeaderGreen),
                shape  = RoundedCornerShape(8.dp)
            ) { Text("Simpan") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal", color = Color.Gray) }
        }
    )
}

// ── Dialog Edit Nama ───────────────────────────────────────────────────────────
@Composable
private fun EditNamaDialog(
    namaAwal  : String,
    onDismiss : () -> Unit,
    onConfirm : (String) -> Unit
) {
    var nama by remember { mutableStateOf(namaAwal) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = CardBg,
        title = { Text("Edit Nama Kas", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
        text  = {
            OutlinedTextField(
                value         = nama,
                onValueChange = { nama = it },
                label         = { Text("Nama Kas") },
                singleLine    = true,
                modifier      = Modifier.fillMaxWidth(),
                shape         = RoundedCornerShape(10.dp),
                colors        = kasDialogFieldColors()
            )
        },
        confirmButton = {
            Button(
                onClick = { if (nama.isNotBlank()) onConfirm(nama) },
                colors  = ButtonDefaults.buttonColors(containerColor = HeaderGreen),
                shape   = RoundedCornerShape(8.dp)
            ) { Text("Simpan") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal", color = Color.Gray) }
        }
    )
}

// ── Dialog Transaksi ───────────────────────────────────────────────────────────
@Composable
private fun TransaksiDialog(
    kas       : Kas,
    onDismiss : () -> Unit,
    onConfirm : (Double, String, Boolean) -> Unit
) {
    var amount    by remember { mutableStateOf("") }
    var deskripsi by remember { mutableStateOf("") }
    var isTambah  by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = CardBg,
        title = {
            Text(
                "Transaksi — ${kas.name}",
                fontWeight = FontWeight.Bold,
                fontSize   = 15.sp
            )
        },
        text = {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Black.copy(alpha = 0.05f))
                        .padding(4.dp)
                ) {
                    listOf(true to "Tambah", false to "Kurang").forEach { (value, label) ->
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    if (isTambah == value) HeaderGreen else Color.Transparent
                                )
                                .clickable { isTambah = value }
                                .padding(vertical = 8.dp)
                        ) {
                            Text(
                                text       = label,
                                color      = if (isTambah == value) Color.White else Color.Gray,
                                fontSize   = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value           = amount,
                    onValueChange   = { amount = it.filter { c -> c.isDigit() || c == '.' } },
                    label           = { Text("Jumlah") },
                    singleLine      = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier        = Modifier.fillMaxWidth(),
                    shape           = RoundedCornerShape(10.dp),
                    colors          = kasDialogFieldColors()
                )

                Spacer(Modifier.height(10.dp))

                OutlinedTextField(
                    value         = deskripsi,
                    onValueChange = { deskripsi = it },
                    label         = { Text("Keterangan") },
                    singleLine    = true,
                    modifier      = Modifier.fillMaxWidth(),
                    shape         = RoundedCornerShape(10.dp),
                    colors        = kasDialogFieldColors()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val a = amount.toDoubleOrNull() ?: 0.0
                    if (a > 0 && deskripsi.isNotBlank()) onConfirm(a, deskripsi, isTambah)
                },
                colors = ButtonDefaults.buttonColors(containerColor = HeaderGreen),
                shape  = RoundedCornerShape(8.dp)
            ) { Text("Simpan") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal", color = Color.Gray) }
        }
    )
}

// ── Helper ─────────────────────────────────────────────────────────────────────
@Composable
private fun kasDialogFieldColors() = OutlinedTextFieldDefaults.colors(
    unfocusedContainerColor = Color.White,
    focusedContainerColor   = Color.White,
    unfocusedBorderColor    = Color(0xFFDDDDDD),
    focusedBorderColor      = HeaderGreen,
    unfocusedTextColor      = Color.Black,
    focusedTextColor        = Color.Black,
    cursorColor             = HeaderGreen
)