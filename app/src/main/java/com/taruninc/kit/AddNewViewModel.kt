package com.taruninc.kit

import androidx.lifecycle.ViewModel
import kotlin.random.Random

class AddNewViewModel: ViewModel() {

    var temperature: Float = 0f
    var qrInfo: String = ""

    fun setRandom() {
        temperature = Random.nextFloat() * 4 + 98
    }

}