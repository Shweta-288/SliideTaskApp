package me.shwetagoyal.sliidesampleapp.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import me.shwetagoyal.sliidesampleapp.data.StaticTokenProvider
import me.shwetagoyal.sliidesampleapp.network.TokenProvider

@Module
@InstallIn(SingletonComponent::class)
object TokenModule {

    @Provides
    fun provideTokenProvider(): TokenProvider = StaticTokenProvider()
}