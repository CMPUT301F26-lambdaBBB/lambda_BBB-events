package com.example.lambda_bbb_events.model.message

/**
 * Defines the supported conversation types.
 */
enum class ConversationType {

    /**
     * Private conversation between a camper and
     * a campsite owner.
     */
    OWNER,

    /**
     * Private conversation between a camper and
     * one specific administrator.
     */
    ADMIN
}
