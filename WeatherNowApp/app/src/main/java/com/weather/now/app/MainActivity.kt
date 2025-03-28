package com.weather.now.app


import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import org.json.JSONObject

class MainActivity : AppCompatActivity() {

    private lateinit var cityInput: EditText
    private lateinit var searchButton: Button
    private lateinit var temperatureText: TextView
    private lateinit var humidityText: TextView
    private lateinit var descriptionText: TextView
    private lateinit var weatherIcon: ImageView
    private lateinit var progressBar: ProgressBar

    private val API_KEY = "7321fbfa0918f349fe756d8538d8aeb2"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        cityInput = findViewById(R.id.cityInput)
        searchButton = findViewById(R.id.searchButton)
        temperatureText = findViewById(R.id.temperatureText)
        humidityText = findViewById(R.id.humidityText)
        descriptionText = findViewById(R.id.descriptionText)
        weatherIcon = findViewById(R.id.weatherIcon)
        progressBar = findViewById(R.id.progressBar)

        val lastCity = loadLastCity()

        val initialCity = lastCity.ifEmpty { "Toronto" }
        cityInput.setText(initialCity)
        getWeatherInfo(initialCity)

        searchButton.setOnClickListener {
            val city = cityInput.text.toString().trim()
            if (city.isNotEmpty()) {
                saveCity(city)
                getWeatherInfo(city)
            } else {
                Toast.makeText(this@MainActivity, "Enter a city name", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getWeatherInfo(city: String) {
        progressBar.visibility = View.VISIBLE
        val url = "https://api.openweathermap.org/data/2.5/weather?q=$city&appid=$API_KEY&units=metric"

        val jsonObjectRequest = JsonObjectRequest(Request.Method.GET, url, null,
            { response ->
                progressBar.visibility = View.GONE
                parseWeatherData(response)
            },
            { error ->
                progressBar.visibility = View.GONE
                Toast.makeText(this@MainActivity, "Failed to fetch weather data", Toast.LENGTH_SHORT).show()
            })

        Volley.newRequestQueue(this@MainActivity).add(jsonObjectRequest)
    }

    private fun parseWeatherData(response: JSONObject) {
        val main = response.getJSONObject("main")
        val weatherArray = response.getJSONArray("weather").getJSONObject(0)

        val temperature = main.getDouble("temp")
        val humidity = main.getInt("humidity")
        val description = weatherArray.getString("description")
        val icon = weatherArray.getString("icon")

        temperatureText.text = "Temperature: $temperature°C"
        humidityText.text = "Humidity: $humidity%"
        descriptionText.text = description.capitalize()

        val iconUrl = "https://openweathermap.org/img/wn/$icon@2x.png"
        Glide.with(this@MainActivity).load(iconUrl).into(weatherIcon)
    }

    // Save the last searched city in SharedPreferences
    private fun saveCity(city: String) {
        val sharedPref = getSharedPreferences("WeatherApp", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("lastCity", city)
            apply()
        }
    }

    private fun loadLastCity(): String {
        val sharedPref = getSharedPreferences("WeatherApp", Context.MODE_PRIVATE)
        return sharedPref.getString("lastCity", "") ?: ""
    }
}
