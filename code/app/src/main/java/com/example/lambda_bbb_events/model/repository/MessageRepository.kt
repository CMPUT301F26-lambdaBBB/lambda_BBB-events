package com.example.lambda_bbb_events.model.repository

import androidx.compose.runtime.mutableStateListOf
import com.example.lambda_bbb_events.model.Message
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

/*
 * Manages message data and synchronizes it with Firestore.
 */

/**
 *  Handle the data in the Firestore database for the [Message] class.
 */
class MessageRepository {
    private val db = Firebase.firestore
    private val messagesRef = db.collection("messages")

    private val _messages = mutableStateListOf<Message>()

    init{
        messagesRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                return@addSnapshotListener
            }

            _messages.clear()

            snapshot?.documents?.forEach { document ->
                val message = document.toObject(Message::class.java)
                if (message != null) {
                    _messages.add(message)
                }
            }
        }
    }

    /** Cached messages synchronized with Firestore. */
    val messages : List<Message>
        get() = _messages

    /**
     * Adds a new message to the Firestore database.
     *
     * @param message The message to add.
     */
    fun addMessage(message: Message) {
        messagesRef.document(message.messageId).set(message)
    }

    /**
     * Deletes a message from the Firestore database.
     *
     * @param message The message to be deleted.
     */
    fun deleteMessage(message: Message) {
        messagesRef.document(message.messageId).delete()
    }


    /**
     * Saves a message to Firestore, creating it if absent.
     *
     * @param updatedMessage The message data to save.
     */
    fun updateMessage(updatedMessage: Message) {
        messagesRef.document(updatedMessage.messageId).set(updatedMessage)
    }
}