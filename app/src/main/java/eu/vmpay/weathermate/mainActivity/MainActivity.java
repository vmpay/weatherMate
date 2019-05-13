package eu.vmpay.weathermate.mainActivity;

import android.location.Location;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.squareup.picasso.Picasso;

import java.util.Locale;

import eu.vmpay.weathermate.R;
import eu.vmpay.weathermate.SharedPreferencesUtils;

public class MainActivity extends WearableActivity implements MainContract.View, SwipeRefreshLayout.OnRefreshListener, View.OnClickListener {
    private final String TAG = "MainActivity";

    private MainContract.Presenter mainPresenter;

    private TextView tvCity;
    private TextView tvTemperature;
    private ImageView ivWeather;
    private LinearLayout llWeather;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvCity = findViewById(R.id.tvCity);
        tvTemperature = findViewById(R.id.tvTemperature);
        ivWeather = findViewById(R.id.ivWeather);
        llWeather = findViewById(R.id.llWeather);
        swipeRefreshLayout = findViewById(R.id.swipeRefresh);

        llWeather.setOnClickListener(this);
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setOnRefreshListener(this);
            swipeRefreshLayout.setOnClickListener(this);
        }
        if (mainPresenter == null) {
            mainPresenter = MainPresenter.Companion.getInstance();
        }
        mainPresenter.setUp(this);

        // Enables Always-on
        setAmbientEnabled();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mainPresenter.takeView(this);
    }

    @Override
    protected void onPause() {
        mainPresenter.dropView();
        super.onPause();
    }

    @Override
    public void showNetworkError() {
        swipeRefreshLayout.setRefreshing(false);

        tvCity.setText(R.string.empty_string);
        tvTemperature.setText(R.string.network_error);
        ivWeather.setImageResource(R.mipmap.ic_launcher);
    }

    @Override
    public void showLocationError() {
        swipeRefreshLayout.setRefreshing(false);

        tvCity.setText(R.string.empty_string);
        tvTemperature.setText(R.string.location_error);
        ivWeather.setImageResource(R.mipmap.ic_launcher);
    }

    @Override
    public void showError() {
        swipeRefreshLayout.setRefreshing(false);

        tvCity.setText(R.string.empty_string);
        tvTemperature.setText(R.string.unknown_error);
        ivWeather.setImageResource(R.mipmap.ic_launcher);
    }

    @Override
    public void showWeather(@NonNull String city, @NonNull String shortDescription, @NonNull Double temperature) {
        tvCity.setText(city);
        tvTemperature.setText(String.format(Locale.US, "%.2f C\u00b0", temperature));

        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void showIcon(String url) {
        Log.d(TAG, "Url " + url);
        Picasso.get().load(url).into(ivWeather);
    }

    @Override
    public boolean writeLocationData(@NonNull Location location) {
        return SharedPreferencesUtils.writeLocationData(this, location);
    }

    @Override
    public float[] readLocationData() {
        return SharedPreferencesUtils.readLocationData(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        mainPresenter.updateLocation();
    }

    /**
     * Called when a swipe gesture triggers a refresh.
     */
    @Override
    public void onRefresh() {
        if (mainPresenter != null) {
            swipeRefreshLayout.setRefreshing(false);

            mainPresenter.updateWeather();
        }
    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        if (mainPresenter != null) {
            swipeRefreshLayout.setRefreshing(true);

            mainPresenter.updateLocation();
        }
    }
}
