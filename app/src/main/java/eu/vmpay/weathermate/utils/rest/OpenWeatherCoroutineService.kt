package eu.vmpay.weathermate.utils.rest

import retrofit2.http.GET
import retrofit2.http.Query

interface OpenWeatherCoroutineService {
    @GET("2.5/weather")
    suspend fun getWeatherByCoordinates(@Query("APPID") apiKey: String, @Query("lat") lat: String, @Query("lon") lon: String, @Query("units") units: String): WeatherResponse
}