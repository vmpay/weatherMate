package eu.vmpay.weathermate.utils.location;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.wearable.Wearable;

import eu.vmpay.weathermate.mainActivity.MainPresenter;

/**
 * Created by andrew on 1/19/18.
 */

public class LocationService implements LocationContract.Service, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
    private final String TAG = "LocationService";

    private final GoogleApiClient mGoogleApiClient;

    private LocationContract.Receiver receiver;
    private Activity activity;

    public LocationService(Activity activity) {
        this.activity = activity;

        if (receiver == null) {
            receiver = MainPresenter.Companion.getInstance();
        }

        mGoogleApiClient = new GoogleApiClient.Builder(activity)
                .addApi(LocationServices.API)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        connect();
    }

    @Override
    public void connect() {
        mGoogleApiClient.connect();
    }

    @Override
    public void disconnect() {
        if ((mGoogleApiClient != null) && (mGoogleApiClient.isConnected()) &&
                (mGoogleApiClient.isConnecting())) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "mGoogleApiClient onConnected");

        getLastKnownLocation();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "mGoogleApiClient onConnectionSuspended");

        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "mGoogleApiClient onConnectionFailed");
    }

    private boolean checkPermission() {
        // Get the last known location
//		if(ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            ActivityCompat.requestPermissions(activity, new String[]{
//					Manifest.permission.ACCESS_COARSE_LOCATION }, 11);
                    Manifest.permission.ACCESS_FINE_LOCATION}, 11);
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void getLastKnownLocation() {
//		if(!hasGps() && receiver != null)
//		{
//			Log.d(TAG, "No FEATURE_LOCATION_GPS detected");
//			receiver.postError();
//			return;
//		}

        /*
         * mGpsPermissionApproved covers 23+ (M+) style permissions. If that is already approved or
         * the device is pre-23, the app uses mSaveGpsLocation to save the user's location
         * preference.
         */

        if (!mGoogleApiClient.isConnected()) {
            return;
        }

        if (checkPermission()) {
            LocationRequest locationRequest = LocationRequest.create()
                    .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                    .setInterval(10_000)
                    .setFastestInterval(5_000);

            LocationServices.FusedLocationApi
                    .requestLocationUpdates(mGoogleApiClient, locationRequest, this)
                    .setResultCallback(new ResultCallback<Status>() {
                        @Override
                        public void onResult(Status status) {
                            if (status.getStatus().isSuccess()) {
                                Log.d(TAG, "Successfully requested location updates");
                            } else {
                                Log.e(TAG,
                                        "Failed in requesting location updates, "
                                                + "status code: "
                                                + status.getStatusCode() + ", message: " + status
                                                .getStatusMessage());
                            }
                        }
                    });
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "location " + location);
        if (receiver != null) {
            receiver.onLocationUpdate(location);
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
    }

    private boolean hasGps() {
        return activity != null && activity.getPackageManager().hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS);
    }

}
