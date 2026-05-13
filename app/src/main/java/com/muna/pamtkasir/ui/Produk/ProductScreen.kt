package com.muna.pamtkasir.ui.produk

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
import com.muna.pamtkasir.model.Produk
import com.muna.pamtkasir.ui.kas.formatRupiah
import io.github.jan.supabase.auth.auth


// ── Warna (sama dengan KasScreen biar konsisten) ───────────────────────────────
private val BgGreen     = Color(0xFF66B499)
private val HeaderGreen = Color(0xFF1A6651)
private val CardBg      = Color(0xFFF6F6F6)
private val ErrorRed    = Color(0xFFE24B4A)

@Composable
fun ProdukScreen() {
    // Ambil ViewModel dan observe state-nya
    val viewModel: ProdukViewModel = viewModel()
    val produkState by viewModel.produkState.collectAsState()
    val actionState by viewModel.actionState.collectAsState()

    // State untuk kontrol dialog mana yang tampil
    var showAddDialog  by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf<Produk?>(null) }

    // Snackbar untuk notifikasi berhasil/gagal
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
                    // Logo aplikasi
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
                            text     = "Manajemen Produk",
                            color    = Color.White.copy(alpha = 0.7f),
                            fontSize = 11.sp
                        )
                    }
                }
                // Tombol tambah produk baru
                IconButton(
                    onClick  = { showAddDialog = true },
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.15f))
                ) {
                    Icon(Icons.Outlined.Add, contentDescription = "Tambah Produk", tint = Color.White)
                }
            }

            // ── Isi Konten berdasarkan state ───────────────────────────
            when (val state = produkState) {

                // Loading: tampilkan spinner
                is ProdukState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color.White)
                    }
                }

                // Error: tampilkan pesan dan tombol retry
                is ProdukState.Error -> {
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
                                onClick = { viewModel.fetchProduk() },
                                border  = BorderStroke(1.dp, Color.White)
                            ) {
                                Text("Coba Lagi", color = Color.White)
                            }
                        }
                    }
                }

                // Sukses: tampilkan list atau pesan kosong
                is ProdukState.Success -> {
                    if (state.data.isEmpty()) {
                        // Belum ada produk
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Outlined.Inventory2,
                                    contentDescription = null,
                                    tint     = Color.White.copy(alpha = 0.5f),
                                    modifier = Modifier.size(56.dp)
                                )
                                Spacer(Modifier.height(10.dp))
                                Text(
                                    "Belum ada produk",
                                    color    = Color.White.copy(alpha = 0.7f),
                                    fontSize = 14.sp
                                )
                                Text(
                                    "Tap + untuk menambah produk baru",
                                    color    = Color.White.copy(alpha = 0.5f),
                                    fontSize = 12.sp
                                )
                            }
                        }
                    } else {
                        // Ada data: tampilkan list kartu produk
                        LazyColumn(
                            contentPadding = PaddingValues(
                                start  = 20.dp,
                                end    = 20.dp,
                                top    = 16.dp,
                                bottom = 24.dp
                            ),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(state.data) { produk ->
                                ProdukCard(
                                    produk = produk,
                                    onEdit = { showEditDialog = produk }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // ── Dialog Tambah Produk ───────────────────────────────────────────
    if (showAddDialog) {
        AddProdukDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, price, stock ->
                // Ambil user ID yang sedang login
                val userId = SupabaseClientProvider.client.auth.currentUserOrNull()?.id ?: ""
                viewModel.addProduk(name, price, stock, userId)
                showAddDialog = false
            }
        )
    }

    // ── Dialog Edit Produk ─────────────────────────────────────────────
    showEditDialog?.let { produk ->
        EditProdukDialog(
            produk    = produk,
            onDismiss = { showEditDialog = null },
            onConfirm = { name, price, stock ->
                viewModel.editProduk(produk.id, name, price, stock)
                showEditDialog = null
            }
        )
    }
}

// ── Kartu satu produk ──────────────────────────────────────────────────────────
@Composable
private fun ProdukCard(
    produk : Produk,
    onEdit : () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .shadow(3.dp, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .fillMaxWidth()
            .background(CardBg)
            .padding(14.dp)
    ) {
        // Ikon produk
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(HeaderGreen.copy(alpha = 0.12f))
        ) {
            Icon(
                Icons.Outlined.ShoppingBag,
                contentDescription = null,
                tint     = HeaderGreen,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(Modifier.width(12.dp))

        // Info produk: nama, harga, stok
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text       = produk.name,
                fontWeight = FontWeight.SemiBold,
                fontSize   = 13.sp,
                color      = Color.Black
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text       = formatRupiah(produk.price),
                fontSize   = 12.sp,
                color      = HeaderGreen,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(2.dp))
            // Stok merah kalau habis, abu-abu kalau masih ada
            Text(
                text     = "Stok: ${produk.stock.toInt()}",
                fontSize = 11.sp,
                color    = if (produk.stock > 0) Color.Gray else ErrorRed
            )
        }

        // Tombol edit
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(HeaderGreen.copy(alpha = 0.12f))
                .clickable { onEdit() }
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(
                text       = "Edit",
                color      = HeaderGreen,
                fontSize   = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

// ── Dialog untuk tambah produk baru ───────────────────────────────────────────
@Composable
private fun AddProdukDialog(
    onDismiss : () -> Unit,
    onConfirm : (String, Double, Double) -> Unit  // nama, harga, stok
) {
    var name  by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var stock by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = CardBg,
        title = { Text("Tambah Produk", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
        text  = {
            Column {
                OutlinedTextField(
                    value         = name,
                    onValueChange = { name = it },
                    label         = { Text("Nama Produk") },
                    singleLine    = true,
                    modifier      = Modifier.fillMaxWidth(),
                    shape         = RoundedCornerShape(10.dp),
                    colors        = produkFieldColors()
                )
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value           = price,
                    onValueChange   = { price = it.filter { c -> c.isDigit() || c == '.' } },
                    label           = { Text("Harga") },
                    singleLine      = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier        = Modifier.fillMaxWidth(),
                    shape           = RoundedCornerShape(10.dp),
                    colors          = produkFieldColors()
                )
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value           = stock,
                    onValueChange   = { stock = it.filter { c -> c.isDigit() } },
                    label           = { Text("Stok") },
                    singleLine      = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier        = Modifier.fillMaxWidth(),
                    shape           = RoundedCornerShape(10.dp),
                    colors          = produkFieldColors()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val p = price.toDoubleOrNull() ?: 0.0
                    val s = stock.toDoubleOrNull() ?: 0.0
                    // Hanya simpan kalau nama tidak kosong
                    if (name.isNotBlank()) onConfirm(name, p, s)
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

// ── Dialog untuk edit produk yang sudah ada ────────────────────────────────────
@Composable
private fun EditProdukDialog(
    produk    : Produk,
    onDismiss : () -> Unit,
    onConfirm : (String, Double, Double) -> Unit  // nama, harga, stok
) {
    // Pre-fill field dengan data produk yang dipilih
    var name  by remember { mutableStateOf(produk.name) }
    var price by remember { mutableStateOf(produk.price.toInt().toString()) }
    var stock by remember { mutableStateOf(produk.stock.toInt().toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = CardBg,
        title = { Text("Edit Produk", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
        text  = {
            Column {
                OutlinedTextField(
                    value         = name,
                    onValueChange = { name = it },
                    label         = { Text("Nama Produk") },
                    singleLine    = true,
                    modifier      = Modifier.fillMaxWidth(),
                    shape         = RoundedCornerShape(10.dp),
                    colors        = produkFieldColors()
                )
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value           = price,
                    onValueChange   = { price = it.filter { c -> c.isDigit() || c == '.' } },
                    label           = { Text("Harga") },
                    singleLine      = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier        = Modifier.fillMaxWidth(),
                    shape           = RoundedCornerShape(10.dp),
                    colors          = produkFieldColors()
                )
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value           = stock,
                    onValueChange   = { stock = it.filter { c -> c.isDigit() } },
                    label           = { Text("Stok") },
                    singleLine      = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier        = Modifier.fillMaxWidth(),
                    shape           = RoundedCornerShape(10.dp),
                    colors          = produkFieldColors()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val p = price.toDoubleOrNull() ?: 0.0
                    val s = stock.toDoubleOrNull() ?: 0.0
                    if (name.isNotBlank()) onConfirm(name, p, s)
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

// ── Warna field yang konsisten di semua dialog ─────────────────────────────────
@Composable
private fun produkFieldColors() = OutlinedTextFieldDefaults.colors(
    unfocusedContainerColor = Color.White,
    focusedContainerColor   = Color.White,
    unfocusedBorderColor    = Color(0xFFDDDDDD),
    focusedBorderColor      = HeaderGreen,
    unfocusedTextColor      = Color.Black,
    focusedTextColor        = Color.Black,
    cursorColor             = HeaderGreen
)