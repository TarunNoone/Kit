package com.taruninc.kit.database

import android.annotation.SuppressLint
import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parceler
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(tableName = "user_list")
// don't worry about this error.
data class User (
    @PrimaryKey(autoGenerate = true)
    val UUID: Int,
    var userFirstName: String,
    var userLastName: String,
    var userTemperature: Float
): Parcelable