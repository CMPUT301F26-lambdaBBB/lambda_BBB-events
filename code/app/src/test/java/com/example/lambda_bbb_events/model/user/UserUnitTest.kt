package com.example.lambda_bbb_events.model.user

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests the [User] model and its roles.
 */
class UserUnitTest {

    @Test
    fun updateProfileChangesOnlyGivenFields() {
        val user = User(deviceId = "d", name = "Ed", email = "ed@example.com", phone = "1", profilePictureImageId = "img")

        user.updateProfile(name = "Edward")

        assertEquals("Edward", user.name)
        assertEquals("ed@example.com", user.email)
        assertEquals("1", user.phone)
        assertEquals("img", user.profilePictureImageId)

        user.updateProfile(email = "new@example.com", phone = "2", imageId = "img-2")
        assertEquals("new@example.com", user.email)
        assertEquals("2", user.phone)
        assertEquals("img-2", user.profilePictureImageId)
    }

    @Test
    fun updateProfileRejectsBlankFieldsWithoutChangingAnything() {
        val user = User(name = "Ed", email = "ed@example.com", phone = "1")

        assertThrows(IllegalArgumentException::class.java) { user.updateProfile(name = " ") }
        assertThrows(IllegalArgumentException::class.java) { user.updateProfile(email = "") }
        assertThrows(IllegalArgumentException::class.java) { user.updateProfile(name = "New", phone = "") }

        assertEquals("Ed", user.name)
        assertEquals("1", user.phone)
    }

    @Test
    fun setAllowNotificationsTogglesSetting() {
        val user = User()
        assertTrue(user.notificationsEnabled)

        user.setAllowNotifications(false)
        assertFalse(user.notificationsEnabled)
    }

    @Test
    fun rolesHaveMatchingRoleField() {
        assertEquals(UserRole.ENTRANT, Entrant().role)
        assertEquals(UserRole.OWNER, CampsiteOwner().role)
        assertEquals(UserRole.ADMIN, Admin().role)
    }
}
