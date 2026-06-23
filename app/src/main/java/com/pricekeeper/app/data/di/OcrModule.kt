package com.pricekeeper.app.data.di

import android.content.Context
import com.pricekeeper.app.data.ocr.MlKitOcrEngine
import com.pricekeeper.app.data.ocr.OcrEngine
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object OcrModule {

    @Provides
    @Singleton
    fun provideOcrEngine(
        @ApplicationContext context: Context
    ): OcrEngine = MlKitOcrEngine(context)
}
