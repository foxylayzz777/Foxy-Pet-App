package com.example

import com.example.data.model.PetEntity
import com.example.data.model.PetMood
import com.example.data.model.PetType
import org.junit.Assert.assertEquals
import org.junit.Test

class PetLogicTest {

    @Test
    fun testMoodCalculation_ecstaticWhenStatsHigh() {
        val pet = PetEntity(
            id = "fox",
            name = "Rusty",
            type = PetType.FOX,
            hunger = 90f,
            happiness = 90f,
            energy = 85f,
            cleanliness = 85f,
            health = 100f
        )
        assertEquals(PetMood.ECSTATIC, pet.calculateMood())
    }

    @Test
    fun testMoodCalculation_hungryWhenLowHunger() {
        val pet = PetEntity(
            id = "cat",
            name = "Luna",
            type = PetType.CAT,
            hunger = 20f,
            happiness = 70f,
            energy = 70f,
            cleanliness = 80f,
            health = 90f
        )
        assertEquals(PetMood.HUNGRY, pet.calculateMood())
    }

    @Test
    fun testMoodCalculation_sleepyWhenLowEnergy() {
        val pet = PetEntity(
            id = "dog",
            name = "Milo",
            type = PetType.DOG,
            hunger = 80f,
            happiness = 70f,
            energy = 15f,
            cleanliness = 80f,
            health = 90f
        )
        assertEquals(PetMood.SLEEPY, pet.calculateMood())
    }
}
