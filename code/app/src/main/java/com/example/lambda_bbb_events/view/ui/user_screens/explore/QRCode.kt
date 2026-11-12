package com.example.lambda_bbb_events.view.ui.user_screens.explore

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.example.lambda_bbb_events.controller.AppControllers
import com.example.lambda_bbb_events.model.generateQR
import com.example.lambda_bbb_events.view.ui.components.common.ScreenTitle

/**
 * Displays a full-screen view for a campsite's QR Code.
 *
 * @param navController The [NavController] used for navigation.
 * @param controllers The [AppControllers] used to fetch campsite details.
 * @param campsiteId The ID of the campsite to display the QR code for.
 */
@Composable
fun QRCodeScreen(
    navController: NavController,
    controllers: AppControllers,
    campsiteId: String
) {
    val campsite = controllers.campsites.getCampsite(campsiteId)
    val qrBitmap = remember(campsiteId) {
        generateQR(campsiteId, 800, 800)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ScreenTitle(
            title = "Campsite QR Code",
            navController = navController,
            showBack = true
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = campsite?.campsiteName ?: "Campsite QR Code",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Scan this QR code with another device to open this campsite's details page.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Box(
                modifier = Modifier.padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                if (qrBitmap != null) {
                    Image(
                        bitmap = qrBitmap.asImageBitmap(),
                        contentDescription = "QR Code for ${campsite?.campsiteName}",
                        modifier = Modifier.size(260.dp)
                    )
                } else {
                    Text(
                        text = "Unable to generate QR Code",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { navController.popBackStack() },
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .height(48.dp),
            shape = RoundedCornerShape(24.dp)
        ) {
            Text("Close")
        }
    }
}

/**
 * Displays a popup dialog showing the enlarged QR code for a campsite.
 *
 * @param campsiteName The name of the campsite.
 * @param campsiteId The ID of the campsite.
 * @param onDismiss Callback invoked when the dialog is dismissed.
 */
@Composable
fun QRCodeDialog(
    campsiteName: String,
    campsiteId: String,
    onDismiss: () -> Unit
) {
    val qrBitmap = remember(campsiteId) {
        generateQR(campsiteId, 800, 800)
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = campsiteName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Scan to view campsite details",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Box(
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (qrBitmap != null) {
                        Image(
                            bitmap = qrBitmap.asImageBitmap(),
                            contentDescription = "QR Code for $campsiteName",
                            modifier = Modifier.size(220.dp)
                        )
                    } else {
                        Text(
                            text = "Unable to generate QR Code",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(0.8f)
                ) {
                    Text("Close")
                }
            }
        }
    }
}
