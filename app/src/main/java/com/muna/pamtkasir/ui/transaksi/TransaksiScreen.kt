package com.muna.pamtkasir.ui.transaksi

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.muna.pamtkasir.R
import com.muna.pamtkasir.model.CartItem
import com.muna.pamtkasir.model.Customer
import com.muna.pamtkasir.model.Kas
import com.muna.pamtkasir.model.Produk
import com.muna.pamtkasir.ui.kas.formatRupiah

private val BgGreen     = Color(0xFF66B499)
private val HeaderGreen = Color(0xFF1A6651)
private val CardBg      = Color(0xFFF6F6F6)
private val ErrorRed    = Color(0xFFE24B4A)

@Composable
fun TransaksiScreen() {
    val viewModel: TransaksiViewModel = viewModel()

    val pelangganList by viewModel.pelangganList.collectAsState()
    val kasList       by viewModel.kasList.collectAsState()
    val produkList    by viewModel.produkList.collectAsState()
    val cart          by viewModel.cart.collectAsState()
    val state         by viewModel.transaksiState.collectAsState()

    var selectedPelanggan by remember { mutableStateOf<Customer?>(null) }
    var selectedKas       by remember { mutableStateOf<Kas?>(null) }
    var showPelangganPicker by remember { mutableStateOf(false) }
    var showKasPicker       by remember { mutableStateOf(false) }
    var showPembayaran      by remember { mutableStateOf(false) }
    var showSukses          by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state) {
        when (state) {
            is TransaksiState.Success -> {
                showSukses = true
                viewModel.resetState()
            }
            is TransaksiState.Error -> {
                snackbarHostState.showSnackbar((state as TransaksiState.Error).message)
                viewModel.resetState()
            }
            else -> {}
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
                        Text("KASIRKU", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("Transaksi Penjualan", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                    }
                }
                if (cart.isNotEmpty()) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White.copy(alpha = 0.2f))
                            .clickable { showPembayaran = true }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.ShoppingCart, null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Bayar", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                        }
                    }
                }
            }

            LazyColumn(
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // ── Pilih Pelanggan ────────────────────────────────────
                item {
                    SectionCard(
                        title    = "Pelanggan",
                        icon     = Icons.Outlined.People,
                        onClick  = { showPelangganPicker = true }
                    ) {
                        Text(
                            text  = selectedPelanggan?.name ?: "Tap untuk pilih pelanggan",
                            color = if (selectedPelanggan != null) Color.Black else Color.Gray,
                            fontSize = 13.sp
                        )
                        if (selectedPelanggan != null) {
                            Text(
                                text     = selectedPelanggan!!.phone,
                                color    = Color.Gray,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                // ── Pilih Kas ──────────────────────────────────────────
                item {
                    SectionCard(
                        title   = "Kas",
                        icon    = Icons.Outlined.AccountBalanceWallet,
                        onClick = { showKasPicker = true }
                    ) {
                        Text(
                            text  = selectedKas?.name ?: "Tap untuk pilih kas",
                            color = if (selectedKas != null) Color.Black else Color.Gray,
                            fontSize = 13.sp
                        )
                        if (selectedKas != null) {
                            Text(
                                text     = "Saldo: ${formatRupiah(selectedKas!!.balance)}",
                                color    = HeaderGreen,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                // ── List Produk ────────────────────────────────────────
                item {
                    Text(
                        text       = "Pilih Produk",
                        color      = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize   = 14.sp,
                        modifier   = Modifier.padding(bottom = 4.dp)
                    )
                }

                items(produkList) { produk ->
                    ProdukItemCard(
                        produk  = produk,
                        cartQty = cart.find { it.produk.id == produk.id }?.quantity ?: 0.0,
                        onTambah = { viewModel.tambahKeKeranjang(produk) },
                        onKurang = { viewModel.kurangiDariKeranjang(produk.id) }
                    )
                }

                // ── Ringkasan Keranjang ────────────────────────────────
                if (cart.isNotEmpty()) {
                    item {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text       = "Keranjang",
                            color      = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize   = 14.sp,
                            modifier   = Modifier.padding(bottom = 4.dp)
                        )
                    }
                    items(cart) { item ->
                        CartItemCard(
                            item    = item,
                            onHapus = { viewModel.hapusDariKeranjang(item.produk.id) }
                        )
                    }
                    item {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(HeaderGreen)
                                .padding(16.dp)
                        ) {
                            Text("Total", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(formatRupiah(viewModel.totalHarga), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }
            }
        }
    }

    // ── Picker Pelanggan ───────────────────────────────────────────────
    if (showPelangganPicker) {
        PickerDialog(
            title    = "Pilih Pelanggan",
            onDismiss = { showPelangganPicker = false }
        ) {
            pelangganList.forEach { pelanggan ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            selectedPelanggan = pelanggan
                            showPelangganPicker = false
                        }
                        .padding(vertical = 12.dp, horizontal = 4.dp)
                ) {
                    Icon(Icons.Outlined.Person, null, tint = HeaderGreen, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(pelanggan.name, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Text(pelanggan.phone, color = Color.Gray, fontSize = 11.sp)
                    }
                }
                HorizontalDivider(color = Color(0xFFEEEEEE))
            }
        }
    }

    // ── Picker Kas ─────────────────────────────────────────────────────
    if (showKasPicker) {
        PickerDialog(
            title     = "Pilih Kas",
            onDismiss = { showKasPicker = false }
        ) {
            kasList.forEach { kas ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            selectedKas = kas
                            showKasPicker = false
                        }
                        .padding(vertical = 12.dp, horizontal = 4.dp)
                ) {
                    Icon(Icons.Outlined.AccountBalanceWallet, null, tint = HeaderGreen, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(kas.name, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Text(formatRupiah(kas.balance), color = HeaderGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
                HorizontalDivider(color = Color(0xFFEEEEEE))
            }
        }
    }

    // ── Dialog Pembayaran ──────────────────────────────────────────────
    if (showPembayaran) {
        PembayaranDialog(
            total         = viewModel.totalHarga,
            onDismiss     = { showPembayaran = false },
            onConfirm     = { paidAmount ->
                val pelangganId = selectedPelanggan?.id
                val kasId       = selectedKas?.id
                if (pelangganId == null) {
                    showPembayaran = false
                } else if (kasId == null) {
                    showPembayaran = false
                } else {
                    viewModel.prosesTransaksi(pelangganId, kasId, paidAmount)
                    showPembayaran = false
                }
            }
        )
    }

    // ── Dialog Sukses ──────────────────────────────────────────────────
    if (showSukses) {
        SuksesDialog(
            onDismiss = {
                showSukses        = false
                selectedPelanggan = null
                selectedKas       = null
                viewModel.fetchAll()
            }
        )
    }
}

// ── Section Card ──────────────────────────────────────────────────────────────
@Composable
private fun SectionCard(
    title   : String,
    icon    : androidx.compose.ui.graphics.vector.ImageVector,
    onClick : () -> Unit,
    content : @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .shadow(3.dp, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .fillMaxWidth()
            .background(CardBg)
            .clickable { onClick() }
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 8.dp)) {
            Icon(icon, null, tint = HeaderGreen, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text(title, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = HeaderGreen)
            Spacer(Modifier.weight(1f))
            Icon(Icons.Outlined.ChevronRight, null, tint = Color.Gray, modifier = Modifier.size(16.dp))
        }
        content()
    }
}

// ── Produk Item Card ──────────────────────────────────────────────────────────
@Composable
private fun ProdukItemCard(
    produk  : Produk,
    cartQty : Double,
    onTambah: () -> Unit,
    onKurang: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .shadow(2.dp, RoundedCornerShape(10.dp))
            .clip(RoundedCornerShape(10.dp))
            .fillMaxWidth()
            .background(CardBg)
            .padding(12.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(HeaderGreen.copy(alpha = 0.12f))
        ) {
            Icon(Icons.Outlined.ShoppingBag, null, tint = HeaderGreen, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(produk.name, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = Color.Black)
            Text(formatRupiah(produk.price), color = HeaderGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Text("Stok: ${produk.stock.toInt()}", color = if (produk.stock > 0) Color.Gray else ErrorRed, fontSize = 10.sp)
        }
        if (cartQty > 0) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(ErrorRed.copy(alpha = 0.12f))
                        .clickable { onKurang() }
                ) {
                    Icon(Icons.Outlined.Remove, null, tint = ErrorRed, modifier = Modifier.size(14.dp))
                }
                Text(
                    text     = cartQty.toInt().toString(),
                    modifier = Modifier.padding(horizontal = 10.dp),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(HeaderGreen.copy(alpha = 0.12f))
                        .clickable(enabled = produk.stock > cartQty) { onTambah() }
                ) {
                    Icon(Icons.Outlined.Add, null, tint = HeaderGreen, modifier = Modifier.size(14.dp))
                }
            }
        } else {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (produk.stock > 0) HeaderGreen.copy(alpha = 0.12f) else Color.Gray.copy(alpha = 0.12f))
                    .clickable(enabled = produk.stock > 0) { onTambah() }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text  = if (produk.stock > 0) "Tambah" else "Habis",
                    color = if (produk.stock > 0) HeaderGreen else Color.Gray,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

// ── Cart Item Card ────────────────────────────────────────────────────────────
@Composable
private fun CartItemCard(
    item    : CartItem,
    onHapus : () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .shadow(2.dp, RoundedCornerShape(10.dp))
            .clip(RoundedCornerShape(10.dp))
            .fillMaxWidth()
            .background(CardBg)
            .padding(12.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(item.produk.name, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
            Text("${item.quantity.toInt()} x ${formatRupiah(item.produk.price)}", color = Color.Gray, fontSize = 11.sp)
        }
        Text(formatRupiah(item.subtotal), fontWeight = FontWeight.Bold, color = HeaderGreen, fontSize = 13.sp)
        Spacer(Modifier.width(8.dp))
        IconButton(onClick = onHapus, modifier = Modifier.size(24.dp)) {
            Icon(Icons.Outlined.Close, null, tint = ErrorRed, modifier = Modifier.size(16.dp))
        }
    }
}

// ── Picker Dialog ─────────────────────────────────────────────────────────────
@Composable
private fun PickerDialog(
    title     : String,
    onDismiss : () -> Unit,
    content   : @Composable ColumnScope.() -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .shadow(16.dp, RoundedCornerShape(20.dp))
                .clip(RoundedCornerShape(20.dp))
                .background(CardBg)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(HeaderGreen)
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
            Column(
                modifier = Modifier
                    .padding(horizontal = 20.dp, vertical = 8.dp)
                    .verticalScroll(rememberScrollState()),
                content = content
            )
        }
    }
}

// ── Pembayaran Dialog ─────────────────────────────────────────────────────────
@Composable
private fun PembayaranDialog(
    total     : Double,
    onDismiss : () -> Unit,
    onConfirm : (Double) -> Unit
) {
    var bayar     by remember { mutableStateOf("") }
    val bayarVal  = bayar.toDoubleOrNull() ?: 0.0
    val kembalian = bayarVal - total

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .shadow(16.dp, RoundedCornerShape(20.dp))
                .clip(RoundedCornerShape(20.dp))
                .background(CardBg)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(HeaderGreen)
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Text("Pembayaran", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Total", fontSize = 13.sp, color = Color.Gray)
                    Text(formatRupiah(total), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
                Spacer(Modifier.height(14.dp))
                Text("Uang Bayar", fontSize = 11.sp, color = Color.Black, modifier = Modifier.padding(bottom = 6.dp))
                OutlinedTextField(
                    value           = bayar,
                    onValueChange   = { bayar = it.filter { c -> c.isDigit() } },
                    placeholder     = { Text("0", color = Color.Gray) },
                    singleLine      = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape           = RoundedCornerShape(10.dp),
                    modifier        = Modifier.fillMaxWidth(),
                    colors          = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Color.White,
                        focusedContainerColor   = Color.White,
                        unfocusedBorderColor    = Color(0xFFDDDDDD),
                        focusedBorderColor      = HeaderGreen,
                        unfocusedTextColor      = Color.Black,
                        focusedTextColor        = Color.Black,
                        cursorColor             = HeaderGreen
                    )
                )
                Spacer(Modifier.height(14.dp))
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (kembalian >= 0) HeaderGreen.copy(alpha = 0.1f) else ErrorRed.copy(alpha = 0.1f))
                        .padding(12.dp)
                ) {
                    Text("Kembalian", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Text(
                        text       = if (bayar.isNotEmpty()) formatRupiah(kembalian) else "-",
                        fontWeight = FontWeight.Bold,
                        color      = if (kembalian >= 0) HeaderGreen else ErrorRed,
                        fontSize   = 13.sp
                    )
                }
                Spacer(Modifier.height(20.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFE0E0E0))
                            .clickable { onDismiss() }
                            .padding(vertical = 12.dp)
                    ) {
                        Text("Batal", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Color.Black)
                    }
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (kembalian >= 0 && bayar.isNotEmpty()) HeaderGreen else Color.Gray)
                            .clickable(enabled = kembalian >= 0 && bayar.isNotEmpty()) { onConfirm(bayarVal) }
                            .padding(vertical = 12.dp)
                    ) {
                        Text("Proses", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Color.White)
                    }
                }
            }
        }
    }
}

// ── Sukses Dialog ─────────────────────────────────────────────────────────────
@Composable
private fun SuksesDialog(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .shadow(16.dp, RoundedCornerShape(20.dp))
                .clip(RoundedCornerShape(20.dp))
                .background(CardBg)
                .padding(32.dp)
        ) {
            Icon(
                Icons.Outlined.CheckCircle,
                null,
                tint     = HeaderGreen,
                modifier = Modifier.size(64.dp)
            )
            Spacer(Modifier.height(16.dp))
            Text("Transaksi Berhasil!", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Black)
            Spacer(Modifier.height(8.dp))
            Text("Stok produk dan saldo kas telah diperbarui.", color = Color.Gray, fontSize = 12.sp)
            Spacer(Modifier.height(24.dp))
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(HeaderGreen)
                    .clickable { onDismiss() }
                    .padding(vertical = 12.dp)
            ) {
                Text("Selesai", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    }
}