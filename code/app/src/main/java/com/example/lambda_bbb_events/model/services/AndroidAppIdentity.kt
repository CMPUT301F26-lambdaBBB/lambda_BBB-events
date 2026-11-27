package com.example.lambda_bbb_events.model.services

import android.content.Context
import android.content.pm.PackageManager
import android.content.pm.Signature
import android.os.Build
import java.security.MessageDigest

/*
 * Identifies the app to Google web services. The Maps key only accepts requests from this app,
 * and the Maps SDK proves that by itself, but requests the app sends itself, such as place
 * searches, must say which app they are from.
 *
 * Reference:
 * https://cloud.google.com/docs/authentication/api-keys#adding-application-restrictions
 */

/**
 * The app's package name and signing certificate, as Google's Android key restriction checks them.
 *
 * @property packageName The app's package name, sent as `X-Android-Package`.
 * @property certificateSha1 The SHA-1 of the app's signing certificate in upper-case hex without
 * colons, sent as `X-Android-Cert`.
 */
data class AndroidAppIdentity(val packageName: String, val certificateSha1: String) {

    companion object {
        /**
         * Reads the identity of the running app.
         *
         * @return The identity, or `null` if the app's signature cannot be read.
         */
        fun of(context: Context): AndroidAppIdentity? {
            val signature = signingCertificate(context) ?: return null
            return AndroidAppIdentity(context.packageName, sha1Hex(signature.toByteArray()))
        }

        /** The certificate the running app is signed with, or `null` if it cannot be read. */
        private fun signingCertificate(context: Context): Signature? {
            val packageManager = context.packageManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val info = packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.GET_SIGNING_CERTIFICATES
                )
                return info.signingInfo?.apkContentsSigners?.firstOrNull()
            }
            @Suppress("DEPRECATION")
            val info = packageManager.getPackageInfo(context.packageName, PackageManager.GET_SIGNATURES)
            @Suppress("DEPRECATION")
            return info.signatures?.firstOrNull()
        }

        /** Formats the SHA-1 of [bytes] the way Google expects it. */
        internal fun sha1Hex(bytes: ByteArray): String =
            MessageDigest.getInstance("SHA-1").digest(bytes).joinToString("") { "%02X".format(it) }
    }
}
