package com.pricekeeper.app.data.di

import com.pricekeeper.app.data.export.ExportRepository
import com.pricekeeper.app.data.export.ExportRepositoryImpl
import com.pricekeeper.app.data.export.ImportRepository
import com.pricekeeper.app.data.export.ImportRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ExportModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        prettyPrint = false
        encodeDefaults = true
        ignoreUnknownKeys = true
    }

    @Provides
    @Singleton
    fun provideExportRepository(impl: ExportRepositoryImpl): ExportRepository = impl

    @Provides
    @Singleton
    fun provideImportRepository(impl: ImportRepositoryImpl): ImportRepository = impl
}
