package com.example.lambda_bbb_events

import androidx.lifecycle.Lifecycle
import androidx.navigation.NavController
import com.example.lambda_bbb_events.view.ui.components.navigation.popBackStackIfResumed
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BackNavigationUnitTest {
    private val navController = mockk<NavController>()

    private fun lifecycleIn(state: Lifecycle.State): Lifecycle {
        val lifecycle = mockk<Lifecycle>()
        every { lifecycle.currentState } returns state
        return lifecycle
    }

    @Test
    fun resumedScreenGoesBack() {
        every { navController.popBackStack() } returns true

        assertTrue(navController.popBackStackIfResumed(lifecycleIn(Lifecycle.State.RESUMED)))
        verify(exactly = 1) { navController.popBackStack() }
    }

    @Test
    fun screenThatIsLeavingIgnoresBack() {
        // A popped screen drops below RESUMED while it animates out
        for (state in listOf(Lifecycle.State.STARTED, Lifecycle.State.CREATED, Lifecycle.State.DESTROYED)) {
            assertFalse(navController.popBackStackIfResumed(lifecycleIn(state)))
        }
        verify(exactly = 0) { navController.popBackStack() }
    }

    @Test
    fun failedPopIsReported() {
        every { navController.popBackStack() } returns false

        assertFalse(navController.popBackStackIfResumed(lifecycleIn(Lifecycle.State.RESUMED)))
    }
}
