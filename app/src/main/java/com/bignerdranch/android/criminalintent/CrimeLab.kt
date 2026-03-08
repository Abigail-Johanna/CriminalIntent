package com.bignerdranch.android.criminalintent

import android.content.Context
import java.util.UUID

class CrimeLab private constructor(context: Context) {

    private val crimes: MutableMap<UUID, Crime> = LinkedHashMap()

    init {
        for (i in 0 until 100) {
            val crime = Crime()
            crime.title = "Crime #$i"
            crime.isSolved = i % 2 == 0
            crime.requiresPolice = i % 5 == 0
            crimes[crime.id] = crime
        }
    }

    fun getCrimes(): List<Crime> {
        return crimes.values.toList()
    }

    fun getCrime(id: UUID): Crime? {
        return crimes[id]
    }

    companion object {
        private var INSTANCE: CrimeLab? = null

        fun get(context: Context): CrimeLab {
            return INSTANCE ?: CrimeLab(context).also {
                INSTANCE = it
            }
        }
    }
}