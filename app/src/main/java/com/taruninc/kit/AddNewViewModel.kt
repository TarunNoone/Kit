package com.taruninc.kit

import androidx.lifecycle.ViewModel
import kotlin.random.Random

class AddNewViewModel: ViewModel() {

    var temperature: Float = 0f

    fun setRandom() {
        temperature = Random.nextFloat() * 4 + 98
    }

}