package com.sifu.mylocation.data.dto


import com.google.gson.annotations.SerializedName

data class BranchListDto(
    @SerializedName("marker")
    val marker: List<BranchDto> = emptyList()
)

