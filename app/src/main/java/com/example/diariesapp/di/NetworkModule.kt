package com.example.diariesapp.di

import android.content.Context
import com.example.diariesapp.connectivity.NetworkConnectivityObserver
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideNetworkModule(@ApplicationContext context: Context): NetworkConnectivityObserver =
        NetworkConnectivityObserver(context)
}