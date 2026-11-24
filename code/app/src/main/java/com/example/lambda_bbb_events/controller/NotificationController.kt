package com.example.lambda_bbb_events.controller

import androidx.compose.runtime.mutableStateMapOf
import com.example.lambda_bbb_events.model.local.InMemoryNotificationMuteStore
import com.example.lambda_bbb_events.model.local.NotificationMuteStore

/**
 * Controller for muting notifications from a person or in a category.
 *
 * @param store Where mutes are saved.
 * @param now The current time, in milliseconds since 1970. Replaced in tests.
 */
class NotificationController(
    private val store: NotificationMuteStore = InMemoryNotificationMuteStore(),
    private val now: () -> Long = System::currentTimeMillis
) {
    /** When each mute ends, by the muted ID. Compose state, so screens follow changes. */
    private val mutes = mutableStateMapOf<String, Long>().apply { putAll(store.load()) }

    /** When the next mute ends, or `null` if none of them end by themselves. */
    val nextUnmuteAt: Long?
        get() = mutes.values.filter { it != FOREVER }.minOrNull()

    /**
     * Gets when a mute ends.
     *
     * @param id The person or category.
     * @return The time in milliseconds since 1970, [FOREVER] if it lasts until it is unmuted,
     * or `null` if it is not muted.
     */
    fun mutedUntil(id: String): Long? = mutes[id]?.takeIf { it > now() }

    /**
     * Checks if notifications are muted. Check this before showing a notification.
     *
     * @param id The person or category.
     * @return `true` if it is muted, `false` otherwise.
     */
    fun isMuted(id: String): Boolean = mutedUntil(id) != null

    /**
     * Mutes a person or category until it is unmuted.
     *
     * @param id The person or category.
     */
    fun mute(id: String) = muteUntil(id, FOREVER)

    /**
     * Mutes a person or category for [TEMPORARY_MUTE_MINUTES], starting now.
     *
     * @param id The person or category.
     */
    fun muteTemporarily(id: String) = muteUntil(id, now() + TEMPORARY_MUTE_MINUTES * 60_000L)

    /**
     * Unmutes a person or category.
     *
     * @param id The person or category.
     */
    fun unmute(id: String) {
        mutes.remove(id)
        store.remove(id)
    }

    /** Unmutes everything whose mute has ended. */
    fun removeEndedMutes() {
        mutes.filterValues { it <= now() }.keys.forEach { unmute(it) }
    }

    /**
     * Gets how long it is until a time.
     *
     * @param time The time in milliseconds since 1970.
     * @return The remaining milliseconds. Negative if the time has passed.
     */
    fun millisUntil(time: Long): Long = time - now()

    private fun muteUntil(id: String, until: Long) {
        mutes[id] = until
        store.save(id, until)
    }

    companion object {
        /** The end of a mute that lasts until it is unmuted. */
        const val FOREVER = Long.MAX_VALUE

        /** How long a temporary mute lasts. */
        const val TEMPORARY_MUTE_MINUTES = 10
    }
}
