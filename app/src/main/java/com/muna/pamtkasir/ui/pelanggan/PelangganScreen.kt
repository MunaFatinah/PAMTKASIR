package com.muna.pamtkasir.ui.pelanggan

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
import com.muna.pamtkasir.model.Customer
import io.github.jan.supabase.auth.auth
import java.text.SimpleDateFormat
import java.util.Locale

private val BgGreen     = Color(0xFF66B499)
private val HeaderGreen = Color(0xFF1A6651)
private val CardBg      = Color(0xFFF6F6F6)
private val ErrorRed    = Color(0xFFE24B4A)

@Composable
fun PelangganScreen(
    onNavigateToLog: (Customer) -> Unit = {}
) {
    val viewModel: PelangganViewModel = viewModel()
    val pelangganState by viewModel.pelangganState.collectAsState()
    val actionState    by viewModel.actionState.collectAsState()

    var showAddDialog  by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf<Customer?>(null) }

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
                            text     = "Manajemen Pelanggan",
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
                    Icon(Icons.Outlined.Add, contentDescription = "Tambah Pelanggan", tint = Color.White)
                }
            }

            // ── Content ────────────────────────────────────────────────
            when (val state = pelangganState) {

                is PelangganState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color.White)
                    }
                }

                is PelangganState.Error -> {
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
                                onClick = { viewModel.fetchPelanggan() },
                                border  = BorderStroke(1.dp, Color.White)
                            ) {
                                Text("Coba Lagi", color = Color.White)
                            }
                        }
                    }
                }

                is PelangganState.Success -> {
                    if (state.data.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Outlined.PeopleOutline,
                                    contentDescription = null,
                                    tint     = Color.White.copy(alpha = 0.5f),
                                    modifier = Modifier.size(56.dp)
                                )
                                Spacer(Modifier.height(10.dp))
                                Text(
                                    "Belum ada pelanggan",
                                    color    = Color.White.copy(alpha = 0.7f),
                                    fontSize = 14.sp
                                )
                                Text(
                                    "Tap + untuk menambah pelanggan baru",
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
                            items(state.data) { customer ->
                                PelangganCard(
                                    customer   = customer,
                                    onLihatLog = { onNavigateToLog(customer) },
                                    onEdit     = { showEditDialog = customer },
                                    onToggle   = { viewModel.toggleAktif(customer.id, customer.isActive) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // ── Dialog Tambah Pelanggan ────────────────────────────────────────
    if (showAddDialog) {
        AddPelangganDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, phone ->
                val userId = SupabaseClientProvider.client.auth.currentUserOrNull()?.id ?: ""
                viewModel.addPelanggan(name, phone, userId)
                showAddDialog = false
            }
        )
    }

    // ── Dialog Edit Pelanggan ──────────────────────────────────────────
    showEditDialog?.let { customer ->
        EditPelangganDialog(
            customer  = customer,
            onDismiss = { showEditDialog = null },
            onConfirm = { name, phone ->
                viewModel.editPelanggan(customer.id, name, phone)
                showEditDialog = null
            }
        )
    }
}

// ── Pelanggan Card ─────────────────────────────────────────────────────────────
@Composable
private fun PelangganCard(
    customer   : Customer,
    onLihatLog : () -> Unit,
    onEdit     : () -> Unit,
    onToggle   : () -> Unit
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
                    Icons.Outlined.Person,
                    contentDescription = null,
                    tint     = HeaderGreen,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = customer.name,
                    fontWeight = FontWeight.SemiBold,
                    fontSize   = 14.sp,
                    color      = Color.Black
                )
                Text(
                    text     = customer.phone.ifBlank { "-" },
                    fontSize = 12.sp,
                    color    = Color.Gray
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        if (customer.isActive) HeaderGreen.copy(alpha = 0.12f)
                        else Color.Gray.copy(alpha = 0.12f)
                    )
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text       = if (customer.isActive) "Aktif" else "Nonaktif",
                    color      = if (customer.isActive) HeaderGreen else Color.Gray,
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
            PelangganActionButton(
                icon     = Icons.Outlined.History,
                label    = "Log",
                modifier = Modifier.weight(1f),
                onClick  = onLihatLog
            )
            PelangganActionButton(
                icon     = Icons.Outlined.Edit,
                label    = "Edit",
                modifier = Modifier.weight(1f),
                onClick  = onEdit
            )
            PelangganActionButton(
                icon     = if (customer.isActive) Icons.Outlined.ToggleOff else Icons.Outlined.ToggleOn,
                label    = if (customer.isActive) "Nonaktif" else "Aktifkan",
                modifier = Modifier.weight(1f),
                onClick  = onToggle
            )
        }
    }
}

