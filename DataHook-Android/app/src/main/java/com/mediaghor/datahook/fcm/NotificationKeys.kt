package com.mediaghor.datahook.fcm

object NotificationKeys {
    /** Intent extra carrying the endpoint id a tapped notification should open. */
    const val EXTRA_ENDPOINT_ID = "dh_endpoint_id"

    /** Intent extra carrying a Base64-encoded submission, opening it directly. */
    const val EXTRA_SUBMISSION_PAYLOAD = "dh_submission_payload"
}
