package com.tacktikcwin.app

import com.google.firebase.database.FirebaseDatabase

object FirebaseWebUrlChecker {
    private const val DATABASE_URL =
        "https://tacktick-b8493-default-rtdb.europe-west1.firebasedatabase.app"
    private const val URL_NODE = "url"

    fun checkUrl(onUrlFound: (String) -> Unit) {
        try {
            FirebaseDatabase
                .getInstance(DATABASE_URL)
                .reference
                .child(URL_NODE)
                .get()
                .addOnSuccessListener { snapshot ->
                    val url = snapshot.getValue(String::class.java)?.trim().orEmpty()
                    if (WebUrlStore.isWebUrl(url)) {
                        onUrlFound(url)
                    }
                }
                .addOnFailureListener {
                    // Firebase is optional at runtime: keep the native app on any failure.
                }
        } catch (_: Throwable) {
            // Missing or invalid Firebase configuration must not block the native app.
        }
    }
}
