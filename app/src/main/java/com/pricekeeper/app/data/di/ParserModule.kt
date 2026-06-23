package com.pricekeeper.app.data.di

import com.pricekeeper.app.data.parser.DefaultParser
import com.pricekeeper.app.data.parser.HemaParser
import com.pricekeeper.app.data.parser.ParserStrategySelector
import com.pricekeeper.app.data.parser.ReceiptParser
import com.pricekeeper.app.data.parser.YonghuiParser
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ParserModule {

    @Provides
    @Singleton
    fun provideDefaultParser(): DefaultParser = DefaultParser()

    @Provides
    @Singleton
    fun provideYonghuiParser(): YonghuiParser = YonghuiParser()

    @Provides
    @Singleton
    fun provideHemaParser(): HemaParser = HemaParser()

    @Provides
    @Singleton
    fun provideReceiptParser(selector: ParserStrategySelector): ReceiptParser = selector
}
