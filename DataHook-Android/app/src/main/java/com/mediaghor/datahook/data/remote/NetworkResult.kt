package com.mediaghor.datahook.data.remote

import kotlinx.coroutines.CancellationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

/**
 * Runs a suspending API [block] and wraps the outcome in a [Result], converting
 * exceptions into user-friendly messages. Cancellation is re-thrown so coroutine
 * scoping keeps working.
 */
suspend fun <T> apiCall(block: suspend () -> T): Result<T> = try {
    Result.success(block())
} catch (e: CancellationException) {
    throw e
} catch (e: HttpException) {
    Result.failure(ApiException(e.userMessage(), e.code()))
} catch (e: IOException) {
    Result.failure(ApiException("Network error. Check your connection and try again."))
} catch (e: Exception) {
    Result.failure(ApiException(e.message ?: "Something went wrong."))
}

class ApiException(
    override val message: String,
    val code: Int? = null,
) : Exception(message)

/** Turns a `Response<Unit>` result into a plain `Result<Unit>`, failing on non-2xx. */
fun Result<Response<Unit>>.unwrapUnit(): Result<Unit> = mapCatching { resp ->
    if (!resp.isSuccessful) throw ApiException("Request failed (${resp.code()}).", resp.code())
    Unit
}

private val errorJson = Json { ignoreUnknownKeys = true; isLenient = true }

/** Extracts a readable message from a DRF error body when possible. */
private fun HttpException.userMessage(): String {
    val raw = try {
        response()?.errorBody()?.string()
    } catch (_: Exception) {
        null
    }

    val parsed = raw?.let { body ->
        runCatching {
            when (val el = errorJson.parseToJsonElement(body)) {
                is JsonObject -> firstMessage(el)
                is JsonArray -> el.firstStringOrNull()
                is JsonPrimitive -> el.contentOrNull()
                else -> null
            }
        }.getOrNull()
    }

    return parsed ?: when (code()) {
        400 -> "Invalid request. Please check your input."
        401 -> "Session expired. Please sign in again."
        403 -> "You don't have permission to do that."
        404 -> "Not found."
        in 500..599 -> "Server error. Please try again later."
        else -> "Request failed (${code()})."
    }
}

private fun firstMessage(obj: JsonObject): String? {
    // Prefer common DRF keys, then fall back to the first field error.
    for (key in listOf("detail", "error", "message", "non_field_errors")) {
        obj[key]?.let { el ->
            when (el) {
                is JsonArray -> el.firstStringOrNull()
                is JsonPrimitive -> el.contentOrNull()
                else -> null
            }?.let { return it }
        }
    }
    for ((field, value) in obj) {
        val msg = when (value) {
            is JsonArray -> value.firstStringOrNull()
            is JsonPrimitive -> value.contentOrNull()
            else -> null
        }
        if (msg != null) return if (field.isBlank()) msg else "$field: $msg"
    }
    return null
}

private fun JsonArray.firstStringOrNull(): String? =
    (firstOrNull() as? JsonPrimitive)?.contentOrNull()

private fun JsonPrimitive.contentOrNull(): String? = content.ifBlank { null }