@Composable
private fun PelangganActionButton(
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

// ── Dialog Tambah Pelanggan ────────────────────────────────────────────────────
@Composable
private fun AddPelangganDialog(
    onDismiss : () -> Unit,
    onConfirm : (String, String) -> Unit
) {
    var nama  by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = CardBg,
        title = { Text("Tambah Pelanggan", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
        text  = {
            Column {
                OutlinedTextField(
                    value         = nama,
                    onValueChange = { nama = it },
                    label         = { Text("Nama Pelanggan") },
                    singleLine    = true,
                    modifier      = Modifier.fillMaxWidth(),
                    shape         = RoundedCornerShape(10.dp),
                    colors        = pelangganFieldColors()
                )
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value           = phone,
                    onValueChange   = { phone = it.filter { c -> c.isDigit() || c == '+' } },
                    label           = { Text("No. Telepon") },
                    singleLine      = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier        = Modifier.fillMaxWidth(),
                    shape           = RoundedCornerShape(10.dp),
                    colors          = pelangganFieldColors()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (nama.isNotBlank()) onConfirm(nama, phone) },
                colors  = ButtonDefaults.buttonColors(containerColor = HeaderGreen),
                shape   = RoundedCornerShape(8.dp)
            ) { Text("Simpan") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal", color = Color.Gray) }
        }
    )
}

// ── Dialog Edit Pelanggan ──────────────────────────────────────────────────────
@Composable
private fun EditPelangganDialog(
    customer  : Customer,
    onDismiss : () -> Unit,
    onConfirm : (String, String) -> Unit
) {
    var nama  by remember { mutableStateOf(customer.name) }
    var phone by remember { mutableStateOf(customer.phone) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = CardBg,
        title = { Text("Edit Pelanggan", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
        text  = {
            Column {
                OutlinedTextField(
                    value         = nama,
                    onValueChange = { nama = it },
                    label         = { Text("Nama Pelanggan") },
                    singleLine    = true,
                    modifier      = Modifier.fillMaxWidth(),
                    shape         = RoundedCornerShape(10.dp),
                    colors        = pelangganFieldColors()
                )
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value           = phone,
                    onValueChange   = { phone = it.filter { c -> c.isDigit() || c == '+' } },
                    label           = { Text("No. Telepon") },
                    singleLine      = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier        = Modifier.fillMaxWidth(),
                    shape           = RoundedCornerShape(10.dp),
                    colors          = pelangganFieldColors()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (nama.isNotBlank()) onConfirm(nama, phone) },
                colors  = ButtonDefaults.buttonColors(containerColor = HeaderGreen),
                shape   = RoundedCornerShape(8.dp)
            ) { Text("Simpan") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal", color = Color.Gray) }
        }
    )
}

// ── Helper ─────────────────────────────────────────────────────────────────────
@Composable
private fun pelangganFieldColors() = OutlinedTextFieldDefaults.colors(
    unfocusedContainerColor = Color.White,
    focusedContainerColor   = Color.White,
    unfocusedBorderColor    = Color(0xFFDDDDDD),
    focusedBorderColor      = HeaderGreen,
    unfocusedTextColor      = Color.Black,
    focusedTextColor        = Color.Black,
    cursorColor             = HeaderGreen
)

