package eu.vmpay.weathermate.mainActivity;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import org.reactivestreams.Subscription;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

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

public class MainPresenter implements MainContract.Presenter
{
	private final String TAG = "MainPresenter";
	private final String BASE_URL = "https://api.openweathermap.org/";

	private static MainPresenter instance = new MainPresenter();

	private Retrofit retrofit;
	private OpenWeatherService openWeatherService;

	private MainContract.View mainView;
	private DisposableSubscriber<WeatherResponse> refreshDisposable;
	private FusedLocationProviderClient mFusedLocationClient;
	private Activity activity;
	private Location currentLocation;

	private MainPresenter()
	{
	}

	public static MainPresenter getInstance()
	{
		return instance;
	}

	public void setUp(Activity activity)
	{
		this.activity = activity;
		Log.d(TAG, "setUp");
		retrofit = new Retrofit.Builder()
				.baseUrl(BASE_URL + "data/")
				.addCallAdapterFactory(RxJava2CallAdapterFactory.create())
				.addConverterFactory(GsonConverterFactory.create())
				.build();
		openWeatherService = retrofit.create(OpenWeatherService.class);

		mFusedLocationClient = LocationServices.getFusedLocationProviderClient(activity);
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
		updateLocation();
	}

	/**
	 * Drops the reference to the view when destroyed
	 */
	@Override
	public void dropView()
	{
		Log.d(TAG, "dropView");
		mainView = null;
	}

	@Override
	public void updateWeather()
	{
		loadWeather();
	}

	@Override
	public void updateLocation()
	{
		if(ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
				&& ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
		{
			ActivityCompat.requestPermissions(activity, new String[] {
					Manifest.permission.ACCESS_COARSE_LOCATION }, 111);
			return;
		}
		mFusedLocationClient.requestLocationUpdates(new LocationRequest(), new LocationCallback(), null);

		mFusedLocationClient.getLastLocation()
				.addOnSuccessListener(new OnSuccessListener<Location>()
				{
					@Override
					public void onSuccess(Location location)
					{
						if(location != null)
						{
							Log.d(TAG, location.toString());
						}
						currentLocation = location;
						loadWeather();
					}
				})
				.addOnFailureListener(new OnFailureListener()
				{
					@Override
					public void onFailure(@NonNull Exception e)
					{
						Log.d(TAG, e.toString());
						currentLocation = null;
						loadWeather();
					}
				});
	}

	private void loadWeather()
	{
		String lat = "52.229816";
		String lon = "21.011761";
		if(currentLocation != null)
		{
			lat = String.format(Locale.US, "%f", currentLocation.getLatitude());
			lon = String.format(Locale.US, "%f", currentLocation.getLongitude());
		}
		Log.d(TAG, String.format(Locale.US, "lat: %s, lon: %s", lat, lon));
		if(openWeatherService != null)
		{
			if(refreshDisposable != null)
			{
				refreshDisposable.dispose();
			}

			refreshDisposable =
					openWeatherService.getWeatherByCoordinates("15646a06818f61f7b8d7823ca833e1ce", lat, lon, "metric")
							.delay(600, TimeUnit.MILLISECONDS)
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
								}
							});
		}
	}

}
