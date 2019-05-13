package eu.vmpay.weathermate.mainActivity

import android.app.Activity
import android.location.Location
import android.os.CountDownTimer
import android.util.Log
import eu.vmpay.weathermate.utils.location.LocationContract
import eu.vmpay.weathermate.utils.location.LocationService
import eu.vmpay.weathermate.utils.rest.OpenWeatherCoroutineService
import eu.vmpay.weathermate.utils.rest.WeatherResponse
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Created by andrew on 1/19/18.
 */

class MainPresenter private constructor() : MainContract.Presenter, LocationContract.Receiver {
    private val TAG = "MainPresenter"
    private val BASE_URL = "https://api.openweathermap.org/"

    private var retrofit: Retrofit? = null
    private var openWeatherService: OpenWeatherCoroutineService? = null
    private var locationService: LocationContract.Service? = null

    private var mainView: MainContract.View? = null
    private var refreshLocationTimer: CountDownTimer? = null
    private var requestWeatherTimer: CountDownTimer? = null

    override fun setUp(activity: Activity) {
        Log.d(TAG, "setUp")
        retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL + "data/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        openWeatherService = retrofit?.create(OpenWeatherCoroutineService::class.java)

        refreshLocationTimer = object : CountDownTimer(60000, 60000) {
            override fun onTick(millisUntilFinished: Long) {}

            override fun onFinish() {
                mainView?.showLocationError()
            }
        }
        requestWeatherTimer = object : CountDownTimer(60000, 60000) {
            override fun onTick(millisUntilFinished: Long) {}

            override fun onFinish() {
                mainView?.showNetworkError()
            }
        }

        locationService = LocationService(activity)
    }


    /**
     * Binds presenter with a view when resumed. The Presenter will perform initialization here.
     *
     * @param view the view associated with this presenter
     */
    override fun takeView(view: MainContract.View) {
        Log.d(TAG, "takeView")
        mainView = view
        loadCachedWeather()
    }

    /**
     * Drops the reference to the view when destroyed
     */
    override fun dropView() {
        Log.d(TAG, "dropView")
        mainView = null
        //		locationService.disconnect();
    }

    override fun updateWeather() {
        loadCachedWeather()
    }

    override fun updateLocation() {
        refreshLocationTimer?.start()
        locationService?.getLastKnownLocation()
    }

    private fun loadCachedWeather() {
        val location = mainView?.readLocationData()
        if (location != null && location.size == 2 && location[0] != 0f && location[1] != 0f) {
            loadWeather(location[0], location[1])
        }
        locationService?.getLastKnownLocation()
    }

    private fun loadWeather(location: Location) {
        loadWeather(location.latitude, location.longitude)
    }

    private fun loadWeather(latitude: Double, longitude: Double) {
        loadWeather(latitude.toFloat(), longitude.toFloat())
    }

    private fun loadWeather(latitude: Float, longitude: Float) {
        requestWeatherTimer?.start()
        Log.d(TAG, "lat: $latitude lon: $longitude")
        GlobalScope.launch(IO) {
            var weatherResponse: WeatherResponse? = null
            try {
                weatherResponse = openWeatherService?.getWeatherByCoordinates("15646a06818f61f7b8d7823ca833e1ce", "$latitude", "$longitude", "metric")
            } catch (e: Exception) {
                e.printStackTrace()
            }
            GlobalScope.launch(Main) {
                mainView?.run {
                    if (weatherResponse != null) {
                        showWeather(weatherResponse.name, weatherResponse.weather[0].main, weatherResponse.main.temp)
                        showIcon("${BASE_URL}img/w/${weatherResponse.weather[0].icon}")
                    } else
                        mainView?.showNetworkError()
                }
            }
            requestWeatherTimer?.cancel()
        }
    }

    override fun onLocationUpdate(location: Location?) {
        refreshLocationTimer?.cancel()
        if (location != null) {
            loadWeather(location)
            mainView?.writeLocationData(location)
        } else {
            mainView?.showLocationError()
        }
    }

    override fun postError() {
        mainView?.showError()
    }

    companion object {
        val instance = MainPresenter()
    }
}