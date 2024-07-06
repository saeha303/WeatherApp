package com.example.weatherapp

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.media.MediaPlayer
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.ListView
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Locale
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode

//2hyIyUkwVLmv1kAFsNvO:ID
//oO0WU1ynTyvF3pXScEyOp5fkuSdh0MSJFi2XfP0sdsE:api key


//import java.util.jar.Manifest

/*location not rigid
layout change, not like the original
turn into apk
modify spotify
modify book part
insert other things like lifestyle
if possible, insert youtube videos
api key for places: AIzaSyBVu9LCN6I7k0wqm4ADCnH5N7DWAeQ0szM*/
class MainActivity : AppCompatActivity() {

    private lateinit var listView: ListView
    private lateinit var adapter: BookAdapter

    private val LOCATION_PERMISSION_REQUEST_CODE=1
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var LATITUDE:String
    private lateinit var LONGITUDE:String
    private val AUTOCOMPLETE_REQUEST_CODE = 1337

    override fun onCreate(savedInstanceState: Bundle?) {
//        transparentStatusAndNavigation()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        val videoView: VideoView = findViewById<VideoView>(R.id.videoView)

        // Set the path to the video file
        val uri = Uri.parse("android.resource://" + packageName + "/" + R.raw.clear_sky)
        videoView.setVideoURI(uri)


        // If you want the video to start muted
        videoView.setOnPreparedListener { mp ->
            mp.setVolume(0f, 0f)
            mp.isLooping = true
            mp.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT)
            videoView.scaleY=1.5f
        }
        // Start the video
        videoView.start()
//        enableFullScreenMode()
        val play = findViewById<ImageView>(R.id.pausesong)
        play.setOnClickListener {
            println("baksoo")
            if (Utils.getIsPaused()) {
                play.setImageResource(R.drawable.pause)
                if(SpotifyService.check())
                    SpotifyService.connect(this)
                else
                    SpotifyService.resume()
            } else {
                play.setImageResource(R.drawable.play)
                SpotifyService.pause()
            }
            Utils.toggleIsPaused()

        }
        val stop=findViewById<ImageView>(R.id.stopapp)
        stop.setOnClickListener {
            println("oh hi")
            SpotifyService.disconnect()
            Utils.setIsPaused(true)
        }
//        book part==================================

        // Find a reference to the {@link ListView} in the layout
        listView = findViewById<View>(R.id.list) as ListView
//        listView.visibility=View.VISIBLE
//        val mNoResultTextView = findViewById<View>(R.id.empty_view) as TextView
//        listView.emptyView = mNoResultTextView

//        // Create a new adapter that takes an empty list of books as input
//        adapter = BookAdapter(this, mutableListOf<Item>())
//
//
//        // Set the adapter on the {@link ListView}
//        // so the list can be populated in the user interface
//        listView.adapter = adapter
        // Create a logging interceptor
//        val logging = HttpLoggingInterceptor().apply {
//            level = HttpLoggingInterceptor.Level.BODY
//        }
//
//// Create an OkHttpClient and attach the interceptor
//        val okHttpClient = OkHttpClient.Builder()
//            .addInterceptor(logging)
//            .build()
        val retrofit = Retrofit.Builder()
            .baseUrl("https://www.googleapis.com/books/v1/")
//            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(GoogleBooksService::class.java)
        // Search for books with a keyword
        searchBooks(service,"Harry") // Replace with your search keyword
//        val listView: ListView = findViewById(R.id.list)
//        val adapter = BookAdapter(this, items)
//        listView.adapter = adapter
//      location is not hardcoded anymore=======================
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        checkLocationPermission()
//        location can be searched
        // Initialize the Places API
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, "AIzaSyBVu9LCN6I7k0wqm4ADCnH5N7DWAeQ0szM")
        }

        val placesClient: PlacesClient = Places.createClient(this)
        val searchButton: ImageView = findViewById(R.id.search)
        searchButton.setOnClickListener {
            openPlaceAutocomplete()
            }
