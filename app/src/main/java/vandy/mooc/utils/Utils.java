package vandy.mooc.utils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.WeakHashMap;

import vandy.mooc.aidl.WeatherData;
import vandy.mooc.jsonweather.JsonWeather;
import vandy.mooc.jsonweather.WeatherJSONParser;
import android.app.Activity;
import android.content.Context;
import android.os.IBinder;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

/**
 * @class WeatherDownloadUtils
 *
 * @brief Handles the downloading of Weather information from the Weather web service.
 */
public class Utils {

	private static final int MAX_TIME_TO_KEEP_CACHED = 10000;

	/**
	 * Weather result Cache
	 */
	private static final Map<String, CacheEntry> cacheMap = new WeakHashMap<>();

	/**
	 * Logging tag used by the debugger.
	 */
	private final static String TAG = Utils.class.getCanonicalName();

	/**
	 * URL to the Weather web service.
	 */
	private final static String weather_Web_Service_URL = "http://api.openweathermap.org/data/2.5/weather?units=metric&q=";

	/**
	 * Obtain the Weather information.
	 *
	 * @return The information that responds to your current weather search.
	 */
	public static synchronized WeatherData getResults(final String location) throws IllegalArgumentException {

		// check the cache first
		WeatherData result = getResultFromCache(location);
		if (result != null) {
			return result;
		}

		// A List of JsonWeather objects.
		JsonWeather jsonWeather = null;

		// Append the location to create the full URL.
		try {
			final URL url = new URL(weather_Web_Service_URL + location);
			Log.d(TAG, "Executing request: " + url);

			// Opens a connection to the Weather Service.
			HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

			// Sends the GET request and reads the Json results.
			try (InputStream in = new BufferedInputStream(urlConnection.getInputStream())) {
				// Create the parser.
				final WeatherJSONParser parser = new WeatherJSONParser();

				// Parse the Json results and create JsonWeather data
				// objects.
				jsonWeather = parser.parseJsonStream(in);
			} finally {
				urlConnection.disconnect();
			}
		} catch (IOException e) {
			Log.e(TAG, "Error running request", e);
		}

		if (jsonWeather != null) {
			// Convert the JsonWeather data objects to our WeatherData
			// object, which can be passed between processes.
			if (jsonWeather.getCod() == 200) {
				result = new WeatherData(//
						jsonWeather.getName(), //
						jsonWeather.getWeather().get(0).getIcon(), //
						jsonWeather.getWind().getSpeed(), //
						jsonWeather.getWind().getDeg(), //
						jsonWeather.getMain().getTemp(), //
						jsonWeather.getMain().getPressure(), //
						jsonWeather.getMain().getHumidity(), //
						jsonWeather.getSys().getSunrise(), //
						jsonWeather.getSys().getSunset() //
				);

				// cache the result
				cacheResult(location, result);
			} else {
				throw new IllegalArgumentException(jsonWeather.getMessage());
			}
		}
		// Return the List of WeatherData.
		return result;
	}

	private static synchronized WeatherData getResultFromCache(String location) {
		location = location.trim().toLowerCase();
		CacheEntry entry = cacheMap.get(location);
		if (entry != null) {
			entry.getData().mCached = true;
			if (System.currentTimeMillis() - entry.getTimestamp() <= MAX_TIME_TO_KEEP_CACHED) {
				Log.d(TAG, "Weather data found in cache: UPDATE");
				return entry.getData();
			}
			Log.d(TAG, "Weather data found in cache: STALE");
		} else {
			Log.d(TAG, "Weather data not found in cache.");
		}
		return null;
	}

	private static synchronized void cacheResult(String location, WeatherData data) {
		location = location.trim().toLowerCase();
		cacheMap.put(location, new CacheEntry(System.currentTimeMillis(), data));
	}

	/**
	 * This method is used to hide a keyboard after a user has finished typing the url.
	 */
	public static void hideKeyboard(Activity activity, IBinder windowToken) {
		InputMethodManager mgr = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
		mgr.hideSoftInputFromWindow(windowToken, 0);
	}

	/**
	 * Show a toast message.
	 */
	public static void showToast(Context context, String message) {
		Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
	}

	/**
	 * Ensure this class is only used as a utility.
	 */
	private Utils() {
		throw new AssertionError();
	}
}
