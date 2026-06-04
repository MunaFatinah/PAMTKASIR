package com.muna.pamtkasir.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Transaction(
    val id: String = "",
    @SerialName("user_id") val userId: String = "",
    @SerialName("customer_id") val customerId: String = "",
    @SerialName("kas_id") val kasId: String = "",
    val total: Double = 0.0,
    @SerialName("paid_amount") val paidAmount: Double = 0.0,
    @SerialName("change_amount") val changeAmount: Double = 0.0,
    @SerialName("created_at") val createdAt: String = ""
)

@Serializable
data class TransactionItem(
    val id: String = "",
    @SerialName("transaction_id") val transactionId: String = "",
    @SerialName("product_id") val productId: String = "",
    val quantity: Double = 0.0,
    val price: Double = 0.0
)

// Model untuk keranjang belanja di UI
data class CartItem(
    val produk: Produk,
    val quantity: Double
) {
    val subtotal: Double get() = produk.price * quantity
}