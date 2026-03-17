package com.sifu.mylocation.data.dto


import com.google.gson.annotations.SerializedName

data class BranchDto(
    @SerializedName("address")
    val address: String = "",
    @SerializedName("business")
    val business: String = "",
    @SerializedName("discount")
    val discount: String = "",
    @SerializedName("email")
    val email: String = "",
    @SerializedName("fax")
    val fax: String = "",
    @SerializedName("fix_m")
    val fixM: String = "",
    @SerializedName("hour")
    val hour: String = "",
    @SerializedName("id")
    val id: String = "",
    @SerializedName("lat")
    val lat: String = "",
    @SerializedName("lng")
    val lng: String = "",
    @SerializedName("name")
    val name: String = "",
    @SerializedName("note")
    val note: String = "",
    @SerializedName("openacc_m")
    val openaccM: String = "",
    @SerializedName("photo")
    val photo: String = "",
    @SerializedName("place_id")
    val placeId: String = "",
    @SerializedName("province")
    val province: String = "",
    @SerializedName("self_m")
    val selfM: String = "",
    @SerializedName("service")
    val service: String = "",
    @SerializedName("status")
    val status: String = "",
    @SerializedName("tel")
    val tel: String = "",
    @SerializedName("type")
    val type: String = "",
    @SerializedName("validity_date")
    val validityDate: String = "",
    @SerializedName("website")
    val website: String = ""
)