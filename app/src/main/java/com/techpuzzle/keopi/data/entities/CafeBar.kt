package com.techpuzzle.keopi.data.entities

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class CafeBar(

    @PrimaryKey(autoGenerate = false)
    val id: String = "",
    val address: String = "",
    val bio: String = "",
    val name: String = "",
    val capacity: String = "",
    val betting: Boolean = false,
    val location: String = "",
    val cityId: String = "",
    val areaId: String = "",
    val latitude: String = "",
    val longitude: String = "",
    val music: String = "",
    val age: String = "",
    val smoking: Boolean = false,
    val startingWorkTime: Int = 0,
    val endingWorkTime: Int = 0,
    val picture: String = "",
    val instagram: String = "",
    val facebook: String = "",
    val terrace: Boolean = false,
    val dart: Boolean = false,
    val billiards: Boolean = false,
    val hookah: Boolean = false,
    val playground: Boolean = false
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readByte() != 0.toByte(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readByte() != 0.toByte(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readByte() != 0.toByte(),
        parcel.readByte() != 0.toByte(),
        parcel.readByte() != 0.toByte(),
        parcel.readByte() != 0.toByte(),
        parcel.readByte() != 0.toByte()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(address)
        parcel.writeString(bio)
        parcel.writeString(name)
        parcel.writeString(capacity)
        parcel.writeByte(if (betting) 1 else 0)
        parcel.writeString(location)
        parcel.writeString(cityId)
        parcel.writeString(areaId)
        parcel.writeString(latitude)
        parcel.writeString(longitude)
        parcel.writeString(music)
        parcel.writeString(age)
        parcel.writeByte(if (smoking) 1 else 0)
        parcel.writeInt(startingWorkTime)
        parcel.writeInt(endingWorkTime)
        parcel.writeString(picture)
        parcel.writeString(instagram)
        parcel.writeString(facebook)
        parcel.writeByte(if (terrace) 1 else 0)
        parcel.writeByte(if (dart) 1 else 0)
        parcel.writeByte(if (billiards) 1 else 0)
        parcel.writeByte(if (hookah) 1 else 0)
        parcel.writeByte(if (playground) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<CafeBar> {
        override fun createFromParcel(parcel: Parcel): CafeBar {
            return CafeBar(parcel)
        }

        override fun newArray(size: Int): Array<CafeBar?> {
            return arrayOfNulls(size)
        }
    }
}
