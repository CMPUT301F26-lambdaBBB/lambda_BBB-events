package com.example.lambda_bbb_events.data

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.lambda_bbb_events.model.local.SharedPreferencesNotificationMuteStore
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

/** Tests saving mutes on the device, in a file used only by these tests. */
@RunWith(AndroidJUnit4::class)
class NotificationMuteStoreTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()

    private fun openStore() = SharedPreferencesNotificationMuteStore(context, FILE)

    @After
    fun cleanUp() {
        context.deleteSharedPreferences(FILE)
    }

    @Test
    fun mutesCanBeSavedReplacedAndRemoved() {
        openStore().save("a", 5L)
        openStore().save("a", Long.MAX_VALUE)
        openStore().save("b", 7L)
        assertEquals(mapOf("a" to Long.MAX_VALUE, "b" to 7L), openStore().load())

        openStore().remove("a")
        assertEquals(mapOf("b" to 7L), openStore().load())
    }

    private companion object {
        const val FILE = "test_notification_mutes"
    }
}
