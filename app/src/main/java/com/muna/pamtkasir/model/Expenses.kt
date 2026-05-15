package com.muna.pamtkasir.model

import kotlinx.serialization.Serializable

@Serializable
data class Expense(
    val id: String? = null,
    val kas_id: String,
    val description: String,
    val total: Double,
    val status: String = "active",
    val created_at: String? = null
)