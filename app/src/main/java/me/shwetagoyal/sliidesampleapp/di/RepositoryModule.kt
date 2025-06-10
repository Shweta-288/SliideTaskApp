package me.shwetagoyal.sliidesampleapp.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import me.shwetagoyal.sliidesampleapp.data.UserRepositoryImpl
import me.shwetagoyal.sliidesampleapp.domain.UserRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideUserRepository(client: HttpClient): UserRepository {
        return UserRepositoryImpl(client)
    }
}