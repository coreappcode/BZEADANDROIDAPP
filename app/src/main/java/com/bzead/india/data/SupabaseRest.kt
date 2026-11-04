package com.bzead.india.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

data class RestResult(
    val ok: Boolean,
    val code: Int,
    val rows: JSONArray = JSONArray(),
    val error: String? = null
)

/** PostgREST client that runs requests with the signed-in user's access token (RLS applies). */
internal object SupabaseRest {

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private val json = "application/json".toMediaType()

    fun enc(v: String): String = URLEncoder.encode(v, "UTF-8")

    /** PostgREST `in.(...)` list with quoted values. */
    fun inList(values: Collection<String>): String =
        values.joinToString(",", "(", ")") { "\"" + it.replace("\"", "\\\"") + "\"" }

    suspend fun request(
        method: String,
        path: String,
        token: String,
        body: String? = null,
        prefer: String? = null,
        range: IntRange? = null
    ): RestResult = withContext(Dispatchers.IO) {
        try {
            val builder = Request.Builder()
                .url("${SupabaseClient.BASE_URL}/$path")
                .header("apikey", SupabaseClient.ANON_KEY)
                .header("Authorization", "Bearer $token")
                .header("Accept", "application/json")
            if (prefer != null) builder.header("Prefer", prefer)
            if (range != null) {
                builder.header("Range-Unit", "items")
                builder.header("Range", "${range.first}-${range.last}")
            }
            val payload = body?.toRequestBody(json)
            builder.method(method, payload ?: if (method == "GET" || method == "DELETE") null else "".toRequestBody(json))
            client.newCall(builder.build()).execute().use { res ->
                val text = res.body?.string().orEmpty()
                if (res.isSuccessful) {
                    val rows = when {
                        text.isBlank() -> JSONArray()
                        text.trimStart().startsWith("[") -> JSONArray(text)
                        else -> JSONArray().put(JSONObject(text))
                    }
                    RestResult(true, res.code, rows)
                } else {
                    val msg = runCatching { JSONObject(text).optString("message") }.getOrNull()
                    RestResult(false, res.code, error = msg?.takeIf { it.isNotBlank() } ?: "Request failed (${res.code})")
                }
            }
        } catch (e: Exception) {
            RestResult(false, -1, error = "Network error. Please check your connection and try again.")
        }
    }
}
