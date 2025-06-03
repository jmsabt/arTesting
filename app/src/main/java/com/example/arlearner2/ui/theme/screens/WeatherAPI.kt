package com.example.arlearner2.network

import retrofit2.http.GET
import retrofit2.http.Query

data class WeatherResponse(
    val main: Main
)

data class Main(
    val temp: Double
)

interface WeatherApi {
    @GET("weather")
    suspend fun getCurrentWeather(
        @Query("q") city: String,             // city name, e.g. "Quezon City,PH"
        @Query("appid") apiKey: String,       // your OpenWeatherMap API key
        @Query("units") units: String = "metric" // Celsius
    ): WeatherResponse
}
