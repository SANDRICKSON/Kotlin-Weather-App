package com.example.weatherappusingkotlin


import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiInterface {
    @GET("weather")
    fun getWeatherData(
        @Query("q") city:String,
        @Query("appid") appids:String,
        @Query("units") units:String
    ) : Call<WeatherApp>
}