//        weatherTask().execute()
    }
    private fun checkLocationPermission() {
        println("checkLocationPermission")
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
        } else {
            getCurrentLocation()
        }
    }
    private fun openPlaceAutocomplete() {
        val fields = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG)
        val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields).build(this)
        startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE)
    }
    private fun searchBooks(service: GoogleBooksService, query: String) {
        val call = service.searchBooks(query)
        println(call)
        call.enqueue(object : Callback<BookResponse> {
            override fun onResponse(call: Call<BookResponse>, response: Response<BookResponse>) {
                if (response.isSuccessful) {
                    val books = response.body()?.items ?: emptyList()
                    println("books:")
//                    println(books)
                    val itemList = books.map { bookItem ->
                        val volumeInfo = bookItem.volumeInfo
                        val title = volumeInfo.title
                        val authors = volumeInfo.authors?.joinToString(", ") ?: "No authors listed"
                        val description = volumeInfo.description ?: "No description available"
                        val photoLink = volumeInfo.imageLinks?.thumbnail ?: ""
                        println(title)
                        Item(
                            title = title,
                            authors = authors,
                            description = description,
                            photo = photoLink
                        )
                    }.toMutableList()
//                    val itemList = mutableListOf(
//                        Item(title = "Book 1", authors = "A", description = "Description 1"),
//                        Item(title = "Book 2", authors = "B", description = "Description 2")
//                    )
//                    println(itemList)
                    val temp= emptyList<Item>().toMutableList()
                    temp.addAll(itemList)
                    adapter = BookAdapter(this@MainActivity, itemList.take(5).toMutableList())
                    listView.adapter = adapter
                    adapter.notifyDataSetChanged()
                    println("shesh")
                    println(temp)
                    // Log the size of the list to verify
                    Log.d("MainActivity", "Number of books fetched: ${itemList.size}")
                    // Scroll listener to load more items when reaching the bottom
//                    listView.setOnScrollListener(object : AbsListView.OnScrollListener {
//                        override fun onScrollStateChanged(view: AbsListView?, scrollState: Int) {}
//
//                        override fun onScroll(view: AbsListView?, firstVisibleItem: Int, visibleItemCount: Int, totalItemCount: Int) {
//                            if (firstVisibleItem + visibleItemCount >= totalItemCount && totalItemCount != 0) {
//                                loadMoreItems(itemList)
//                            }
//                        }
//                    })
                    runOnUiThread {
//                        adapter.clear()
//                        for (book in itemList) {
//                            println(book)
//                            adapter.add(book)
//                        }
//                        adapter.addAll(temp)
//                        adapter.notifyDataSetChanged()
//                        findViewById<ProgressBar>(R.id.progress_bar).visibility = View.GONE
                    }
                } else {
                    Toast.makeText(this@MainActivity, "Failed to load books", Toast.LENGTH_SHORT).show()
                    Log.e("MainActivity", "Failed to fetch books: ${response.message()}")
                    runOnUiThread {
//                        findViewById<ProgressBar>(R.id.progress_bar).visibility = View.GONE
                    }
                }
            }


            override fun onFailure(call: Call<BookResponse>, t: Throwable) {
                Toast.makeText(this@MainActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                Log.e("MainActivity", "Network request failed", t)
                runOnUiThread {
//                    findViewById<ProgressBar>(R.id.progress_bar).visibility = View.GONE
                }
            }
        })
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                getCurrentLocation()
            } else {
                // Permission denied, show a message to the user
                Log.e("MainActivity", "Location permission denied")
            }
        }
    }

    private fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        println("getCurrentLocation")
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                location?.let {
                    LATITUDE=it.latitude.toString()
                    LONGITUDE=it.longitude.toString()
                    // Use latitude and longitude to fetch weather data
                    Log.d("MainActivity", "Latitude: $LATITUDE, Longitude: $LONGITUDE")
                    weatherTask().execute()
                }
            }
    }
    override fun onResume() {
        super.onResume()
        // Resume playing the video when the activity resumes
        findViewById<VideoView>(R.id.videoView).start()
//        enableFullScreenMode()
    }

    override fun onPause() {
        super.onPause()
        // Pause the video when the activity is paused
        findViewById<VideoView>(R.id.videoView).pause()
    }

    override fun onStop() {
        super.onStop()
        SpotifyService.disconnect()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        println("onActivityResult")
        SpotifyService.postOnActivityResult(requestCode,resultCode,intent,this)
        if(requestCode==AUTOCOMPLETE_REQUEST_CODE) {
            when (resultCode) {
                RESULT_OK -> {
                    val place = Autocomplete.getPlaceFromIntent(intent!!)
                    val latLng = place.latLng
                    if (latLng != null) {
                        LATITUDE = latLng.latitude.toString()
                        LONGITUDE = latLng.longitude.toString()
                        // Use latitude and longitude to fetch weather data
                    }
                }
                AutocompleteActivity.RESULT_ERROR -> {
                    val status = Autocomplete.getStatusFromIntent(intent!!)
                    Log.i("MainActivity", status.statusMessage ?: "Error")
                }
                RESULT_CANCELED -> {
                    // The user canceled the operation
                }
            }
        }
    }
