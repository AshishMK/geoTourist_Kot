package com.x.geotourist.di.module

import android.app.Application
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
            .addMigrations(MIGRATION_1_2)
            .allowMainThreadQueries().build()
    }

    /**Add new column to star a content entity */
    val MIGRATION_1_2: Migration = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(
                "ALTER TABLE MarkerEntity "
                        + " ADD COLUMN markerOrder INTEGER DEFAULT 0 NOT NULL"
            )
        }
    }

    @Provides
    @Singleton
    internal fun provideDao(appDatabase: AppDatabase): TourDao {
        return appDatabase.tourDao()
    }



}
