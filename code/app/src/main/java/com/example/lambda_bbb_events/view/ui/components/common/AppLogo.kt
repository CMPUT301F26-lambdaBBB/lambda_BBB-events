package com.example.lambda_bbb_events.view.ui.components.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.lambda_bbb_events.R

/**
 * Displays the Lambda Camping logo: a tent shaped like the letter lambda under a night sky.
 *
 * @param modifier Customizes the logo's layout.
 * @param size The width and height of the logo.
 */
@Composable
fun AppLogo(
    modifier: Modifier = Modifier,
    size: Dp = 120.dp
) {
    Image(
        painter = painterResource(R.drawable.lambda_logo),
        contentDescription = "Lambda Camping logo",
        modifier = modifier
            .size(size)
            .testTag("appLogo")
    )
}
