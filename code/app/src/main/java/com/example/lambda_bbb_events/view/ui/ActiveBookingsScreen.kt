package com.example.lambda_bbb_events.view.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Hiking
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.lambda_bbb_events.model.Reservation
import com.example.lambda_bbb_events.model.repository.CampsiteRepository
import com.example.lambda_bbb_events.view.ui.components.BottomNavigationBar
import com.example.lambda_bbb_events.view.ui.components.ScreenTitle
import com.example.lambda_bbb_events.view.ui.components.SubTitle
import com.example.lambda_bbb_events.ui.theme.BookingsTypeBar


/**
 * Displays the user's active campsite reservations.
 *
 * Provides navigation between active, past, and lottery
 * reservations, along with the application's main screens.
 *
 * @param navController Handles screen navigation.
 */
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.lambda_bbb_events.controller.AppControllers
import com.example.lambda_bbb_events.model.repository.ReservationRepository
import com.example.lambda_bbb_events.view.ui.components.BottomNavigationBar
import com.example.lambda_bbb_events.view.ui.components.ScreenTitle
import com.example.lambda_bbb_events.view.ui.components.SubTitle
import com.example.lambda_bbb_events.ui.theme.BookingsTypeBar
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Hiking
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.ui.unit.sp
import com.example.lambda_bbb_events.model.ReservationStatus

@Composable
fun ActiveBookingsScreen(navController: NavController, controllers: AppControllers) {
    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                navController
            )
        }
    ) { innerPadding ->


        val reservationRepo = ReservationRepository()
//        val res_all = reservationRepo.reservations  // res_all is empty
        val reservations: List<Reservation> = reservationRepo.getReservationsForEntrant( controllers.users.user.deviceId)


        val res1 = Reservation(
            "111",
            "035a2a44-30ee-42bc-b6e8-3eea3ba0e203",
            controllers.users.user.deviceId,
            "466a08d621173733",
            "2026-12-01",
            "2026-12-03",
            2,
            ReservationStatus.PENDING,
            true
        )
        val res2 = Reservation(
            "112",
            "035a2a44-30ee-42bc-b6e8-3eea3ba0e203",
            controllers.users.user.deviceId,
            "466a08d621173733",
            "2027-2-01",
            "2026-2-03",
            3,
            ReservationStatus.PENDING,
            true
        )
        val res3 = Reservation(
            "112",
            "2811678e-d2c4-4d8e-8b6a-52b131f8f817",
            controllers.users.user.deviceId,
            "466a08d621173733",
            "2027-2-01",
            "2026-2-03",
            3,
            ReservationStatus.PENDING,
            true
        )

        val res_all = listOf<Reservation>(res1, res2, res3)


        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding).testTag("screen_bookings")
        ) {
            ScreenTitle(
                title = "My Reservations",
                navController = navController
            )
            BookingsTypeBar(navController)
            Spacer(modifier = Modifier.height(25.dp))
            SubTitle(title = "Active Reservations")
            Spacer(modifier = Modifier.height(25.dp))


            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {


                items(res_all.size) { index ->
//                    Text(text = "Testing index $index")
                    VerticalCampsiteCard(modifier = Modifier.fillMaxWidth(), reservation = res_all[index])
                }
            }
        }
    }
}



@Composable
fun VerticalCampsiteCard(modifier: Modifier, reservation: Reservation){



    val campsite = CampsiteRepository().get(reservation.campsiteId)



    val campsiteName = campsite?.campsiteName ?: "Unknown Name"
    val rating = campsite?.rating ?: 0



    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 4.dp),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, Color.LightGray),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            // top row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = campsiteName,
                    fontSize = 18.sp,
                    modifier = Modifier.weight(1f),
                    color = Color.Black
                )

                // stars
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (index in 1.. 5) {
                        Icon(
                            imageVector = if (index < rating.toInt() ) {
                                Icons.Default.Star
                            } else {
                                Icons.Default.StarBorder
                            },
                            contentDescription = null,
                            tint = Color(0xFFFFC928),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = rating.toString(),
                        fontSize = 10.sp,
                        color = Color.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            HorizontalDivider(
                thickness = 3.dp,
                color = Color(0xFFE8DDF8)
            )

            Spacer(modifier = Modifier.height(4.dp))


            // lower row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {

                // image
                Box(
                    modifier = Modifier
                        .size(92.dp)
                        .background(Color(0xFFE0E0E0)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = "Campsite image",
                        tint = Color.LightGray,
                        modifier = Modifier.size(48.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // RESERVATION DETAILS
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    DetailRow(Icons.Default.Check, "Confirmed")
                    DetailRow(Icons.Default.LocationOn, campsite?.address ?: "Unknown Address")
                    DetailRow(Icons.Default.Home, "Tent")
                    DetailRow(Icons.Default.Hiking, "${campsite?.latitude}, ${campsite?.longitude}")
                    DetailRow(Icons.Default.CalendarToday, reservation?.startDate ?: "Unknown Date")
                }

                // RIGHT ARROW
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "View reservation",
                    tint = Color.Black
                )
            }
        }
    }
}



@Composable
fun DetailRow(
    icon: ImageVector,
    text: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(12.dp),
            tint = Color.Black
        )

        Spacer(modifier = Modifier.width(6.dp))

        Text(
            text = text,
            fontSize = 10.sp,
            color = Color.Black
        )
    }
}
