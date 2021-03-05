package com.taruninc.kit

import android.os.Parcel
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class User(
//    val userAadhar: String,
    var userFirstName: String = "Sherlock",
    var userLastName: String = "Holmes",
    val userGender: Boolean = true, //True = M, False = F
    val userAge: Int = 24,
    val userPhoneNumber: String = "221B",
    var userTemperature: Float = 98.6f,
    val userQRInfo: String = "Baker Street",
    val userMedHist: String = "Ageing"
) : Parcelable
