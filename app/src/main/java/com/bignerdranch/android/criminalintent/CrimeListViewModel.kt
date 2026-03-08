package com.bignerdranch.android.criminalintent

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class CrimeListViewModel : ViewModel() {

    private val _crimes = MutableLiveData<List<Crime>>()
    val crimes: LiveData<List<Crime>> = _crimes

    init {
        val crimeList = mutableListOf<Crime>()
        for (i in 0 until 100) {
            val crime = Crime(
                title = "Crime #$i",
                isSolved = i % 2 == 0,
                requiresPolice = i % 2 != 0
            )
            crimeList += crime
        }
        _crimes.value = crimeList
    }
}