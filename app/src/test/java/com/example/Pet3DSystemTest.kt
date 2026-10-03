package com.example

import com.example.data.model.PetEntity
import com.example.data.model.PetMood
import com.example.data.model.PetType
import com.example.ui.pet3d.controller.AnimationState
import com.example.ui.pet3d.controller.Pet3DController
import com.example.ui.pet3d.controller.PetWidgetDataManager
import com.example.ui.pet3d.engine.Pet3DModelFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class Pet3DSystemTest {

    @Test
    fun test3DModelCreation_Fox() {
        val fox = Pet3DModelFactory.buildPet(PetType.FOX)
        assertNotNull(fox.rootBone)
        assertNotNull(fox.boneHead)
        assertNotNull(fox.boneTail1)
        assertNotNull(fox.boneTail2)
        assertNotNull(fox.boneTail3)
        assertNotNull(fox.socketHead)
        assertNotNull(fox.socketGlasses)
        assertNotNull(fox.socketCollar)
        assertTrue(fox.scene.meshParts.isNotEmpty())
    }

    @Test
    fun test3DModelCreation_CatAndDog() {
        val cat = Pet3DModelFactory.buildPet(PetType.CAT)
        val dog = Pet3DModelFactory.buildPet(PetType.DOG)
        assertEquals(PetType.CAT, cat.type)
        assertEquals(PetType.DOG, dog.type)
        assertNotNull(cat.boneLeftEar)
        assertNotNull(dog.boneLeftEar)
    }

    @Test
    fun testAnimationController_Transitions() {
        val controller = Pet3DController(PetType.FOX)
        assertEquals(AnimationState.IDLE, controller.animationController.currentState)

        controller.onFeedAction()
        assertEquals(AnimationState.EATING, controller.animationController.currentState)

        controller.onCleanAction()
        assertEquals(AnimationState.CLEANING, controller.animationController.currentState)

        controller.onPetAction()
        assertEquals(AnimationState.PET_HAPPY, controller.animationController.currentState)

        controller.onSleepAction(true)
        assertEquals(AnimationState.SLEEPING, controller.animationController.currentState)

        controller.onSleepAction(false)
        assertEquals(AnimationState.IDLE, controller.animationController.currentState)
    }

    @Test
    fun testCustomization_AttachmentsAndSkins() {
        val controller = Pet3DController(PetType.FOX)
        val pet = PetEntity(
            id = "fox",
            name = "Rusty",
            type = PetType.FOX,
            skinId = "skin_golden",
            selectedHat = "hat_crown",
            selectedGlasses = "glasses_shades",
            selectedCollar = "collar_bell",
            selectedAccessory = "acc_wings"
        )
        controller.syncFromEntity(pet)

        val crownMesh = controller.character.scene.meshParts.find { it.name == "item_hat_crown" }
        assertNotNull(crownMesh)
        assertTrue(crownMesh!!.isVisible)

        val shadesMesh = controller.character.scene.meshParts.find { it.name == "item_glasses_shades" }
        assertNotNull(shadesMesh)
        assertTrue(shadesMesh!!.isVisible)
    }

    @Test
    fun testWidgetDataManager_SnapshotSync() {
        val pet = PetEntity(
            id = "dog",
            name = "Milo",
            type = PetType.DOG,
            hunger = 85f,
            happiness = 90f,
            energy = 75f
        )
        PetWidgetDataManager.updateSnapshot(pet, level = 4)
        val snapshot = PetWidgetDataManager.getSnapshot()
        assertNotNull(snapshot)
        assertEquals("Milo", snapshot!!.petName)
        assertEquals(PetType.DOG, snapshot.type)
        assertEquals(4, snapshot.level)
        assertEquals(85, snapshot.hungerPercent)
    }
}
