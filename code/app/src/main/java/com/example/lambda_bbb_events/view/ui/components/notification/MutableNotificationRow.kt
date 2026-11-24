package com.example.lambda_bbb_events.view.ui.components.notification

import android.text.format.DateFormat
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.NotificationsOff
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.AbsoluteAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import com.example.lambda_bbb_events.controller.NotificationController
import java.util.Date
import kotlin.math.roundToInt

/*
 * Defines a notification row that can be muted by pressing and holding it.
 */

/**
 * Displays a person or category. Pressing and holding it lifts it and opens a menu to mute,
 * mute for a short time, or unmute it. The menu grows out of the row where it was pressed.
 *
 * @param id Identifies the person or category.
 * @param title The name of the person or category.
 * @param unmutedText Displayed below the title while it is not muted.
 * @param icon Displayed at the start of the row.
 * @param notifications Controller that mutes and unmutes.
 * @param heldRow Told when this row is held. Use the one given to the [HeldRowBox] around it.
 */
@Composable
fun MutableNotificationRow(
    id: String,
    title: String,
    unmutedText: String,
    icon: ImageVector,
    notifications: NotificationController,
    heldRow: HeldRowState
) {
    var menuOpen by remember(id) { mutableStateOf(false) }
    // Where the menu opens, in pixels from the left of the row
    var menuX by remember(id) { mutableStateOf(0) }
    // Where a finger is pressing the row. Nowhere when it is held by using a screen reader.
    val press = remember(id) { arrayOfNulls<Offset>(1) }
    val layer = rememberGraphicsLayer()
    val row = remember(layer) { HeldRowState.Row(layer) }

    if (menuOpen) {
        DisposableEffect(heldRow, row) {
            heldRow.hold(row)
            onDispose { heldRow.release() }
        }
    }

    val mutedUntil = notifications.mutedUntil(id)
    val isMuted = mutedUntil != null

    Box {
        NotificationRow(
            title = title,
            subtitle = muteText(mutedUntil) ?: unmutedText,
            onClick = {},
            onLongClick = {
                menuX = press[0]?.x?.roundToInt() ?: 0
                menuOpen = true
            },
            onLongClickLabel = "Mute options",
            isMuted = isMuted,
            modifier = Modifier
                .testTag("notification_$id")
                .recordRow(row)
                .pointerInput(id) {
                    awaitEachGesture {
                        press[0] = awaitFirstDown(
                            requireUnconsumed = false,
                            pass = PointerEventPass.Initial
                        ).position
                        try {
                            do {
                                val event = awaitPointerEvent(PointerEventPass.Initial)
                            } while (event.changes.any { it.pressed })
                        } finally {
                            press[0] = null
                        }
                    }
                },
            leadingIcon = {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        )

        // The menu opens above or below its parent, which is slightly taller than the row to
        // leave a gap
        Box(modifier = Modifier.matchParentSize()) {
            Box(
                modifier = Modifier
                    .align(AbsoluteAlignment.TopLeft)
                    .layout { measurable, constraints ->
                        val gap = MENU_GAP.roundToPx()
                        val height = constraints.maxHeight + 2 * gap
                        val anchor = measurable.measure(Constraints.fixed(0, height))
                        layout(0, constraints.maxHeight) { anchor.place(menuX, -gap) }
                    }
            ) {
                DropdownMenu(
                    expanded = menuOpen,
                    onDismissRequest = { menuOpen = false },
                    shape = RoundedCornerShape(16.dp),
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    MuteOption(
                        text = "Mute",
                        icon = Icons.Outlined.NotificationsOff,
                        enabled = mutedUntil != NotificationController.FOREVER,
                        tag = "mute_option",
                        onClick = {
                            notifications.mute(id)
                            menuOpen = false
                        }
                    )
                    MuteOption(
                        text = "Mute for ${NotificationController.TEMPORARY_MUTE_MINUTES} minutes",
                        icon = Icons.Outlined.Schedule,
                        enabled = true,
                        tag = "mute_temporarily_option",
                        onClick = {
                            notifications.muteTemporarily(id)
                            menuOpen = false
                        }
                    )
                    MuteOption(
                        text = "Unmute",
                        icon = Icons.Outlined.NotificationsActive,
                        enabled = isMuted,
                        tag = "unmute_option",
                        onClick = {
                            notifications.unmute(id)
                            menuOpen = false
                        }
                    )
                }
            }
        }
    }
}

/** Describes a mute, such as "Muted until 3:45 PM". `null` if it is not muted. */
@Composable
private fun muteText(mutedUntil: Long?): String? = when (mutedUntil) {
    null -> null
    NotificationController.FOREVER -> "Muted"
    else -> {
        // Follows the device's 12 or 24 hour setting
        val timeFormat = DateFormat.getTimeFormat(LocalContext.current)
        "Muted until " + timeFormat.format(Date(mutedUntil))
    }
}

@Composable
private fun MuteOption(
    text: String,
    icon: ImageVector,
    enabled: Boolean,
    tag: String,
    onClick: () -> Unit
) {
    DropdownMenuItem(
        text = { Text(text) },
        leadingIcon = { Icon(imageVector = icon, contentDescription = null) },
        enabled = enabled,
        onClick = onClick,
        modifier = Modifier.testTag(tag)
    )
}

/** The space between a lifted row and its menu. */
private val MENU_GAP = 8.dp
