package com.example.lambda_bbb_events.model.repository

import com.example.lambda_bbb_events.model.message.Message
import com.google.firebase.Firebase
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore

/*
 * Manages message data and synchronizes it with Firestore.
 */

/**
 *  Handle the data in the Firestore database for the [Message] class.
 */
class MessageRepository(
    db: FirebaseFirestore = Firebase.firestore
) : Repository<Message>("messages", db) {

    init {
        startListening()
    }

    override fun idOf(item: Message) = item.messageId

    override fun fromDocument(document: DocumentSnapshot): Message? =
        document.toObject(Message::class.java)

    /** Cached messages synchronized with Firestore. */
    val messages: List<Message>
        get() = cached

    /**
     * Adds a new message to the Firestore database.
     *
     * @param message The message to add.
     */
    suspend fun addMessage(message: Message) = add(message)

    /**
     * Deletes a message from the Firestore database.
     *
     * @param message The message to be deleted.
     */
    suspend fun deleteMessage(message: Message) = delete(message)


    /**
     * Saves a message to Firestore, creating it if absent.
     *
     * @param updatedMessage The message data to save.
     */
    suspend fun updateMessage(updatedMessage: Message) = update(updatedMessage)
}
