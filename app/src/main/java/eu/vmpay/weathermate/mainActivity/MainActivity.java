package eu.vmpay.weathermate.mainActivity;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.Locale;

import eu.vmpay.weathermate.R;

public class MainActivity extends WearableActivity implements MainContract.View
{
	private final String TAG = "MainActivity";

	private MainContract.Presenter mainPresenter;

	private TextView tvCity;
	private TextView tvTemperature;
	private ImageView ivWeather;
	private ProgressBar pbRefresh;
	private LinearLayout llWeather;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		tvCity = findViewById(R.id.tvCity);
		tvTemperature = findViewById(R.id.tvTemperature);
		ivWeather = findViewById(R.id.ivWeather);
		pbRefresh = findViewById(R.id.pbRefresh);
		llWeather = findViewById(R.id.llWeather);

		if(mainPresenter == null)
		{
			mainPresenter = MainPresenter.getInstance();
		}
		mainPresenter.setUp(this);

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
		ivWeather.setImageResource(R.mipmap.ic_launcher);
	}

	@Override
	public void showError()
	{
		tvCity.setText(R.string.empty_string);
		tvTemperature.setText(R.string.unknown_error);
		ivWeather.setImageResource(R.mipmap.ic_launcher);
	}

	@Override
	public void showWeather(@NonNull String city, @NonNull String shortDescription, @NonNull Double temperature)
	{
		tvCity.setText(city);
		tvTemperature.setText(String.format(Locale.US, "%.2f C\u00b0", temperature));

		pbRefresh.setVisibility(View.GONE);
		llWeather.setVisibility(View.VISIBLE);
	}

	@Override
	public void showIcon(String url)
	{
		Log.d(TAG, "Url " + url);
		Picasso.with(this).load(url).into(ivWeather);
	}

	public void refresh(View view)
	{
		pbRefresh.setVisibility(View.VISIBLE);
		llWeather.setVisibility(View.GONE);

		mainPresenter.updateLocation();
	}

}
