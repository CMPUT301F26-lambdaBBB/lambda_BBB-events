package com.example.lambda_bbb_events.model

data class Image (
    val imageId: String,
    val uploaderUserId: String,
    val url: String           // will contain firebase storage download URL instead of file, i think firestore cant store image files
)