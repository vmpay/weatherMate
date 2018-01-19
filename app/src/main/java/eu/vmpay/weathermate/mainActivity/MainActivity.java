package eu.vmpay.weathermate.mainActivity;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.wearable.activity.WearableActivity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.Locale;

import eu.vmpay.weathermate.R;

public class MainActivity extends WearableActivity implements MainContract.View
{
	private final String TAG = "MainActivity";

	private MainContract.Presenter mainPresenter;

	private TextView tvCity;
	private TextView tvTemperature;
	private TextView tvDescription;
	private ProgressBar pbRefresh;
	private LinearLayout llWeather;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		tvCity = findViewById(R.id.tvCity);
		tvTemperature = findViewById(R.id.tvTemperature);
		tvDescription = findViewById(R.id.tvDescription);
		pbRefresh = findViewById(R.id.pbRefresh);
		llWeather = findViewById(R.id.llWeather);

		if(mainPresenter == null)
		{
			mainPresenter = MainPresenter.getInstance();
		}
		mainPresenter.setUp();

		// Enables Always-on
		setAmbientEnabled();
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		mainPresenter.takeView(this);
	}

	@Override
	protected void onDestroy()
	{
		mainPresenter.dropView();
		super.onDestroy();
	}

	@Override
	public void showNetworkError()
	{
		tvCity.setText(R.string.empty_string);
		tvTemperature.setText(R.string.network_error);
		tvDescription.setText(R.string.empty_string);
	}

	@Override
	public void showError()
	{
		tvCity.setText(R.string.empty_string);
		tvTemperature.setText(R.string.unknown_error);
		tvDescription.setText(R.string.empty_string);
	}

	@Override
	public void showWeather(@NonNull String city, @NonNull String shortDescription, @NonNull Double temperature)
	{

		tvCity.setText(city);
		tvTemperature.setText(String.format(Locale.US, "%.2f C\u00b0", temperature));
		tvDescription.setText(shortDescription);
		pbRefresh.setVisibility(View.GONE);
		llWeather.setVisibility(View.VISIBLE);
	}

	public void refresh(View view)
	{
		pbRefresh.setVisibility(View.VISIBLE);
		llWeather.setVisibility(View.GONE);
		mainPresenter.updateWeather();
	}
}