//    override fun onNewIntent(intent: Intent?) {
//        println("on new intent")
//        super.onNewIntent(intent)
//        SpotifyService.handleSpotifyLoginResponse(intent)
//    }

    inner class weatherTask() : AsyncTask<String, Void, String>() {

        override fun onPreExecute() {
            super.onPreExecute()
            /* Showing the ProgressBar, Making the main design GONE */
            findViewById<ProgressBar>(R.id.loader).visibility = View.VISIBLE
            findViewById<RelativeLayout>(R.id.mainContainer).visibility = View.GONE
            findViewById<TextView>(R.id.errorText).visibility = View.GONE
        }

        override fun doInBackground(vararg params: String?): String? {
            var response:String?
            try{
//                22.3384 91.8317
                println("I don't know")
                println(LATITUDE)
                println(LONGITUDE)
                println("https://api.open-meteo.com/v1/forecast?latitude=$LATITUDE&longitude=$LONGITUDE&current=temperature_2m,relative_humidity_2m,apparent_temperature,is_day,precipitation,rain,showers,weather_code,wind_speed_10m&hourly=temperature_2m,relative_humidity_2m,apparent_temperature,pressure_msl,wind_speed_80m,temperature_80m&daily=weather_code,temperature_2m_max,temperature_2m_min,sunrise,sunset&timezone=auto&forecast_days=1")
                response = URL("https://api.open-meteo.com/v1/forecast?latitude=$LATITUDE&longitude=$LONGITUDE&current=temperature_2m,relative_humidity_2m,apparent_temperature,is_day,precipitation,rain,showers,weather_code,wind_speed_10m&hourly=temperature_2m,relative_humidity_2m,apparent_temperature,pressure_msl,wind_speed_80m,temperature_80m&daily=weather_code,temperature_2m_max,temperature_2m_min,sunrise,sunset&timezone=auto&forecast_days=1").readText(
                    Charsets.UTF_8
                )

            }catch (e: Exception){
                println("here I am")
                response = null
            }
            return response
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            println("small girl fantasy")
            try {
                println("lover")
                val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.getDefault())
                val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())

                /* Extracting JSON returns from the API */
                val jsonObj = JSONObject(result)
                println("chua")
                val current = jsonObj.getJSONObject("current")
                val daily=jsonObj.getJSONObject("daily")
                val hourly = jsonObj.getJSONObject("hourly")

                val updatedAt:String = current.getString("time")
                val date = formatter.parse(updatedAt)
                val updatedAtText = "Updated at: "+ dateFormatter.format(date)

                val temp = current.getString("temperature_2m")+"°C"
                val tempMin = "Min Temp: " + daily.getJSONArray("temperature_2m_min").getString(0)+"°C"
                val tempMax = "Max Temp: " + daily.getJSONArray("temperature_2m_max").getString(0)+"°C"
                val pressure = hourly.getJSONArray("pressure_msl").getString(0)+"hPa"
                val humidity = current.getString("relative_humidity_2m")+"%"

                val sunrise = daily.getJSONArray("sunrise").getString(0)
                val sunriseTime = formatter.parse(sunrise)
                val sunriseText = timeFormatter.format(sunriseTime)+"AM"

                val sunset = daily.getJSONArray("sunset").getString(0)
                val sunsetTime = formatter.parse(sunset)
                val sunsetText = timeFormatter.format(sunsetTime)+"PM"

                val windSpeed = hourly.getJSONArray("wind_speed_80m").getString(0)+"km/h"
                val weatherCode = daily.getJSONArray("weather_code").getInt(0)
                val weatherInfo=Utils.getWeatherInfo(weatherCode)
                if (weatherInfo != null) {
                    val uri = Uri.parse(weatherInfo.video)
                    val videoView: VideoView = findViewById<VideoView>(R.id.videoView)
                    videoView.setVideoURI(uri)
                    videoView.start()

                    videoView.setOnPreparedListener { mp ->
                        mp.isLooping = true
                        mp.setVolume(0f, 0f)
                    }
                }
                val address = jsonObj.getString("timezone")
                /* Populating extracted data into our views */
                findViewById<TextView>(R.id.address).text = address
                findViewById<TextView>(R.id.updated_at).text =  updatedAtText
                if(weatherInfo != null)
                    findViewById<TextView>(R.id.status).text = weatherInfo.description.capitalize()
                findViewById<TextView>(R.id.temp).text = temp
                findViewById<TextView>(R.id.temp_min).text = tempMin
                findViewById<TextView>(R.id.temp_max).text = tempMax
                findViewById<TextView>(R.id.sunrise).text = sunriseText
                findViewById<TextView>(R.id.sunset).text = sunsetText
                findViewById<TextView>(R.id.wind).text = windSpeed
                findViewById<TextView>(R.id.pressure).text = pressure
                findViewById<TextView>(R.id.humidity).text = humidity

                /* Views populated, Hiding the loader, Showing the main design */
                findViewById<ProgressBar>(R.id.loader).visibility = View.GONE
                findViewById<RelativeLayout>(R.id.mainContainer).visibility = View.VISIBLE

            } catch (e: Exception) {
                println("run on")
                findViewById<RelativeLayout>(R.id.mainContainer).visibility = View.VISIBLE
                findViewById<ProgressBar>(R.id.loader).visibility = View.GONE
                findViewById<TextView>(R.id.errorText).visibility = View.VISIBLE
            }

        }
    }

}
