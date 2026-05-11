package com.muna.pamtkasir.auth

import com.muna.pamtkasir.SupabaseClientProvider.client
import com.muna.pamtkasir.model.Profile
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.from


class AuthRepository {

    suspend fun register(email: String, password: String) {
        client.auth.signUpWith(Email) {
            this.email = email
            this.password = password
        }
    }

    suspend fun login(email: String, password: String): String {

        client.auth.signInWith(Email) {
            this.email = email
            this.password = password
        }

        val userId = client.auth.currentUserOrNull()?.id
            ?: throw Exception("Login gagal")

        val profile = client
            .from("profiles")
            .select {
                filter {
                    eq("id", userId)
                }
            }
            .decodeSingle<Profile>()

        return profile.role
    }

    suspend fun logout() {
        client.auth.signOut()
    }
}