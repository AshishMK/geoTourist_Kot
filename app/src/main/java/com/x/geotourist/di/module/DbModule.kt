package com.x.geotourist.di.module

import android.app.Application
import androidx.room.Room
import com.x.geotourist.data.local.AppDatabase
import com.x.geotourist.data.local.dao.TourDao
import dagger.Module
import dagger.Provides

import javax.inject.Singleton

@Module
class DbModule {

    @Provides
    @Singleton
    internal fun provideDatabase(application: Application): AppDatabase {
        return Room.databaseBuilder(
            application, AppDatabase::class.java, "tour.db")
            .allowMainThreadQueries().build()
    }

    @Provides
    @Singleton
    internal fun provideDao(appDatabase: AppDatabase): TourDao {
        return appDatabase.tourDao()
    }



}
