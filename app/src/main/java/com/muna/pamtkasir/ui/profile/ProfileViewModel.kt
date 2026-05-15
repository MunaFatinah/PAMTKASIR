package com.muna.pamtkasir.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.muna.pamtkasir.SupabaseClientProvider
import com.muna.pamtkasir.model.Profile
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel() {

    private val _profile   = MutableStateFlow<Profile?>(null)
    val profile: StateFlow<Profile?> = _profile

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    init { loadProfile() }

    private fun loadProfile() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val userId = SupabaseClientProvider.client.auth.currentUserOrNull()?.id ?: return@launch
                val result = SupabaseClientProvider.client.postgrest
                    .from("profiles")
                    .select { filter { eq("id", userId) } }
                    .decodeSingle<Profile>()
                _profile.value = result
            } catch (e: Exception) {
                // tetap tampilkan halaman walau gagal load
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun logout(onLogout: () -> Unit) {
        viewModelScope.launch {
            try {
                SupabaseClientProvider.client.auth.signOut()
            } finally {
                onLogout()
            }
        }
    }
}