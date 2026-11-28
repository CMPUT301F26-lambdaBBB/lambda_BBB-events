package com.example.lambda_bbb_events.view.ui.components.navigation

import androidx.navigation.NavController
import com.example.lambda_bbb_events.model.campsite.Campsite
import com.example.lambda_bbb_events.view.ui.user_screens.explore.CampsiteSection

/**
 * Opens the expanded campsite list for a selected section.
 *
 * @param navController The application's navigation controller.
 * @param section The selected campsite section.
 * @param campsites The complete list of campsites in the section.
 */
fun navigateToSeeAll(
    navController: NavController,
    section: CampsiteSection,
    campsites: Collection<Campsite>
) {
    val currentEntry = navController.currentBackStackEntry ?: return
    val source = currentEntry.destination.route ?: return

    currentEntry.savedStateHandle["seeAllCampsiteIds"] =
        ArrayList(
            campsites.map { it.campsiteId }.distinct()
        )

    navController.navigate(
        "see_all_campsites/$source/${section.name}"
    ) {
        launchSingleTop = true
    }
}