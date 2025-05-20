package com.example.weatherappusingkotlin

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiInterface {

    // ამინდის გამოთხოვნა ქალაქის სახელით
    @GET("weather")
    fun getWeatherData(
        @Query("q") city: String,
        @Query("appid") appid: String,
        @Query("units") units: String
    ): Call<WeatherApp>

    // ამინდის გამოთხოვნა კოორდინატებით (GPS)
    @GET("weather")
    fun getWeatherByCoordinates(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("appid") appid: String,
        @Query("units") units: String
    ): Call<WeatherApp>
}
