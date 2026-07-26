package com.example.service

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.storage.Storage

object SupabaseClientProvider {
    val client = createSupabaseClient(
        supabaseUrl = "https://akvlcblgmkcyouwifbcd.supabase.co",
        supabaseKey = "sb_publishable_EqtJ203upZidqltX3zQHKQ_EONcs_Wg"
    ) {
        install(Postgrest)
        install(Auth) {}
        install(Storage)
    }
}
