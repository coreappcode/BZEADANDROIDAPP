package com.bzead.india.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit

data class DetectedLocation(
    val place: String = "",
    val city: String = "",
    val state: String = "",
    val country: String = "",
    val countryCode: String = "",
    val postalCode: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val provider: String = ""
)

/** Result of the web `useDeliveryEstimate` check; `serviceable == null` means not checked. */
data class DeliveryResult(
    val serviceable: Boolean? = null,
    val expectedDeliveryDate: String = "",
    val error: String? = null
)

/** Port of the web `useDeliveryEstimate.runCheck`, `tatService.estimateCarrierTat` and `locationService` reverse geocode / IP detection. */
internal object DeliveryEstimateApi {

    private const val CARRIER_DOMESTIC_DAYS = 5
    private const val CARRIER_INTERNATIONAL_DAYS = 10
    private val INDIA_TOKENS = setOf("IN", "IND", "INDIA")

    private val http = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .build()

    private fun iso(country: String): String {
        val t = country.trim().uppercase()
        return if (t in INDIA_TOKENS) "IN" else t
    }

    private fun carrierEstimate(domestic: Boolean): String {
        val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        fmt.timeZone = TimeZone.getTimeZone("UTC")
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_MONTH, if (domestic) CARRIER_DOMESTIC_DAYS else CARRIER_INTERNATIONAL_DAYS)
        return fmt.format(cal.time)
    }

    private fun looksIndianPin(pin: String): Boolean = Regex("^[1-9][0-9]{5}$").matches(pin.replace(" ", ""))

    suspend fun check(product: Product, pin: String, buyerCountry: String, token: String): DeliveryResult {
        return try {
            val buyerIso = iso(buyerCountry)
            val originIso = iso(product.originCountry)
            val originIsIndia = originIso == "IN"
            val buyerIsIndia = buyerIso == "IN"
            val buyerKnown = buyerIso.isNotEmpty()
            val domesticLane = originIsIndia && (if (buyerKnown) buyerIsIndia else looksIndianPin(pin))
            when {
                domesticLane -> {
                    val ok = CheckoutApi.isServiceable(pin, product.sellerId, token)
                    if (!ok) DeliveryResult(serviceable = false)
                    else DeliveryResult(true, CheckoutApi.fetchTat(listOf(product.id), pin, token).maxExpectedDate)
                }
                originIsIndia -> {
                    // India origin to a non-India buyer: BZEAD.IN does not ship internationally.
                    DeliveryResult(serviceable = false)
                }
                buyerIso == originIso -> DeliveryResult(true, carrierEstimate(true))
                product.shipsInternationally -> DeliveryResult(true, carrierEstimate(false))
                else -> DeliveryResult(serviceable = false)
            }
        } catch (e: Exception) {
            DeliveryResult(error = "Unable to check delivery at this time")
        }
    }

    /** "d MMM" of a `yyyy-MM-dd` date, as the web `toLocaleDateString({ day, month: 'short' })`. */
    fun formatDate(date: String): String {
        return try {
            val parsed: Date = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(date) ?: return date
            SimpleDateFormat("d MMM", Locale.getDefault()).format(parsed)
        } catch (e: Exception) {
            date
        }
    }

    private fun str(o: JSONObject?, key: String): String = if (o == null || o.isNull(key)) "" else o.optString(key).trim()

    private suspend fun httpGet(url: String): JSONObject? = withContext(Dispatchers.IO) {
        try {
            val req = Request.Builder().url(url).header("Accept", "application/json")
                .header("User-Agent", "BZEAD-Android").build()
            http.newCall(req).execute().use { res ->
                if (!res.isSuccessful) null else JSONObject(res.body?.string().orEmpty())
            }
        } catch (e: Exception) {
            null
        }
    }

    /** Web `reverseGeocode`: edge function `reverse-geocode`, then Nominatim. Throws when both fail. */
    suspend fun reverseGeocode(latitude: Double, longitude: Double, token: String): DetectedLocation {
        val res = SupabaseRest.callFunction(
            "reverse-geocode", token, JSONObject().put("latitude", latitude).put("longitude", longitude)
        )
        val d = if (res.ok) res.json()?.optJSONObject("data") else null
        if (d != null && str(d, "country").isNotEmpty()) {
            return DetectedLocation(
                place = str(d, "place"), city = str(d, "city"), state = str(d, "state"),
                country = str(d, "country"), countryCode = str(d, "countryCode").uppercase(),
                postalCode = str(d, "postalCode").ifEmpty { str(d, "postal_code") },
                latitude = latitude, longitude = longitude,
                provider = str(d, "provider").ifEmpty { "edge-function" }
            )
        }
        val payload = httpGet(
            "https://nominatim.openstreetmap.org/reverse?format=jsonv2&lat=$latitude&lon=$longitude&zoom=10&addressdetails=1"
        ) ?: throw Exception("Unable to resolve location details.")
        val a = payload.optJSONObject("address")
        fun first(vararg keys: String): String = keys.map { str(a, it) }.firstOrNull { it.isNotEmpty() }.orEmpty()
        return DetectedLocation(
            place = first("suburb", "neighbourhood", "road", "city_district"),
            city = first("city", "town", "village", "hamlet", "county"),
            state = first("state", "region"),
            country = str(a, "country"),
            countryCode = str(a, "country_code").uppercase(),
            postalCode = str(a, "postcode"),
            latitude = latitude, longitude = longitude, provider = "nominatim-fallback"
        )
    }

    /** Web `detectLocationByIP`: ipapi.co, then ipwho.is. */
    suspend fun detectByIp(): DetectedLocation? {
        val a = httpGet("https://ipapi.co/json/")
        if (a != null && str(a, "country_name").isNotEmpty()) {
            return DetectedLocation(
                city = str(a, "city"), state = str(a, "region"), country = str(a, "country_name"),
                countryCode = str(a, "country_code").uppercase(), postalCode = str(a, "postal"),
                latitude = a.optDouble("latitude", 0.0), longitude = a.optDouble("longitude", 0.0),
                provider = "ipapi-fallback"
            )
        }
        val b = httpGet("https://ipwho.is/")
        if (b != null && b.optBoolean("success", true) && str(b, "country").isNotEmpty()) {
            return DetectedLocation(
                city = str(b, "city"), state = str(b, "region"), country = str(b, "country"),
                countryCode = str(b, "country_code").uppercase(), postalCode = str(b, "postal"),
                latitude = b.optDouble("latitude", 0.0), longitude = b.optDouble("longitude", 0.0),
                provider = "ipwhois-fallback"
            )
        }
        return null
    }

    /** Web `saveUserLocationCache` (upsert on `user_id`). */
    suspend fun saveLocationCache(token: String, userId: String, loc: DetectedLocation) {
        fun round3(v: Double) = Math.round(v * 1000.0) / 1000.0
        val iso = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply { timeZone = TimeZone.getTimeZone("UTC") }
        val row = JSONObject()
            .put("user_id", userId)
            .put("latitude", round3(loc.latitude))
            .put("longitude", round3(loc.longitude))
            .put("place", if (loc.place.isEmpty()) JSONObject.NULL else loc.place)
            .put("city", loc.city).put("state", loc.state).put("country", loc.country)
            .put("country_code", loc.countryCode)
            .put("provider", if (loc.provider.isEmpty()) JSONObject.NULL else loc.provider)
            .put("resolved_at", iso.format(Date()))
        SupabaseRest.request(
            "POST", "user_location_cache?on_conflict=user_id", token, row.toString(),
            prefer = "resolution=merge-duplicates,return=minimal"
        )
    }
}
