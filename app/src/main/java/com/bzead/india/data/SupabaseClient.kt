package com.bzead.india.data

import com.bzead.india.model.CountryItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

object SupabaseClient {

    internal const val HOST_URL = "https://aiiefgjfftmerayihpbv.supabase.co"
    internal const val BASE_URL = "$HOST_URL/rest/v1"
    internal const val ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImFpaWVmZ2pmZnRtZXJheWlocGJ2Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzExOTc2MDksImV4cCI6MjA4Njc3MzYwOX0.sbXOq4-CAezq-6mbF7wi5XYVYBakzjb9xjGmU77AZ2M"

    suspend fun fetchBanners(): Pair<List<HeroBanner>, List<AdBanner>> = withContext(Dispatchers.IO) {
        val heroList = mutableListOf<HeroBanner>()
        val adList = mutableListOf<AdBanner>()
        try {
            val url = URL("$BASE_URL/banners?select=*&is_active=eq.true")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.setRequestProperty("apikey", ANON_KEY)
            conn.setRequestProperty("Authorization", "Bearer $ANON_KEY")
            conn.connectTimeout = 8000
            conn.readTimeout = 8000

            if (conn.responseCode == 200) {
                val jsonText = conn.inputStream.bufferedReader().use { it.readText() }
                val array = JSONArray(jsonText)
                for (i in 0 until array.length()) {
                    list.add(parseProduct(array.getJSONObject(i), i))
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        list
    }

    /** Active countries from the `countries` table (same query as the web Signup page). */
    suspend fun fetchCountries(): List<CountryItem> = withContext(Dispatchers.IO) {
        val list = mutableListOf<CountryItem>()
        try {
            val url = URL("$BASE_URL/countries?select=id,country_name,short_code,currency_code,dialing_code&is_active=eq.true&order=country_name.asc")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.setRequestProperty("apikey", ANON_KEY)
            conn.setRequestProperty("Authorization", "Bearer $ANON_KEY")
            conn.connectTimeout = 8000
            conn.readTimeout = 8000
            if (conn.responseCode == 200) {
                val array = JSONArray(conn.inputStream.bufferedReader().use { it.readText() })
                for (i in 0 until array.length()) {
                    val o = array.getJSONObject(i)
                    list.add(
                        CountryItem(
                            id = o.optString("id"),
                            countryName = o.optString("country_name"),
                            shortCode = o.optString("short_code"),
                            currencyCode = o.optString("currency_code"),
                            phoneCode = if (o.isNull("dialing_code")) "" else o.optString("dialing_code")
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        list
    }
}
