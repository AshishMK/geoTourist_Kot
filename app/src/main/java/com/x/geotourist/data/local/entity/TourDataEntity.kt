package com.x.geotourist.data.local.entity

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class TourDataEntity() : Parcelable {
    @PrimaryKey(autoGenerate = true)
    lateinit var id: java.lang.Long

    lateinit var createdAt: java.lang.Long

    lateinit var title: String

    constructor(parcel: Parcel) : this() {
        id = parcel.readValue(java.lang.Long::class.java.classLoader) as java.lang.Long
        createdAt = parcel.readValue(java.lang.Long::class.java.classLoader) as java.lang.Long
        title = parcel.readString()!!

    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeValue(id)
        dest.writeValue(createdAt)
        dest.writeString(this.title)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<TourDataEntity> {
        override fun createFromParcel(parcel: Parcel): TourDataEntity {
            return TourDataEntity(parcel)
        }

        override fun newArray(size: Int): Array<TourDataEntity?> {
            return arrayOfNulls(size)
        }
    }

}