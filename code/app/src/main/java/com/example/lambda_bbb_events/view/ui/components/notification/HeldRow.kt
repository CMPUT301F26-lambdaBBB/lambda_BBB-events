package com.example.lambda_bbb_events.view.ui.components.notification

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.round
import androidx.compose.ui.unit.roundToIntSize

/*
 * Makes a row that is pressed and held stand out from the rest of the screen.
 */

/** Remembers which row is held, so that [HeldRowBox] can lift it. */
@Stable
class HeldRowState {
    /** Whether a row is held now. */
    var isHeld by mutableStateOf(false)
        private set

    /** The last held row. Kept after it is released, so it can be lowered again. */
    internal var row by mutableStateOf<Row?>(null)
        private set

    internal fun hold(row: Row) {
        this.row = row
        isHeld = true
    }

    internal fun release() {
        isHeld = false
    }

    /**
     * A row that can be lifted.
     *
     * @property layer What the row looks like.
     */
    internal class Row(val layer: GraphicsLayer) {
        /** Where the row is. */
        var coordinates: LayoutCoordinates? = null

        /** Gets the row's position inside [box], or `null` if the row is not on the screen. */
        fun boundsIn(box: LayoutCoordinates?): Rect? {
            val row = coordinates?.takeIf { it.isAttached } ?: return null
            return box?.takeIf { it.isAttached }?.localBoundingBoxOf(row, clipBounds = false)
        }
    }
}

/** Records what a row looks like and where it is, so [HeldRowBox] can draw it lifted. */
internal fun Modifier.recordRow(row: HeldRowState.Row): Modifier = this
    .onGloballyPositioned { row.coordinates = it }
    .drawWithContent {
        row.layer.record { this@drawWithContent.drawContent() }
        drawLayer(row.layer)
    }

/**
 * Displays a screen. While one of its rows is held, the screen is dimmed and the row is
 * lifted above it.
 *
 * @param state The row that is held.
 * @param modifier Modifier used to customize the layout.
 * @param content The screen.
 */
@Composable
fun HeldRowBox(
    state: HeldRowState,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val progress by animateFloatAsState(
        targetValue = if (state.isHeld) 1f else 0f,
        animationSpec = tween(LIFT_MILLIS),
        label = "lift"
    )
    val cardLayer = rememberGraphicsLayer()
    val cardColor = MaterialTheme.colorScheme.surface
    val coordinates = remember { arrayOfNulls<LayoutCoordinates>(1) }

    Box(
        modifier = modifier
            .onGloballyPositioned { coordinates[0] = it }
            .drawWithContent {
                drawContent()

                val row = state.row
                val bounds = row?.boundsIn(coordinates[0])
                if (progress == 0f || row == null || bounds == null) {
                    return@drawWithContent
                }
                drawRect(Color.Black, alpha = MAX_DIM * progress)

                val corner = CORNER.toPx() * progress
                cardLayer.record(size = bounds.size.roundToIntSize()) {
                    drawRect(cardColor)
                    drawLayer(row.layer)
                }
                cardLayer.apply {
                    topLeft = bounds.topLeft.round()
                    setRoundRectOutline(Offset.Zero, Size.Unspecified, corner)
                    clip = true
                    shadowElevation = ELEVATION.toPx() * progress
                    scaleX = 1f + EXTRA_SCALE * progress
                    scaleY = 1f + EXTRA_SCALE * progress
                }
                drawLayer(cardLayer)
            }
    ) {
        content()
    }
}

private const val LIFT_MILLIS = 180
private const val MAX_DIM = 0.32f
private const val EXTRA_SCALE = 0.03f
private val CORNER = 16.dp
private val ELEVATION = 8.dp
