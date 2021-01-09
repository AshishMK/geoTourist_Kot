package com.x.geotourist.data.local.dao

import androidx.room.*
import com.x.geotourist.data.local.entity.MarkerEntity
import com.x.geotourist.data.local.entity.TourDataEntity

@Dao
interface TourDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertContents(entry: TourDataEntity): Long

    @Query("SELECT * FROM `TourDataEntity` order by id ")
    fun getTours(): List<TourDataEntity>

    @Query("SELECT * FROM `TourDataEntity` where id = :id")
    fun getTour(id: Long): TourDataEntity

    @Query("SELECT * FROM `TourDataEntity`  order by id desc limit 1")
    fun getLastTour(): TourDataEntity

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertMarker(marker: MarkerEntity): Long

    @Query("SELECT * FROM `MarkerEntity` where tourId = :tourId order by id ")
    fun getMarkers(tourId: Long): List<MarkerEntity>

    @Query("SELECT * FROM `MarkerEntity` where tourId = :tourId order by id desc limit 1")
    fun getLastMarker(tourId: Long): MarkerEntity

    @Query("SELECT * FROM `MarkerEntity` where id = :id")
    fun getMarker(id: Long): MarkerEntity

    @Update
    fun updateMarker(markerEntity: MarkerEntity): Int
}