package com.sifu.mylocation.data.local

import android.content.Context
import com.sifu.mylocation.data.dto.AtmDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

class LocalAtmDataSource(private val context: Context) {

    private val json = Json { ignoreUnknownKeys = true }

    suspend fun getAtms(): List<AtmDto> = withContext(Dispatchers.IO) {
        val jsonString = context.assets
            .open("atmMockup.json")
            .bufferedReader()
            .use { it.readText() }
        json.decodeFromString<List<AtmDto>>(jsonString)
    }
}