package com.muna.pamtkasir.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Produk(
    val id: String = "",
    @SerialName("user_id") val userId: String = "",
    val name: String = "",
    val price: Double = 0.0,
    val stock: Double = 0.0,
    @SerialName("is_active") val isActive: Boolean = true,
    @SerialName("created_at") val createdAt: String = ""
)
@Serializable
data class InventoryLog(
    val id: String = "",
    @SerialName("product_id") val productId: String = "",
    val activity: String = "",
    @SerialName("quantity_change") val quantityChange: Double = 0.0,
    @SerialName("stock_before") val stockBefore: Double = 0.0,
    @SerialName("stock_after") val stockAfter: Double = 0.0,
    @SerialName("created_at") val createdAt: String = ""
)