package eu.vmpay.weathermate.utils.location;

/**
 * Created by andrew on 1/19/18.
 */

public interface LocationContract
{
	void connect();

	void getLastKnownLocation();

	void disconnect();
}
