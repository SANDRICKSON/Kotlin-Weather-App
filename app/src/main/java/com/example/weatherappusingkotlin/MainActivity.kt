package com.example.weatherappusingkotlin

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import com.example.weatherappusingkotlin.databinding.ActivityMainBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private val retrofit by lazy {
        Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .build()
            .create(ApiInterface::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        fetchWeatherData("chiatura")
        setupSearchView()
        getCurrentLocationWeather()
    }

    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query != null) {
                    fetchWeatherData(query)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }
        })
    }

    private fun getCurrentLocationWeather() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1001
            )
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val lat = location.latitude
                val lon = location.longitude
                fetchWeatherByCoordinates(lat, lon)
            } else {
                Toast.makeText(this, "Can't access location", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun fetchWeatherByCoordinates(lat: Double, lon: Double) {
        binding.progressBar.visibility = View.VISIBLE
        val response = retrofit.getWeatherByCoordinates(
            lat,
            lon,
            "186a29d6767a05316291c3e249bfd187",
            "metric"
        )

        response.enqueue(object : Callback<WeatherApp> {
            override fun onResponse(call: Call<WeatherApp>, response: Response<WeatherApp>) {
                binding.progressBar.visibility = View.GONE
                if (response.isSuccessful && response.body() != null) {
                    updateUIWithWeather(response.body()!!)
                }
            }

            override fun onFailure(call: Call<WeatherApp>, t: Throwable) {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(this@MainActivity, "Failed to load GPS weather", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun fetchWeatherData(city: String) {
        binding.progressBar.visibility = View.VISIBLE

        val response = retrofit.getWeatherData(
            city,
            "186a29d6767a05316291c3e249bfd187",
            "metric"
        )

        response.enqueue(object : Callback<WeatherApp> {
            override fun onResponse(call: Call<WeatherApp>, response: Response<WeatherApp>) {
                binding.progressBar.visibility = View.GONE
                val body = response.body()
                if (response.isSuccessful && body != null) {
                    updateUIWithWeather(body)
                } else {
                    Toast.makeText(this@MainActivity, "City not found", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<WeatherApp>, t: Throwable) {
                binding.progressBar.visibility = View.GONE
                Log.e("WEATHER_ERROR", "Failed to fetch weather data", t)
                Toast.makeText(this@MainActivity, "Network error", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateUIWithWeather(data: WeatherApp) {
        val temperature = data.main.temp.toString()
        val humidity = data.main.humidity
        val windSpeed = data.wind.speed
        val sunRise = data.sys.sunrise.toLong()
        val sunSet = data.sys.sunset.toLong()
        val seaLevel = data.main.pressure
        val condition = data.weather.firstOrNull()?.main ?: "Unknown"
        val maxTemp = data.main.temp_max
        val minTemp = data.main.temp_min

        binding.temp.text = "$temperature°C"
        binding.weatherCondition.text = condition
        binding.maxTem.text = "Max Temp: $maxTemp°C"
        binding.minTem.text = "Min Temp: $minTemp°C"
        binding.humidity.text = "$humidity %"
        binding.windspeed.text = "$windSpeed m/s"
        binding.sunRise.text = convertTimestampToTime(sunRise)
        binding.sunSet.text = convertTimestampToTime(sunSet)
        binding.seaLevel.text = "$seaLevel hPa"
        binding.cityName.text = data.name
        binding.dayText.text = dayName(System.currentTimeMillis())
        binding.dateText.text = date()
        binding.clockText.text = getCurrentTime()

        changeImagesAccordingToWeatherCondition(condition)


        if (isNightTime(sunRise, sunSet)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }

    private fun isNightTime(sunrise: Long, sunset: Long): Boolean {
        val now = System.currentTimeMillis() / 1000
        return now < sunrise || now > sunset
    }

    private fun changeImagesAccordingToWeatherCondition(condition: String) {
        when (condition.lowercase(Locale.getDefault())) {
            "clear", "sunny" -> {
                binding.root.setBackgroundResource(R.drawable.sunny_background)
                binding.lottieAnimationView.setAnimation(R.raw.sun)
            }
            "clouds", "overcast", "mist", "foggy" -> {
                binding.root.setBackgroundResource(R.drawable.colud_background)
                binding.lottieAnimationView.setAnimation(R.raw.cloud)
            }
            "drizzle", "moderate rain", "showers", "heavy rain", "light rain" -> {
                binding.root.setBackgroundResource(R.drawable.rain_background)
                binding.lottieAnimationView.setAnimation(R.raw.rain)
            }
            "light snow", "moderate snow", "heavy snow", "blizzard" -> {
                binding.root.setBackgroundResource(R.drawable.snow_background)
                binding.lottieAnimationView.setAnimation(R.raw.snow)
            }
            else -> {
                binding.root.setBackgroundResource(R.drawable.sunny_background)
                binding.lottieAnimationView.setAnimation(R.raw.sun)
            }
        }
        binding.lottieAnimationView.playAnimation()
    }

    private fun convertTimestampToTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        return sdf.format(Date(timestamp * 1000))
    }

    private fun dayName(timestamp: Long): String {
        val sdf = SimpleDateFormat("EEEE", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    private fun date(): String {
        val sdf = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
        return sdf.format(Date())
    }

    private fun getCurrentTime(): String {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        return sdf.format(Date())
    }
}
