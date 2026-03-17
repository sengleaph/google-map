package com.sifu.mylocation.data.local

import android.content.Context
import com.google.gson.Gson
import com.sifu.mylocation.data.dto.BranchDto
import com.sifu.mylocation.data.dto.BranchListDto   // ← wrapper
import com.sifu.mylocation.domain.repository.LocalBranchDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

class LocalBranchDataSourceImpl(
    private val context: Context
) : LocalBranchDataSource {

    override suspend fun getBranchList(): BranchListDto =
        withContext(Dispatchers.IO) {
            val json = context.assets
                .open("atm_list_en.json")
                .bufferedReader()
                .use { it.readText() }

            Gson().fromJson(json, BranchListDto::class.java)
        }
}