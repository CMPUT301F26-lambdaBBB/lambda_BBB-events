package com.example.lambda_bbb_events.model.repository

import android.util.Log
import com.example.lambda_bbb_events.model.review.Review
import com.google.firebase.Firebase
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Transaction
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await
import kotlin.math.roundToInt

/*
 * Manages campsite reviews in Firestore.
 */

/**
 * Handles the data in the Firestore database for the [Review] class.
 *
 * Campsites store their average rating and number of reviews, so every change to a review also
 * updates its campsite. Both are written in one transaction, so they always agree, even when
 * several users review a campsite at the same time.
 */
class ReviewRepository(
    db: FirebaseFirestore = Firebase.firestore
) : Repository<Review>("reviews", db) {

    init {
        startListening()
    }

    override fun idOf(item: Review) = item.reviewId

    override fun fromDocument(document: DocumentSnapshot): Review? =
        document.toObject(Review::class.java)?.takeIf { it.isValid() }

    override fun onListenError(error: FirebaseFirestoreException) {
        Log.e("ReviewRepository", "listener error", error)
    }

    /**
     * Saves a review, replacing the user's earlier review of the campsite if there is one, and
     * updates the campsite's rating.
     *
     * @param item The review to save.
     * @throws IllegalArgumentException if the review is not valid.
     * @throws IllegalStateException if the campsite does not exist.
     */
    override suspend fun add(item: Review) {
        require(item.isValid()) { "The review is not valid" }
        val reviewRef = collectionRef.document(item.reviewId)
        val campsiteRef = db.collection("campsites").document(item.campsiteId)

        db.runTransaction { transaction ->
            val oldRating = ratingOf(transaction.get(reviewRef))
            val campsite = transaction.get(campsiteRef)
            check(campsite.exists()) { "The campsite does not exist" }

            transaction.set(reviewRef, item)
            updateCampsiteRating(transaction, campsiteRef, campsite, oldRating, item.rating)
        }.await()
        putInCache(item)
    }

    /**
     * Deletes a review and takes it out of its campsite's rating. Does nothing if the review
     * does not exist.
     *
     * @param id The ID of the review to delete.
     */
    override suspend fun delete(id: String) {
        val reviewRef = collectionRef.document(id)

        db.runTransaction { transaction ->
            val review = transaction.get(reviewRef)
            val campsiteId = review.getString("campsiteId")
            val rating = ratingOf(review)

            if (rating != null && !campsiteId.isNullOrBlank()) {
                val campsiteRef = db.collection("campsites").document(campsiteId)
                val campsite = transaction.get(campsiteRef)
                if (campsite.exists()) {
                    updateCampsiteRating(transaction, campsiteRef, campsite, rating, null)
                }
            }
            transaction.delete(reviewRef)
        }.await()
        removeFromCache(id)
    }

    /** Gets the stars of a stored review, or `null` if there is no review. */
    private fun ratingOf(review: DocumentSnapshot): Int? = review.getLong("rating")?.toInt()

    /**
     * Updates a campsite's average rating and number of reviews after one review changed.
     *
     * @param oldRating The stars the review had before, or `null` if it is new.
     * @param newRating The stars the review has now, or `null` if it was deleted.
     */
    private fun updateCampsiteRating(
        transaction: Transaction,
        campsiteRef: DocumentReference,
        campsite: DocumentSnapshot,
        oldRating: Int?,
        newRating: Int?
    ) {
        var count = (campsite.getLong("ratingCount") ?: 0L).toInt()
        // Stars are whole numbers, so rounding gives the exact total back from the average
        var total = ((campsite.getDouble("rating") ?: 0.0) * count).roundToInt()

        if (oldRating != null) {
            count--
            total -= oldRating
        }
        if (newRating != null) {
            count++
            total += newRating
        }

        val average = if (count > 0) total.toDouble() / count else 0.0
        transaction.update(
            campsiteRef,
            "rating", average,
            "ratingCount", maxOf(count, 0)
        )
    }

    /**
     * Gets a user's review of a campsite from the cache.
     *
     * @param campsiteId The ID of the campsite.
     * @param reviewerId The device ID of the user.
     * @return The review, or `null` if the user has not reviewed the campsite.
     */
    fun getReview(campsiteId: String, reviewerId: String): Review? =
        getCached(Review.idFor(campsiteId, reviewerId))

    /**
     * Gets every review of a campsite from the cache.
     *
     * @param campsiteId The ID of the campsite.
     * @return The reviews of the campsite, in no particular order.
     */
    fun getReviewsForCampsite(campsiteId: String): List<Review> =
        cached.filter { it.campsiteId == campsiteId }
}
