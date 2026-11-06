
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/*
 * Defines a reusable range slider for selecting minimum and maximum numerical values.
 */

/**
 * Displays a range slider for numbers from a minimum to maximum range
 *
 * Users can drag the nodes at the start and the end to adjust the range
 *
 * @param value The currently selected range.
 * @param onValueChange Callback invoked when the range changes.
 * @param min The minimum selectable value.
 * @param max The maximum selectable value.
 * @param modifier Modifier used to customize the component layout.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RangePicker(
    value: ClosedFloatingPointRange<Float>,
    onValueChange: (ClosedFloatingPointRange<Float>) -> Unit,
    min: Float,
    max: Float,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Range")
            Text("${value.start.toInt()}-${value.endInclusive.toInt()}")
        }

        RangeSlider(
            value = value,
            onValueChange = onValueChange,
            valueRange = min..max,
            modifier = Modifier.fillMaxWidth(),

            colors = SliderDefaults.colors(
                activeTrackColor = Color.LightGray,
                inactiveTrackColor = Color.LightGray
            ),

            startThumb = {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .background(Color.DarkGray, CircleShape)
                )
            },

            endThumb = {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .background(Color.DarkGray, CircleShape)
                )
            }
        )
    }
}
