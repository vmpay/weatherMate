package eu.vmpay.weathermate.mainActivity;

import android.app.Activity;
import android.location.Location;
import android.support.annotation.NonNull;

import eu.vmpay.weathermate.BasePresenter;
import eu.vmpay.weathermate.BaseView;

/**
 * Created by andrew on 1/19/18.
 */

public interface MainContract
{
	interface Presenter extends BasePresenter<View>
	{
		void setUp(Activity activity);

		void updateWeather();

		void updateLocation();
	}

	interface View extends BaseView<Presenter>
	{
		void showNetworkError();

		void showLocationError();

		void showError();

		void showWeather(@NonNull String city, @NonNull String shortDescription, @NonNull Double temperature);

		void showIcon(String url);

		boolean writeLocationData(@NonNull Location location);

		float[] readLocationData();
	}
}
