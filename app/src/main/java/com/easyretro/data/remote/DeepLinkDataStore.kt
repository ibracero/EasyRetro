package com.easyretro.data.remote

import android.net.Uri
import arrow.core.Either
import com.easyretro.domain.model.Failure
import com.google.firebase.dynamiclinks.DynamicLink
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class DeepLinkDataStore() {

    companion object {
        private const val DEEPLINK_DOMAIN = "https://easyretro.page.link"
    }

    suspend fun generateDeepLink(link: String): Either<Failure, String> {
        return suspendCoroutine { continuation ->
            FirebaseDynamicLinks.getInstance().createDynamicLink()
                .setLink(Uri.parse(link))
                .setDomainUriPrefix(DEEPLINK_DOMAIN)
                .setAndroidParameters(DynamicLink.AndroidParameters.Builder().build())
                .buildShortDynamicLink()
                .addOnSuccessListener { shortDynamicLink ->
                    val shortLink = shortDynamicLink.shortLink
                    if (shortLink == null) continuation.resume(Either.left(Failure.UnknownError))
                    else continuation.resume(Either.right(shortLink.toString()))
                }
                .addOnFailureListener {
                    continuation.resume(Either.left(Failure.UnknownError))
                }
        }
    }
}