package com.muna.pamtkasir

import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.serialization.json.Json

object SupabaseClientProvider {

    val client = createSupabaseClient(
        supabaseUrl = BuildConfig.SUPABASE_URL,
        supabaseKey = BuildConfig.SUPABASE_KEY
    ) {
        install(Auth)
        install(Postgrest) {
            serializer = io.github.jan.supabase.serializer.KotlinXSerializer(
                Json {
                    ignoreUnknownKeys = true
                    coerceInputValues = true
                }
            )
        }
    }
}