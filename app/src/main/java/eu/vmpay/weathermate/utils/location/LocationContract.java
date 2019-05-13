package eu.vmpay.weathermate.utils.location;

import android.location.Location;
import androidx.annotation.Nullable;

/**
 * Created by andrew on 1/19/18.
 */

public interface LocationContract
{
	interface Service
	{
		void connect();

		void getLastKnownLocation();

		void disconnect();
	}

	interface Receiver
	{
		void onLocationUpdate(@Nullable Location location);

		void postError();
	}
}
