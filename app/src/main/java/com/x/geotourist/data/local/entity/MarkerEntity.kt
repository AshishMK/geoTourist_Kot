package com.x.geotourist.data.local.entity

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class MarkerEntity() : Parcelable {
    @PrimaryKey(autoGenerate = true)
    lateinit var id: java.lang.Long

    lateinit var tourId: java.lang.Long

    lateinit var title: String

    lateinit var color: Integer

    lateinit var lat: java.lang.Double

    lateinit var lng: java.lang.Double

    var video: String? = null

    lateinit var markerOrder: Integer

    constructor(parcel: Parcel) : this() {
        id = parcel.readValue(java.lang.Long::class.java.classLoader) as java.lang.Long
        tourId  = parcel.readValue(java.lang.Long::class.java.classLoader) as java.lang.Long
        title = parcel.readString()!!
        color  = parcel.readValue(java.lang.Integer::class.java.classLoader) as Integer
        lat = parcel.readValue(Double::class.java.classLoader) as java.lang.Double
        lng = parcel.readValue(Double::class.java.classLoader) as java.lang.Double
        video = parcel.readString()
        markerOrder  = parcel.readValue(java.lang.Integer::class.java.classLoader) as Integer

    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeValue(id)
        dest.writeValue(tourId)
        dest.writeString(title)
        dest.writeValue(color)
        dest.writeValue(lat)
        dest.writeValue(lng)
        dest.writeString(video)
        dest.writeValue(markerOrder)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<MarkerEntity> {
        override fun createFromParcel(parcel: Parcel): MarkerEntity {
            return MarkerEntity(parcel)
        }

        override fun newArray(size: Int): Array<MarkerEntity?> {
            return arrayOfNulls(size)
        }
    }

}