package com.sifu.mylocation.di

import com.sifu.mylocation.data.repository.LocationRepositoryImpl
import com.sifu.mylocation.domain.repository.LocationRepository
import com.sifu.mylocation.domain.usecasae.AddMarkerUseCase
import com.sifu.mylocation.domain.usecasae.GetCurrentLocationUseCase
import com.sifu.mylocation.domain.usecasae.GetSavedMarkersUseCase
import com.sifu.mylocation.domain.usecasae.RemoveMarkerUseCase
import com.sifu.mylocation.domain.usecasae.SearchLocationUseCase
import com.sifu.mylocation.presentation.viewmodel.MapViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val appModule = module {

    // ── Data Layer ────────────────────────────────────────────────────────────
    single<LocationRepository> {
        LocationRepositoryImpl(context = androidContext())
    }

    // ── Domain Layer – Use Cases ──────────────────────────────────────────────
    factory { GetCurrentLocationUseCase(repository = get()) }
    factory { GetSavedMarkersUseCase(repository = get()) }
    factory { AddMarkerUseCase(repository = get()) }
    factory { RemoveMarkerUseCase(repository = get()) }
    factory { SearchLocationUseCase(repository = get()) }

    // ── Presentation Layer – ScreenModels ─────────────────────────────────────
    factory {
        MapViewModel(
            getCurrentLocationUseCase = get(),
            getSavedMarkersUseCase = get(),
            addMarkerUseCase = get(),
            removeMarkerUseCase = get(),
            searchLocationUseCase = get()
        )
    }
}