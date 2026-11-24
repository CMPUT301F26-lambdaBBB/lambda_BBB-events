package com.example.lambda_bbb_events.controller

import com.example.lambda_bbb_events.model.local.InMemoryNotificationMuteStore
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Tests muting and unmuting notifications. */
class NotificationControllerUnitTest {
    private var time = 1_000_000L
    private val tenMinutes = 10 * 60_000L
    private val store = InMemoryNotificationMuteStore()
    private val controller = NotificationController(store) { time }

    @Test
    fun nothingIsMutedAtFirst() {
        assertFalse(controller.isMuted("a"))
        assertNull(controller.nextUnmuteAt)
    }

    @Test
    fun muteLastsUntilUnmuted() {
        controller.mute("a")
        time += 365L * 24 * 3_600_000
        controller.removeEndedMutes()

        assertTrue(controller.isMuted("a"))
        assertFalse(controller.isMuted("b"))
        assertNull(controller.nextUnmuteAt)

        controller.unmute("a")

        assertFalse(controller.isMuted("a"))
        assertTrue(store.load().isEmpty())
    }

    @Test
    fun temporaryMuteEndsAfterTenMinutes() {
        controller.muteTemporarily("a")
        assertEquals(time + tenMinutes, controller.mutedUntil("a"))

        time += tenMinutes - 1
        assertTrue(controller.isMuted("a"))

        time += 1
        assertFalse(controller.isMuted("a"))
    }

    @Test
    fun removingEndedMutesKeepsTheOthers() {
        controller.muteTemporarily("ended")
        controller.mute("forever")
        time += tenMinutes / 2
        controller.muteTemporarily("later")
        time += tenMinutes / 2

        controller.removeEndedMutes()

        assertEquals(setOf("forever", "later"), store.load().keys)
        assertEquals(controller.mutedUntil("later"), controller.nextUnmuteAt)
        assertEquals(tenMinutes / 2, controller.millisUntil(controller.nextUnmuteAt!!))
    }

    @Test
    fun newMuteReplacesTheOldOne() {
        controller.mute("a")
        controller.muteTemporarily("a")
        assertEquals(time + tenMinutes, controller.mutedUntil("a"))

        controller.mute("a")
        assertEquals(NotificationController.FOREVER, controller.mutedUntil("a"))
    }

    @Test
    fun mutesAreKeptAfterARestart() {
        controller.mute("a")
        controller.muteTemporarily("b")

        val restarted = NotificationController(store) { time }

        assertEquals(NotificationController.FOREVER, restarted.mutedUntil("a"))
        assertEquals(time + tenMinutes, restarted.mutedUntil("b"))
    }
}
