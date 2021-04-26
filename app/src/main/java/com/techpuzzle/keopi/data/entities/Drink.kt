package com.techpuzzle.keopi.data.entities

import android.os.Parcel
import android.os.Parcelable

data class Drink(
    val id: String = "",
    val cijena: String = "",
    val ime: String = "",
    val opis: String = "",
    val vrsta: Int = 0,
    val okusi: List<String> = emptyList(),
    val prviArtikli: List<String> = emptyList(),
    val drugiArtikli: List<String> = emptyList(),
    val okus: Int = 0
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readInt(),
        parcel.createStringArrayList()?.toList() ?: listOf(),
        parcel.createStringArrayList()?.toList() ?: listOf(),
        parcel.createStringArrayList()?.toList() ?: listOf(),
        parcel.readInt()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(cijena)
        parcel.writeString(ime)
        parcel.writeString(opis)
        parcel.writeInt(vrsta)
        parcel.writeStringList(okusi)
        parcel.writeStringList(prviArtikli)
        parcel.writeStringList(drugiArtikli)
        parcel.writeInt(okus)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Drink> {
        override fun createFromParcel(parcel: Parcel): Drink {
            return Drink(parcel)
        }

        override fun newArray(size: Int): Array<Drink?> {
            return arrayOfNulls(size)
        }
    }
}