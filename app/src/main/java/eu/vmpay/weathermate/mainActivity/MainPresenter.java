package eu.vmpay.weathermate.mainActivity;

import android.app.Activity;
import android.location.Location;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscription;

import java.util.Locale;

import eu.vmpay.weathermate.utils.location.LocationContract;
import eu.vmpay.weathermate.utils.location.LocationService;
import eu.vmpay.weathermate.utils.rest.OpenWeatherService;
import eu.vmpay.weathermate.utils.rest.WeatherResponse;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subscribers.DisposableSubscriber;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by andrew on 1/19/18.
 */

public class MainPresenter implements MainContract.Presenter, LocationContract.Receiver
{
	private final String TAG = "MainPresenter";
	private final String BASE_URL = "https://api.openweathermap.org/";

	private static MainPresenter instance = new MainPresenter();

	private Retrofit retrofit;
	private OpenWeatherService openWeatherService;
	private LocationContract.Service locationService;

	private MainContract.View mainView;
	private DisposableSubscriber<WeatherResponse> refreshDisposable;
	private CountDownTimer refreshLocationTimer;
	private CountDownTimer requestWeatherTimer;

	private MainPresenter()
	{
	}

	public static MainPresenter getInstance()
	{
		return instance;
	}

	public void setUp(Activity activity)
	{
		Log.d(TAG, "setUp");
		retrofit = new Retrofit.Builder()
				.baseUrl(BASE_URL + "data/")
				.addCallAdapterFactory(RxJava2CallAdapterFactory.create())
				.addConverterFactory(GsonConverterFactory.create())
				.build();
		openWeatherService = retrofit.create(OpenWeatherService.class);

		refreshLocationTimer = new CountDownTimer(60_000, 60_000)
		{
			@Override
			public void onTick(long millisUntilFinished)
			{
			}

			@Override
			public void onFinish()
			{
				if(mainView != null)
				{
					mainView.showLocationError();
				}
			}
		};
		requestWeatherTimer = new CountDownTimer(60_000, 60_000)
		{
			@Override
			public void onTick(long millisUntilFinished)
			{
			}

			@Override
			public void onFinish()
			{
				if(mainView != null)
				{
					mainView.showNetworkError();
				}
			}
		};

		locationService = new LocationService(activity);
	}


	/**
	 * Binds presenter with a view when resumed. The Presenter will perform initialization here.
	 *
	 * @param view the view associated with this presenter
	 */
	@Override
	public void takeView(MainContract.View view)
	{
		Log.d(TAG, "takeView");
		mainView = view;
		loadCachedWeather();
	}

	/**
	 * Drops the reference to the view when destroyed
	 */
	@Override
	public void dropView()
	{
		Log.d(TAG, "dropView");
		mainView = null;
//		locationService.disconnect();
	}

	@Override
	public void updateWeather()
	{
		loadCachedWeather();
	}

	@Override
	public void updateLocation()
	{
		if(refreshLocationTimer != null)
		{
			refreshLocationTimer.start();
		}
		locationService.getLastKnownLocation();
	}

	private void loadCachedWeather()
	{
		float[] location = null;
		if(mainView != null)
		{
			location = mainView.readLocationData();
			if(location != null && location.length == 2 && location[0] != 0 && location[1] != 0)
			{
				loadWeather(location[0], location[1]);
			}
			if(locationService != null)
			{
				locationService.getLastKnownLocation();
			}
		}
	}

	private void loadWeather(@NonNull Location location)
	{
		loadWeather(location.getLatitude(), location.getLongitude());
	}

	private void loadWeather(double latitude, double longitude)
	{
		loadWeather((float) latitude, (float) longitude);
	}

	private void loadWeather(float latitude, float longitude)
	{
		if(requestWeatherTimer != null)
		{
			requestWeatherTimer.start();
		}
		String lat = String.format(Locale.US, "%f", latitude);
		String lon = String.format(Locale.US, "%f", longitude);
		Log.d(TAG, String.format(Locale.US, "lat: %s, lon: %s", lat, lon));
		if(openWeatherService != null)
		{
			if(refreshDisposable != null)
			{
				refreshDisposable.dispose();
			}

			refreshDisposable =
					openWeatherService.getWeatherByCoordinates("15646a06818f61f7b8d7823ca833e1ce", lat, lon, "metric")
//							.delay(600, TimeUnit.MILLISECONDS)
							.subscribeOn(Schedulers.newThread())
							.observeOn(AndroidSchedulers.mainThread())
							.subscribeWith(new DisposableSubscriber<WeatherResponse>()
							{
								/**
								 * Data notification sent by the {@link Publisher} in response to requests to {@link Subscription#request(long)}.
								 *
								 * @param weatherResponse the element signaled
								 */
								@Override
								public void onNext(WeatherResponse weatherResponse)
								{
									Log.d(TAG, weatherResponse.toString());
									if(mainView != null)
									{
										mainView.showWeather(weatherResponse.getName(), weatherResponse.getWeather().get(0).getMain(), weatherResponse.getMain().getTemp());
										mainView.showIcon(String.format(Locale.US, "%s%s%s.png", BASE_URL, "img/w/", weatherResponse.getWeather().get(0).getIcon()));
									}
								}

								/**
								 * Failed terminal state.
								 * <p>
								 * No further events will be sent even if {@link Subscription#request(long)} is invoked again.
								 *
								 * @param t the throwable signaled
								 */
								@Override
								public void onError(Throwable t)
								{
									Log.d(TAG, t.toString());
									if(mainView != null)
									{
										mainView.showNetworkError();
									}
								}

								/**
								 * Successful terminal state.
								 * <p>
								 * No further events will be sent even if {@link Subscription#request(long)} is invoked again.
								 */
								@Override
								public void onComplete()
								{
									if(requestWeatherTimer != null)
									{
										requestWeatherTimer.cancel();
									}
								}
							});
		}
	}

	@Override
	public void onLocationUpdate(@Nullable Location location)
	{
		if(refreshLocationTimer != null)
		{
			refreshLocationTimer.cancel();
		}
		if(location != null)
		{
			loadWeather(location);
			if(mainView != null)
			{
				mainView.writeLocationData(location);
			}
		}
		else
		{
			if(mainView != null)
			{
				mainView.showLocationError();
			}
		}
	}

	@Override
	public void postError()
	{
		if(mainView != null)
		{
			mainView.showError();
		}
	}
}
