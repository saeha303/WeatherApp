package com.example.weatherapp

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import okhttp3.OkHttpClient
import okhttp3.Request
import com.google.gson.Gson
import java.io.IOException
interface GoogleBooksService {
    @GET("volumes")
    fun searchBooks(
        @Query("q") query: String
    ): Call<BookResponse>
}
data class WeatherInfo(val description: String, val video: String)
data class BookResponse(
    val kind: String,
    val totalItems: Int,
    val items: List<BookItem>
)

data class BookItem(
    val kind: String,
    val id: String,
    val volumeInfo: VolumeInfo
)

data class VolumeInfo(
    val title: String,
    val authors: List<String>,
    val publisher: String?,
    val publishedDate: String?,
    val description: String?,
    val imageLinks: ImageLinks?
)

data class ImageLinks(
    val thumbnail: String?
)

data class Item(
    val title: String,
    val authors: String,
    val description: String,
    val photo: String
)
//location
data class Suggestion(
    val title: String,
    val address: Address
)

data class Address(
    val label: String
)

data class AutosuggestResponse(
    val items: List<Suggestion>
)

object Utils {
    private val videoBaseUrl="android.resource://com.example.weatherapp/"
    private var isPaused=true
    fun getIsPaused(): Boolean {
        return this.isPaused
    }
    fun toggleIsPaused(){
        isPaused=!isPaused
    }
    fun setIsPaused(flag:Boolean){
        isPaused=flag
    }
    private val weatherCodes: Map<Int, WeatherInfo> = mapOf(
        0 to WeatherInfo("Clear sky", videoBaseUrl + R.raw.clear_sky),
        1 to WeatherInfo("Mainly clear, partly cloudy, and overcast", videoBaseUrl + R.raw.clear_sky),
        2 to WeatherInfo("Mainly clear, partly cloudy, and overcast", videoBaseUrl + R.raw.clear_sky),
        3 to WeatherInfo("Mainly clear, partly cloudy, and overcast", videoBaseUrl + R.raw.clear_sky),
        45 to WeatherInfo("Fog and depositing rime fog", videoBaseUrl + R.raw.clear_sky),
        48 to WeatherInfo("Fog and depositing rime fog", videoBaseUrl + R.raw.clear_sky),
        51 to WeatherInfo("Drizzle: Light, moderate, and dense intensity", videoBaseUrl + R.raw.clear_sky),
        53 to WeatherInfo("Drizzle: Light, moderate, and dense intensity", videoBaseUrl + R.raw.clear_sky),
        55 to WeatherInfo("Drizzle: Light, moderate, and dense intensity", videoBaseUrl + R.raw.clear_sky),
        56 to WeatherInfo("Freezing Drizzle: Light and dense intensity", videoBaseUrl + R.raw.clear_sky),
        57 to WeatherInfo("Freezing Drizzle: Light and dense intensity", videoBaseUrl + R.raw.clear_sky),
        61 to WeatherInfo("Rain: Slight, moderate and heavy intensity", videoBaseUrl + R.raw.clear_sky),
        63 to WeatherInfo("Rain: Slight, moderate and heavy intensity", videoBaseUrl + R.raw.clear_sky),
        65 to WeatherInfo("Rain: Slight, moderate and heavy intensity", videoBaseUrl + R.raw.clear_sky),
        66 to WeatherInfo("Freezing Rain: Light and heavy intensity", videoBaseUrl + R.raw.clear_sky),
        67 to WeatherInfo("Freezing Rain: Light and heavy intensity", videoBaseUrl + R.raw.clear_sky),
        71 to WeatherInfo("Snow fall: Slight, moderate, and heavy intensity", videoBaseUrl + R.raw.clear_sky),
        73 to WeatherInfo("Snow fall: Slight, moderate, and heavy intensity", videoBaseUrl + R.raw.clear_sky),
        75 to WeatherInfo("Snow fall: Slight, moderate, and heavy intensity", videoBaseUrl + R.raw.clear_sky),
        77 to WeatherInfo("Snow grains", videoBaseUrl + R.raw.clear_sky),
        80 to WeatherInfo("Rain showers: Slight, moderate, and violent", videoBaseUrl + R.raw.clear_sky),
        81 to WeatherInfo("Rain showers: Slight, moderate, and violent", videoBaseUrl + R.raw.clear_sky),
        82 to WeatherInfo("Rain showers: Slight, moderate, and violent", videoBaseUrl + R.raw.clear_sky),
        85 to WeatherInfo("Snow showers slight and heavy", videoBaseUrl + R.raw.clear_sky),
        86 to WeatherInfo("Snow showers slight and heavy", videoBaseUrl + R.raw.clear_sky),
        95 to WeatherInfo("Thunderstorm: Slight or moderate", videoBaseUrl + R.raw.clear_sky),
        96 to WeatherInfo("Thunderstorm with slight and heavy hail", videoBaseUrl + R.raw.clear_sky),
        99 to WeatherInfo("Thunderstorm with slight and heavy hail", videoBaseUrl + R.raw.clear_sky)
    )

    fun getWeatherInfo(code: Int): WeatherInfo? {
        return weatherCodes[code]
    }
//    fun transparentStatusAndNavigation(
//        systemUiScrim: Int = Color.parseColor("#40000000") // 25% black
//    ) {
//        var systemUiVisibility = 0
//        // Use a dark scrim by default since light status is API 23+
//        var statusBarColor = systemUiScrim
//        //  Use a dark scrim by default since light nav bar is API 27+
//        var navigationBarColor = systemUiScrim
//        val winParams = window.attributes
//
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            systemUiVisibility = systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
//            statusBarColor = Color.TRANSPARENT
//        }
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            systemUiVisibility = systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
//            navigationBarColor = Color.TRANSPARENT
//        }
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//            systemUiVisibility = systemUiVisibility or
//                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
//                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
//                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//            window.decorView.systemUiVisibility = systemUiVisibility
//        }
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
//            winParams.flags = winParams.flags or
//                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS or
//                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION
//        }
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            winParams.flags = winParams.flags and
//                    (WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS or
//                            WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION).inv()
//            window.statusBarColor = statusBarColor
//            window.navigationBarColor = navigationBarColor
//        }
//
//        window.attributes = winParams
//    }
//    private fun enableFullScreenMode() {
//        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
//                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
//                or View.SYSTEM_UI_FLAG_FULLSCREEN)
//    }
}
class AutosuggestRepository {
    private val client = OkHttpClient()
    private val gson = Gson()

    fun getSuggestions(query: String, latitude: Double, longitude: Double, apiKey: String, callback: (List<Suggestion>?) -> Unit) {
        val url = "https://autosuggest.search.hereapi.com/v1/autosuggest?at=$latitude,$longitude&q=$query&apikey=$apiKey"
        val request = Request.Builder().url(url).build()

        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                callback(null)
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                response.body?.string()?.let {
                    val autosuggestResponse = gson.fromJson(it, AutosuggestResponse::class.java)
                    callback(autosuggestResponse.items)
                }
            }
        })
    }
}