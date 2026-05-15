package com.muna.pamtkasir.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Customer(
    val id: String = "",

    @SerialName("user_id")
    val userId: String = "",

    val name: String = "",

    val phone: String = "",

    @SerialName("is_active")
    val isActive: Boolean = true,

    @SerialName("created_at")
    val createdAt: String = ""
)

@Serializable
data class CustomerLog(
    val id: String = "",

    @SerialName("customer_id")
    val customerId: String = "",

    @SerialName("activity_type")
    val activityType: String = "",

    @SerialName("created_at")
    val createdAt: String = ""
)