package com.muna.pamtkasir.model

import kotlinx.serialization.Serializable

@Serializable
data class Profile(
    val id: String,
    val email: String,
    val role: String
)