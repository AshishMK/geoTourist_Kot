package com.x.geotourist.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.x.geotourist.data.local.dao.TourDao
import com.x.geotourist.data.local.entity.MarkerEntity
import com.x.geotourist.data.local.entity.TourDataEntity

@Database(entities = [TourDataEntity::class, MarkerEntity::class], version = 2, exportSchema = false)

abstract class AppDatabase : RoomDatabase() {

    abstract fun tourDao(): TourDao

}
