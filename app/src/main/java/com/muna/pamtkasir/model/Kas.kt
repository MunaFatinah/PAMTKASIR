package com.muna.pamtkasir.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Kas(
    val id: String = "",
    @SerialName("user_id") val userId: String = "",
    val name: String = "",
    val balance: Double = 0.0,
    @SerialName("is_active") val isActive: Boolean = true,
    @SerialName("created_at") val createdAt: String = ""
)

@Serializable
data class KasLog(
    val id: String = "",
    @SerialName("kas_id") val kasId: String = "",
    val amount: Double = 0.0,
    val description: String = "",
    @SerialName("created_at") val createdAt: String = ""
)