package eu.vmpay.weathermate;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;

/**
 * Created by andrew on 2/2/18.
 */

public class SharedPreferencesUtils
{
	private static final String LATITUDE_KEY = "latitude";
	private static final String LONGITUDE_KEY = "longitude";

	public static float readFloat(Activity activity, String key)
	{
		if(activity == null)
		{
			return 0;
		}
		SharedPreferences sharedPref = activity.getPreferences(Context.MODE_PRIVATE);
		return sharedPref.getFloat(key, 0);
	}

	public static boolean writeFloat(Activity activity, String key, float value)
	{
		if(activity == null)
		{
			return false;
		}
		SharedPreferences sharedPref = activity.getPreferences(Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPref.edit();
		editor.putFloat(key, value);
		return editor.commit();
	}

	/**
	 * @param activity is required to get access to {@link SharedPreferences}
	 * @return array of two elements in the strict oreder - result[0] = latitude, result[1] = longitude
	 */
	public static float[] readLocationData(Activity activity)
	{
		float[] result = new float[2];
		result[0] = readFloat(activity, LATITUDE_KEY);
		result[1] = readFloat(activity, LONGITUDE_KEY);
		return result;
	}

	/**
	 * @param activity  is required to get access to {@link SharedPreferences}
	 * @param latitude  float latitude value
	 * @param longitude float longitude value
	 * @return returns true if successfully written, and false otherwise
	 */
	public static boolean writeLocationData(Activity activity, float latitude, float longitude)
	{
		boolean isEverythingOk = true;
		if(!writeFloat(activity, LATITUDE_KEY, latitude))
		{
			isEverythingOk = false;
		}
		if(!writeFloat(activity, LONGITUDE_KEY, longitude))
		{
			isEverythingOk = false;
		}
		return isEverythingOk;
	}

	/**
	 * @param activity  is required to get access to {@link SharedPreferences}
	 * @param latitude  double latitude value
	 * @param longitude double longitude value
	 * @return returns true if successfully written, and false otherwise
	 */
	public static boolean writeLocationData(Activity activity, double latitude, double longitude)
	{
		return activity != null && writeLocationData(activity, (float) latitude, (float) longitude);
	}

	/**
	 * @param activity is required to get access to {@link SharedPreferences}
	 * @param location location object which contains latitude and longitude data
	 * @return
	 */
	public static boolean writeLocationData(Activity activity, Location location)
	{
		return activity != null && location != null && writeLocationData(activity, location.getLatitude(), location.getLongitude());
	}
}
