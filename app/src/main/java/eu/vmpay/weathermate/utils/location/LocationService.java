package eu.vmpay.weathermate.utils.location;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

/**
 * Created by andrew on 1/19/18.
 */

public class LocationService implements LocationContract, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener
{
	private final String TAG = "LocationService";

	private final GoogleApiClient mGoogleApiClient;

	private FusedLocationProviderClient mFusedLocationClient;
	private Activity activity;

	private Location currentLocation;
	private LocationCallback mLocationCallback;
	private LocationRequest mLocationRequest;

	public LocationService(Activity activity)
	{
		this.activity = activity;

		mGoogleApiClient = new GoogleApiClient.Builder(activity)
				.addApi(LocationServices.API)
				.addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this)
				.build();

		mFusedLocationClient = LocationServices.getFusedLocationProviderClient(activity);

		mLocationCallback = new LocationCallback()
		{
			@Override
			public void onLocationResult(LocationResult locationResult)
			{
				for(Location location : locationResult.getLocations())
				{
					Log.d(TAG, "onLocationResult " + location);
					// Update UI with location data
					// ...
				}
				stopLocationUpdates();
			}
		};
		createLocationRequest();
	}

	@Override
	public void connect()
	{
		mGoogleApiClient.connect();
	}

	@Override
	public void disconnect()
	{
		if((mGoogleApiClient != null) && (mGoogleApiClient.isConnected()) &&
				(mGoogleApiClient.isConnecting()))
		{
			LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, mLocationCallback);
			mGoogleApiClient.disconnect();
		}
	}

	@Override
	public void onConnected(@Nullable Bundle bundle)
	{
		Log.d(TAG, "mGoogleApiClient onConnected");

//		LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
//				.addLocationRequest(mLocationRequest);
//
//		SettingsClient client = LocationServices.getSettingsClient(activity);
//		Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());
//
//		task.addOnSuccessListener(activity, new OnSuccessListener<LocationSettingsResponse>()
//		{
//			@Override
//			public void onSuccess(LocationSettingsResponse locationSettingsResponse)
//			{
//				// All location settings are satisfied. The client can initialize
//				// location requests here.
//				// ...
//				Log.d(TAG, "LocationSettingsResponse onSuccess");
//				getLastKnownLocation();
//			}
//		});
//
//		task.addOnFailureListener(activity, new OnFailureListener()
//		{
//			@Override
//			public void onFailure(@NonNull Exception e)
//			{
//				if(e instanceof ResolvableApiException)
//				{
//					Log.d(TAG, "LocationSettingsResponse onFailure " + e);
//					// Location settings are not satisfied, but this can be fixed
//					// by showing the user a dialog.
//					try
//					{
//						// Show the dialog by calling startResolutionForResult(),
//						// and check the result in onActivityResult().
//						ResolvableApiException resolvable = (ResolvableApiException) e;
//						resolvable.startResolutionForResult(activity,
//								12);
//					} catch(IntentSender.SendIntentException sendEx)
//					{
//						// Ignore the error.
//					}
//				}
//			}
//		});

		getLastKnownLocation();
	}

	@Override
	public void onConnectionSuspended(int i)
	{
		Log.d(TAG, "mGoogleApiClient onConnectionSuspended");

		LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, mLocationCallback);
	}

	@Override
	public void onConnectionFailed(@NonNull ConnectionResult connectionResult)
	{
		Log.d(TAG, "mGoogleApiClient onConnectionFailed");
	}

	private boolean checkPermission()
	{
		// Get the last known location
//		if(ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
		if(ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
		{
			// TODO: Consider calling
			//    ActivityCompat#requestPermissions
			// here to request the missing permissions, and then overriding
			//   public void onRequestPermissionsResult(int requestCode, String[] permissions,
			//                                          int[] grantResults)
			// to handle the case where the user grants the permission. See the documentation
			// for ActivityCompat#requestPermissions for more details.
			ActivityCompat.requestPermissions(activity, new String[] {
//					Manifest.permission.ACCESS_COARSE_LOCATION }, 11);
					Manifest.permission.ACCESS_FINE_LOCATION }, 11);
			return false;
		}
		else
		{
			return true;
		}
	}

	@Override
	public void getLastKnownLocation()
	{
		/*
		 * mGpsPermissionApproved covers 23+ (M+) style permissions. If that is already approved or
         * the device is pre-23, the app uses mSaveGpsLocation to save the user's location
         * preference.
         */
		if(checkPermission())
		{

			LocationRequest locationRequest = LocationRequest.create()
					.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
					.setInterval(10_000)
					.setFastestInterval(5_000);

			LocationServices.FusedLocationApi
					.requestLocationUpdates(mGoogleApiClient, locationRequest, this)
					.setResultCallback(new ResultCallback<Status>()
					{
						@Override
						public void onResult(Status status)
						{
							if(status.getStatus().isSuccess())
							{
								Log.d(TAG, "Successfully requested location updates");
							}
							else
							{
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

	private void startLocationUpdates()
	{
		if(checkPermission())
		{
			mFusedLocationClient.requestLocationUpdates(mLocationRequest,
					mLocationCallback,
					null /* Looper */);
		}
	}

	private void stopLocationUpdates()
	{
		mFusedLocationClient.removeLocationUpdates(mLocationCallback);
	}

	protected void createLocationRequest()
	{
		mLocationRequest = new LocationRequest();
		mLocationRequest.setInterval(10000);
		mLocationRequest.setFastestInterval(5000);
		mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
	}

	@Override
	public void onLocationChanged(Location location)
	{
		Log.d(TAG, "location " + location);
	}
}
