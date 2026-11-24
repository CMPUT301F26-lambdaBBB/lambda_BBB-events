import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.RangeSliderState
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.lambda_bbb_events.view.ui.components.common.closestStops
import kotlin.math.roundToInt

/*
 * Defines a reusable range slider for selecting
 * minimum and maximum numerical values.
 */

/**
 * Displays a range slider for choosing from a list of numbers.
 *
 * Users can drag the nodes at the start and end to adjust the range. They only stop at the
 * numbers in [stops], which are evenly spaced however far apart the numbers are.
 *
 * @param value The currently selected range. Ends that are not stops show as the closest one.
 * @param onValueChange Callback invoked when the range changes.
 * @param stops The selectable values, smallest first. Must have at least two, all different.
 * @param modifier Modifier used to customize the component layout.
 */
@Composable
fun RangePicker(
    value: IntRange,
    onValueChange: (IntRange) -> Unit,
    stops: List<Int>,
    modifier: Modifier = Modifier
) {
    // The slider moves between positions in the list, which keeps the stops evenly spaced
    val shown = closestStops(value, stops)
    val start = stops.indexOf(shown.first)
    val end = stops.indexOf(shown.last)

    Column(
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Range",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "${shown.first}-${shown.last}",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }

        val sliderState = remember(stops) {
            RangeSliderState(
                startValue = start.toFloat(),
                endValue = end.toFloat(),
                steps = stops.size - 2, // the stops between the two ends
                trackRange = 0f..stops.lastIndex.toFloat()
            )
        }

        // Keep the slider in sync when the range is changed from outside, such as a reset
        LaunchedEffect(sliderState, start, end) {
            sliderState.startValue = start.toFloat()
            sliderState.endValue = end.toFloat()
        }

        RangeSlider(
            state = sliderState,
            onValueChange = { dragged ->
                val newStart = dragged.start.roundToInt().coerceIn(stops.indices)
                val newEnd = dragged.endInclusive.roundToInt().coerceIn(stops.indices)
                // Dragging calls this constantly, but the stop rarely changes
                if (newStart != start || newEnd != end) {
                    onValueChange(stops[newStart]..stops[newEnd])
                }
            },
            modifier = Modifier.fillMaxWidth(),

            colors = SliderDefaults.colors(
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                activeTickColor = MaterialTheme.colorScheme.onPrimary,
                inactiveTickColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),

            startThumb = {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primary,
                            shape = CircleShape
                        )
                )
            },

            endThumb = {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primary,
                            shape = CircleShape
                        )
                )
            }
        )
    }
}
