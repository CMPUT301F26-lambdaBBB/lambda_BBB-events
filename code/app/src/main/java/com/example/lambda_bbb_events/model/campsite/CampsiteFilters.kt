package com.example.lambda_bbb_events.model.campsite

import com.example.lambda_bbb_events.model.lottery.Lottery
import com.example.lambda_bbb_events.model.lottery.LotteryStatus
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date

/**
 * The filters applied when browsing campsites. By default, nothing is filtered.
 *
 * @property startDate The first day the campsite must be available, or `null` for any dates.
 * @property endDate The last day the campsite must be available. `null` for only [startDate].
 * @property location Text the campsite's address or name must contain. Empty for anywhere.
 * @property starRating The lowest average rating, or `null` for any rating.
 * @property guestRange The numbers of guests the campsite can allow, or `null` for any number.
 */
data class CampsiteFilters(
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val location: String = "",
    val starRating: Double? = null,
    val guestRange: IntRange? = null
)

/**
 * Checks if a campsite is available from [start] to [end]: entrants can still sign up for
 * one of its lotteries, and that lottery's booking period covers every one of those days,
 * so a winner could book them.
 *
 * @param lotteries The campsite's lotteries.
 * @param start The first day.
 * @param end The last day.
 * @param today The current day, which decides whether signing up has closed.
 * @return `true` if one of [lotteries] makes the days available, `false` otherwise.
 */
fun isAvailableThroughLottery(
    lotteries: List<Lottery>,
    start: LocalDate,
    end: LocalDate,
    today: LocalDate = LocalDate.now()
): Boolean = lotteries.any { it.acceptsSignUps(today) && it.bookingCovers(start, end) }

/**
 * Whether entrants can sign up now or once registration starts: the lottery is open, every
 * owner has accepted it, its waitlist has room, and registration has not ended.
 */
private fun Lottery.acceptsSignUps(today: LocalDate): Boolean =
    status == LotteryStatus.OPEN && pendingOwnerIds.isEmpty() && !isFull() &&
            !today.isAfter(registrationEnd.toLocalDate())

/** Whether every day from [start] to [end] is inside the booking period. */
private fun Lottery.bookingCovers(start: LocalDate, end: LocalDate): Boolean =
    !start.isBefore(bookingStart.toLocalDate()) && !end.isAfter(bookingEnd.toLocalDate())

/** The day this moment falls on in the phone's time zone. */
private fun Date.toLocalDate(): LocalDate = toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
