package com.example.diariesapp.di

import android.app.Application
import android.content.Context
import androidx.room.Room
import com.example.diariesapp.data.database.ImagesDatabase
import com.example.diariesapp.utils.Constants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
      //  @ApplicationContext context: Context,
        app : Application
    ): ImagesDatabase {
        return Room.databaseBuilder(
            context = app,
            klass = ImagesDatabase::class.java,
            name = Constants.IMAGES_DB
        ).build()
    }

    // NOT REALLY By The moment i can get them we the database provider
    @Provides
    @Singleton
    fun provideFirstDao(database: ImagesDatabase) = database.imageToUploadDao

    @Provides
    @Singleton
    fun provideSecondDao(database: ImagesDatabase) = database.imageToDeleteDao
}