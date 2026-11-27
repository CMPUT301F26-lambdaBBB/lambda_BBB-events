package com.example.lambda_bbb_events.view.ui.components.common

import android.widget.Toast
import android.content.Context

/*
* Quick function for toast pop-up that has the ability to update current toast on screen as well
*/

/** Toast currently displayed on screen, defaults to null*/
private var currentToast: Toast? = null

/**
 * Displays a toast that can update and already occurring toast on screen.
 * Allows for quicker and more efficient use of Toasts
 *
 * @param context Current application context
 * @param message Display text to show on toast
 * @param duration Duration that the toast will be on screen. [Toast.LENGTH_SHORT] or [Toast.LENGTH_LONG]
 * Defaults to [Toast.LENGTH_SHORT]
 */

fun quickToast(context: Context,message: String, duration: Int = Toast.LENGTH_SHORT){
    currentToast?.cancel() // cancel currently displaying toast if applicable for most recent
    val toast = Toast.makeText(context.applicationContext,message, duration)
    toast.show()
    currentToast = toast
}
