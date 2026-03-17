package com.sifu.mylocation.data.mapper

import com.sifu.mylocation.data.dto.BranchDto
import com.sifu.mylocation.domain.model.BranchMarker
import com.sifu.mylocation.domain.model.MapLocation
import com.sifu.mylocation.domain.model.MapMarker

// BranchDto → BranchMarker (returns null if lat/lng invalid → skipped silently)
fun BranchDto.toDomain(): BranchMarker? {
    val latitude = lat.trim().toDoubleOrNull() ?: return null
    val longitude = lng.trim().toDoubleOrNull() ?: return null
    return BranchMarker(
        id = id,
        name = name,
        address = address,
        tel = tel,
        email = email,
        photo = photo,
        lat = latitude,
        lng = longitude,
        type = type,
        service = service,
        hour = hour,
        province = province,
        status = status,
        placeId = placeId,
        business = business,
        discount = discount,
        fax = fax,
        note = note,
        website = website,
        validityDate = validityDate,
        selfM = selfM,
        openaccM = openaccM,
        fixM = fixM,
        )
}

// BranchMarker → MapMarker (used by existing MarkerDetailsSheet)
fun BranchMarker.toMapMarker() = MapMarker(
    id = id,
    location = MapLocation(latitude = lat, longitude = lng),
    title = name,
    snippet = buildString {
        append(address)
        if (tel.isNotBlank()) append("\n☎ $tel")
        if (hour.isNotBlank()) append("\n⏰ $hour")
        if (status.isNotBlank()) append("\n● $status")
    }
)