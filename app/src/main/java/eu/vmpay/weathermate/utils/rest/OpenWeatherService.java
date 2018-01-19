package eu.vmpay.weathermate.utils.rest;

import io.reactivex.Flowable;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by andrew on 1/19/18.
 */

public interface OpenWeatherService
{
	@GET("2.5/weather")
	Flowable<WeatherResponse> getWeatherByCoordinates(@Query("APPID") String apiKey, @Query("lat") String lat, @Query("lon") String lon, @Query("units") String units);
}
