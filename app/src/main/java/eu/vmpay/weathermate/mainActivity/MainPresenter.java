package eu.vmpay.weathermate.mainActivity;

import android.util.Log;

import org.reactivestreams.Subscription;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

import eu.vmpay.weathermate.utils.OpenWeatherService;
import eu.vmpay.weathermate.utils.WeatherResponse;
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

	private MainPresenter()
	{
	}

	public static MainPresenter getInstance()
	{
		return instance;
	}

	public void setUp()
	{
		Log.d(TAG, "setUp");
		retrofit = new Retrofit.Builder()
				.baseUrl(BASE_URL + "data/")
				.addCallAdapterFactory(RxJava2CallAdapterFactory.create())
				.addConverterFactory(GsonConverterFactory.create())
				.build();
		openWeatherService = retrofit.create(OpenWeatherService.class);
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
		loadWeather();
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

	}

	private void loadWeather()
	{
		if(openWeatherService != null)
		{
			if(refreshDisposable != null)
			{
				refreshDisposable.dispose();
			}

			refreshDisposable =
					openWeatherService.getWeatherByCoordinates("15646a06818f61f7b8d7823ca833e1ce", "52.229816", "21.011761", "metric")
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
