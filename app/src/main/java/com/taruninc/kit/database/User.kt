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
    val userAadhar: String,
    var userFirstName: String,
    var userLastName: String,
    val userGender: Boolean, //True = M, False = F
    val userAge: Int,
    val userPhoneNumber: String,
    var userTemperature: Float,
    val userQRInfo: String,
    val userMedHist: String
): Parcelable