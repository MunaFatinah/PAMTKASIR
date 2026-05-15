package com.muna.pamtkasir

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.muna.pamtkasir.model.Kas
import com.muna.pamtkasir.model.Produk
import com.muna.pamtkasir.ui.kas.KasLogScreen
import com.muna.pamtkasir.ui.kas.KasScreen
import com.muna.pamtkasir.ui.login.LoginScreen
import com.muna.pamtkasir.ui.produk.ProdukLogScreen
import com.muna.pamtkasir.ui.produk.ProdukScreen
import com.muna.pamtkasir.ui.register.RegisterScreen
import com.muna.pamtkasir.ui.theme.PAMTKASIRTheme
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
                            onNavigateToProdukLog = { produk ->
                                val json = URLEncoder.encode(Json.encodeToString(produk), "UTF-8")
                                navController.navigate("produk_log/$json")
                            }
                        )
                    }

                    composable("admin_dashboard") {
                        DashboardScreen(
                            onNavigateToKasLog = { kas ->
                                val json = URLEncoder.encode(Json.encodeToString(kas), "UTF-8")
                                navController.navigate("kas_log/$json")
                            },
                            onNavigateToProdukLog = { produk ->
                                val json = URLEncoder.encode(Json.encodeToString(produk), "UTF-8")
                                navController.navigate("produk_log/$json")
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

                    composable("produk_log/{produkJson}") { backStackEntry ->
                        val encoded = backStackEntry.arguments?.getString("produkJson") ?: ""
                        val produk = Json.decodeFromString<Produk>(URLDecoder.decode(encoded, "UTF-8"))
                        ProdukLogScreen(
                            produk         = produk,
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}

// ── Dashboard dengan Bottom Nav ────────────────────────────────────────────────
@Composable
fun DashboardScreen(
    onNavigateToKasLog    : (Kas) -> Unit,
    onNavigateToProdukLog : (Produk) -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }

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
                1 -> ProdukScreen(onNavigateToLog = onNavigateToProdukLog)
            }
        }
    }
}

// ── Bottom Nav Bar ─────────────────────────────────────────────────────────────
@Composable
fun BottomNavBar(
    selectedTab   : Int,
    onTabSelected : (Int) -> Unit
) {
    data class NavItem(val label: String, val icon: ImageVector)

    val items = listOf(
        NavItem("Kas",    Icons.Outlined.AccountBalanceWallet),
        NavItem("Produk", Icons.Outlined.ShoppingBag)
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
                    fontSize   = 10.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}