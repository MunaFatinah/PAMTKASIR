package com.muna.pamtkasir

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.muna.pamtkasir.model.Customer
import com.muna.pamtkasir.model.Kas
import com.muna.pamtkasir.model.Produk
import com.muna.pamtkasir.ui.inventorylog.InventoryLogScreen
import com.muna.pamtkasir.ui.kas.KasLogScreen
import com.muna.pamtkasir.ui.kas.KasScreen
import com.muna.pamtkasir.ui.login.LoginScreen
import com.muna.pamtkasir.ui.pelanggan.PelangganLogScreen
import com.muna.pamtkasir.ui.pelanggan.PelangganScreen
import com.muna.pamtkasir.ui.pengeluaran.PengeluaranScreen
import com.muna.pamtkasir.ui.produk.ProdukScreen
import com.muna.pamtkasir.ui.profile.ProfileScreen
import com.muna.pamtkasir.ui.register.RegisterScreen
import com.muna.pamtkasir.ui.theme.PAMTKASIRTheme
import com.muna.pamtkasir.ui.transaksi.TransaksiScreen
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.net.URLDecoder
import java.net.URLEncoder

private val HeaderGreen = Color(0xFF1A6651)
private val BgGreen     = Color(0xFF66B499)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PAMTKASIRTheme {
                val navController = rememberNavController()

                NavHost(
                    navController    = navController,
                    startDestination = "login"
                ) {
                    composable("login") {
                        LoginScreen(
                            onLoginSuccess = { role ->
                                when (role) {
                                    "admin" -> navController.navigate("admin_dashboard") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                    "cashier" -> navController.navigate("cashier_dashboard") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                }
                            },
                            onGoToRegister = { navController.navigate("register") }
                        )
                    }

                    composable("register") {
                        RegisterScreen(
                            onRegisterSuccess = {
                                navController.navigate("login") {
                                    popUpTo("register") { inclusive = true }
                                }
                            },
                            onGoToLogin = { navController.popBackStack() }
                        )
                    }

                    composable("cashier_dashboard") {
                        DashboardScreen(
                            onNavigateToKasLog = { kas ->
                                val json = URLEncoder.encode(Json.encodeToString(kas), "UTF-8")
                                navController.navigate("kas_log/$json")
                            },
                            onNavigateToPelangganLog = { customer ->
                                val json = URLEncoder.encode(Json.encodeToString(customer), "UTF-8")
                                navController.navigate("pelanggan_log/$json")
                            },
                            onNavigateToInventoryLog = { produk ->
                                val json = URLEncoder.encode(Json.encodeToString(produk), "UTF-8")
                                navController.navigate("inventory_log/$json")
                            },
                            onLogout = {
                                navController.navigate("login") {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                        )
                    }

                    composable("admin_dashboard") {
                        DashboardScreen(
                            onNavigateToKasLog = { kas ->
                                val json = URLEncoder.encode(Json.encodeToString(kas), "UTF-8")
                                navController.navigate("kas_log/$json")
                            },
                            onNavigateToPelangganLog = { customer ->
                                val json = URLEncoder.encode(Json.encodeToString(customer), "UTF-8")
                                navController.navigate("pelanggan_log/$json")
                            },
                            onNavigateToInventoryLog = { produk ->
                                val json = URLEncoder.encode(Json.encodeToString(produk), "UTF-8")
                                navController.navigate("inventory_log/$json")
                            },
                            onLogout = {
                                navController.navigate("login") {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                        )
                    }

                    composable("kas_log/{kasJson}") { backStackEntry ->
                        val encoded = backStackEntry.arguments?.getString("kasJson") ?: ""
                        val kas = Json.decodeFromString<Kas>(URLDecoder.decode(encoded, "UTF-8"))
                        KasLogScreen(
                            kas            = kas,
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }

                    composable("pelanggan_log/{customerJson}") { backStackEntry ->
                        val encoded  = backStackEntry.arguments?.getString("customerJson") ?: ""
                        val customer = Json.decodeFromString<Customer>(URLDecoder.decode(encoded, "UTF-8"))
                        PelangganLogScreen(
                            customer       = customer,
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }

                    composable("inventory_log/{produkJson}") { backStackEntry ->
                        val encoded = backStackEntry.arguments?.getString("produkJson") ?: ""
                        val produk  = Json.decodeFromString<Produk>(URLDecoder.decode(encoded, "UTF-8"))
                        InventoryLogScreen(
                            produk         = produk,
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DashboardScreen(
    onNavigateToKasLog      : (Kas) -> Unit,
    onNavigateToPelangganLog: (Customer) -> Unit,
    onNavigateToInventoryLog: (Produk) -> Unit,
    onLogout                : () -> Unit
) {
    var selectedTab by rememberSaveable { mutableStateOf(0) }

    Scaffold(
        bottomBar = {
            BottomNavBar(
                selectedTab   = selectedTab,
                onTabSelected = { selectedTab = it }
            )
        },
        containerColor = BgGreen
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (selectedTab) {
                0 -> KasScreen(onNavigateToLog = onNavigateToKasLog)
                1 -> ProdukScreen(onNavigateToLog = onNavigateToInventoryLog)
                2 -> PengeluaranScreen()
                3 -> PelangganScreen(onNavigateToLog = onNavigateToPelangganLog)
                4 -> TransaksiScreen()
                5 -> ProfileScreen(onLogout = onLogout)
            }
        }
    }
}

@Composable
fun BottomNavBar(
    selectedTab   : Int,
    onTabSelected : (Int) -> Unit
) {
    data class NavItem(val label: String, val icon: ImageVector)

    val items = listOf(
        NavItem("Kas",         Icons.Outlined.AccountBalanceWallet),
        NavItem("Produk",      Icons.Outlined.ShoppingBag),
        NavItem("Pengeluaran", Icons.Outlined.MoneyOff),
        NavItem("Pelanggan",   Icons.Outlined.People),
        NavItem("Transaksi",   Icons.Outlined.Receipt),
        NavItem("Profil",      Icons.Outlined.Person)
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(HeaderGreen)
            .padding(vertical = 8.dp)
    ) {
        items.forEachIndexed { index, item ->
            val isSelected = selectedTab == index
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .weight(1f)
                    .clickable { onTabSelected(index) }
                    .padding(vertical = 6.dp)
            ) {
                Icon(
                    imageVector        = item.icon,
                    contentDescription = item.label,
                    tint               = if (isSelected) Color.White else Color.White.copy(alpha = 0.45f),
                    modifier           = Modifier.size(22.dp)
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    text       = item.label,
                    color      = if (isSelected) Color.White else Color.White.copy(alpha = 0.45f),
                    fontSize   = 9.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}