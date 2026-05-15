package com.muna.pamtkasir.ui.pengeluaran

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.muna.pamtkasir.model.Expense
import com.muna.pamtkasir.model.Kas
import java.text.NumberFormat
import java.util.Locale

private val BgGreen     = Color(0xFF66B499)
private val HeaderGreen = Color(0xFF1A6651)
private val CardBg      = Color(0xFFF6F6F6)
private val ErrorRed    = Color(0xFFE24B4A)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PengeluaranScreen(
    viewModel: PengeluaranViewModel = viewModel()
) {
    val pengeluaranState by viewModel.pengeluaranState.collectAsState()
    val kasList          by viewModel.kasList.collectAsState()
    val actionState      by viewModel.actionState.collectAsState()

    var showDialog       by remember { mutableStateOf(false) }
    var showBatalDialog  by remember { mutableStateOf(false) }
    var selectedExpense  by remember { mutableStateOf<Expense?>(null) }

    // Auto refresh setiap kali screen dibuka
    LaunchedEffect(Unit) {
        viewModel.loadExpenses()
        viewModel.loadKas()
    }

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
            // Top Bar
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(HeaderGreen)
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Column {
                    Text(
                        text       = "KASIRKU",
                        color      = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize   = 16.sp
                    )
                    Text(
                        text     = "Manajemen Pengeluaran",
                        color    = Color.White.copy(alpha = 0.7f),
                        fontSize = 11.sp
                    )
                }
                IconButton(
                    onClick  = { showDialog = true },
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.15f))
                ) {
                    Icon(Icons.Outlined.Add, contentDescription = "Tambah", tint = Color.White)
                }
            }

            // Content
            when (val state = pengeluaranState) {
                is PengeluaranState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color.White)
                    }
                }

                is PengeluaranState.Error -> {
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
                                onClick = { viewModel.loadExpenses() },
                                border  = androidx.compose.foundation.BorderStroke(1.dp, Color.White)
                            ) {
                                Text("Coba Lagi", color = Color.White)
                            }
                        }
                    }
                }

                is PengeluaranState.Success -> {
                    if (state.data.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Outlined.MoneyOff,
                                    contentDescription = null,
                                    tint     = Color.White.copy(alpha = 0.5f),
                                    modifier = Modifier.size(56.dp)
                                )
                                Spacer(Modifier.height(10.dp))
                                Text(
                                    "Belum ada pengeluaran",
                                    color    = Color.White.copy(alpha = 0.7f),
                                    fontSize = 14.sp
                                )
                                Text(
                                    "Tap + untuk mencatat pengeluaran",
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
                            items(state.data) { expense ->
                                ExpenseCard(
                                    expense  = expense,
                                    kasName  = kasList.find { it.id == expense.kas_id }?.name ?: "-",
                                    onBatalkan = {
                                        selectedExpense = expense
                                        showBatalDialog = true
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialog Tambah Pengeluaran
    if (showDialog) {
        TambahPengeluaranDialog(
            kasList   = kasList,
            onDismiss = { showDialog = false },
            onConfirm = { kasId, desc, total ->
                viewModel.tambahPengeluaran(kasId, desc, total)
                showDialog = false
            }
        )
    }

    // Dialog Batalkan
    if (showBatalDialog && selectedExpense != null) {
        AlertDialog(
            onDismissRequest = { showBatalDialog = false },
            containerColor   = CardBg,
            title = { Text("Batalkan Pengeluaran?", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text  = {
                Text("Saldo kas akan dikembalikan sebesar ${formatRupiahExp(selectedExpense!!.total)}")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.batalkanPengeluaran(selectedExpense!!)
                        showBatalDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed),
                    shape  = RoundedCornerShape(8.dp)
                ) { Text("Batalkan") }
            },
            dismissButton = {
                TextButton(onClick = { showBatalDialog = false }) {
                    Text("Tidak", color = Color.Gray)
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TambahPengeluaranDialog(
    kasList   : List<Kas>,
    onDismiss : () -> Unit,
    onConfirm : (String, String, Double) -> Unit
) {
    var selectedKasId    by remember { mutableStateOf("") }
    var selectedKasName  by remember { mutableStateOf("Pilih Kas") }
    var description      by remember { mutableStateOf("") }
    var total            by remember { mutableStateOf("") }
    var dropdownExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = CardBg,
        title = { Text("Tambah Pengeluaran", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
        text  = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {

                ExposedDropdownMenuBox(
                    expanded        = dropdownExpanded,
                    onExpandedChange = { dropdownExpanded = !dropdownExpanded }
                ) {
                    OutlinedTextField(
                        value         = selectedKasName,
                        onValueChange = {},
                        readOnly      = true,
                        label         = { Text("Pilih Kas") },
                        trailingIcon  = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) },
                        modifier      = Modifier.menuAnchor().fillMaxWidth(),
                        shape         = RoundedCornerShape(10.dp),
                        colors        = pengeluaranFieldColors()
                    )
                    ExposedDropdownMenu(
                        expanded        = dropdownExpanded,
                        onDismissRequest = { dropdownExpanded = false }
                    ) {
                        kasList.forEach { kas ->
                            DropdownMenuItem(
                                text    = { Text("${kas.name} (${formatRupiahExp(kas.balance)})") },
                                onClick = {
                                    selectedKasId   = kas.id
                                    selectedKasName = kas.name
                                    dropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value         = description,
                    onValueChange = { description = it },
                    label         = { Text("Keterangan") },
                    singleLine    = true,
                    modifier      = Modifier.fillMaxWidth(),
                    shape         = RoundedCornerShape(10.dp),
                    colors        = pengeluaranFieldColors()
                )

                OutlinedTextField(
                    value           = total,
                    onValueChange   = { total = it.filter { c -> c.isDigit() || c == '.' } },
                    label           = { Text("Total (Rp)") },
                    singleLine      = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier        = Modifier.fillMaxWidth(),
                    shape           = RoundedCornerShape(10.dp),
                    colors          = pengeluaranFieldColors()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val t = total.toDoubleOrNull() ?: 0.0
                    if (selectedKasId.isNotEmpty() && description.isNotBlank() && t > 0) {
                        onConfirm(selectedKasId, description, t)
                    }
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

@Composable
private fun ExpenseCard(
    expense    : Expense,
    kasName    : String,
    onBatalkan : () -> Unit
) {
    val isActive = expense.status == "active"

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
                    if (isActive) ErrorRed.copy(alpha = 0.12f)
                    else Color.Gray.copy(alpha = 0.12f)
                )
        ) {
            Icon(
                imageVector        = Icons.Outlined.ArrowDownward,
                contentDescription = null,
                tint               = if (isActive) ErrorRed else Color.Gray,
                modifier           = Modifier.size(20.dp)
            )
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text       = expense.description,
                fontWeight = FontWeight.SemiBold,
                fontSize   = 13.sp,
                color      = if (isActive) Color.Black else Color.Gray
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text     = "Kas: $kasName",
                fontSize = 11.sp,
                color    = Color.Gray
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text       = formatRupiahExp(expense.total),
                fontWeight = FontWeight.Bold,
                fontSize   = 14.sp,
                color      = if (isActive) ErrorRed else Color.Gray
            )
            Spacer(Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        if (isActive) HeaderGreen.copy(alpha = 0.12f)
                        else Color.Gray.copy(alpha = 0.12f)
                    )
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text       = if (isActive) "Aktif" else "Dibatalkan",
                    color      = if (isActive) HeaderGreen else Color.Gray,
                    fontSize   = 10.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        if (isActive) {
            IconButton(onClick = onBatalkan) {
                Icon(
                    Icons.Outlined.Close,
                    contentDescription = "Batalkan",
                    tint               = ErrorRed
                )
            }
        }
    }
}

@Composable
private fun pengeluaranFieldColors() = OutlinedTextFieldDefaults.colors(
    unfocusedContainerColor = Color.White,
    focusedContainerColor   = Color.White,
    unfocusedBorderColor    = Color(0xFFDDDDDD),
    focusedBorderColor      = HeaderGreen,
    unfocusedTextColor      = Color.Black,
    focusedTextColor        = Color.Black,
    cursorColor             = HeaderGreen
)

private fun formatRupiahExp(amount: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    return format.format(amount).replace("Rp", "Rp ").replace(",00", "")
}