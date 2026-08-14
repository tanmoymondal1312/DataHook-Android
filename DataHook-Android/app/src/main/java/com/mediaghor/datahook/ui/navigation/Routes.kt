package com.mediaghor.datahook.ui.navigation

object Routes {
    const val AUTH = "auth"
    const val DASHBOARD = "dashboard" // home = cross-endpoint submission feed
    const val ENDPOINTS = "endpoints"
    const val CREATE_ENDPOINT = "create_endpoint"
    const val SETTINGS = "settings"

    const val ARG_ENDPOINT_ID = "endpointId"
    const val ARG_TAB = "tab"
    const val ENDPOINT_DETAIL = "endpoint/{$ARG_ENDPOINT_ID}?$ARG_TAB={$ARG_TAB}"

    fun endpointDetail(id: Int, tab: Int = 0): String = "endpoint/$id?$ARG_TAB=$tab"

    /** Tab index for the Submissions section (used by notification deep links). */
    const val TAB_SUBMISSIONS = 3

    const val ARG_SUBMISSION = "payload"
    const val SUBMISSION_DETAIL = "submission/{$ARG_SUBMISSION}"

    fun submissionDetail(encodedPayload: String): String = "submission/$encodedPayload"
}
