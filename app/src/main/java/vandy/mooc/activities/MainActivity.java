package vandy.mooc.activities;

import vandy.mooc.operations.WeatherOps;
import vandy.mooc.operations.WeatherOpsImpl;
import vandy.mooc.utils.RetainedFragmentManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

/**
 * The main Activity, prompting the user for a Weather location to
 * expand via WeatherServiceSync and WeatherServiceAsync, to view
 * in the UI. Extends LifecycleLoggingActivity so its lifecycle
 * hook methods are logged automatically.
 */
public class MainActivity extends LifecycleLoggingActivity {
	/**
	 * Used to retain the ImageOps state between runtime configuration changes.
	 */
	protected final RetainedFragmentManager mRetainedFragmentManager = new RetainedFragmentManager(this.getFragmentManager(), TAG);

	/**
	 * Provides weather-related operations.
	 */
	private WeatherOps mWeatherOps;

	/**
	 * Hook method called when a new instance of Activity is created.
	 * One-time initialisation goes here for runtime configuration changes.
	 *
	 * @param Bundle object that contains saved state information.
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// Always call super class for necessary
		// initialisation/implementation.
		super.onCreate(savedInstanceState);

		// Create the WeatherOps object one time.
		mWeatherOps = new WeatherOpsImpl(this);

		// Handle any configuration change.
		handleConfigurationChanges();
	}

	/**
	 * Hook method called by Android when Activity is destroyed.
	 */
	@Override
	protected void onDestroy() {
		// Unbind from the Service.
		mWeatherOps.unbindService();

		// Always call super class for necessary operations when an
		// Activity is destroyed.
		super.onDestroy();
	}

	/**
	 * Handle hardware reconfigurations, such as rotating the display.
	 */
	protected void handleConfigurationChanges() {
		// If this method returns true then this is the
		// first time the Activity has been created.
		if (mRetainedFragmentManager.firstTimeIn()) {
			Log.d(TAG, "First call of onCreate()");

			// Create the WeatherOps object one time.  The "true"
			// parameter instructs WeatherOps to use the
			// DownloadImagesBoundService.
			mWeatherOps = new WeatherOpsImpl(this);

			// Store the WeatherOps into the RetainedFragmentManager.
			mRetainedFragmentManager.put("WEATHER_OPS_STATE", mWeatherOps);

			// Initiate the service binding protocol (which may be a
			// no-op, depending on which type of service is used).
			mWeatherOps.bindService();
		} else {
			// The RetainedFragmentManager was previously initialised,
			// which means that a runtime configuration change occurred.

			Log.d(TAG, "Subsequent call of onCreate()");

			// Obtain the WeatherOps object from the
			// RetainedFragmentManager.
			mWeatherOps = mRetainedFragmentManager.get("WEATHER_OPS_STATE");

			// This check stops potential crash
			if (mWeatherOps == null) {
				// Create the WeatherOps object once. The "true"
				// parameter instructs WeatherOps to use the
				// bound service.
				mWeatherOps = new WeatherOpsImpl(this);

				// Store WeatherOps in RetainedFragmentManager.
				mRetainedFragmentManager.put("WEATHER_OPS_STATE", mWeatherOps);

				// Initiate the service binding protocol (which may be
				// a no-op, depending on which type of service is used).
				mWeatherOps.bindService();
			} else {
				// Inform it that the runtime configuration change has
				// completed.
				mWeatherOps.onConfigurationChange(this);
			}
		}
	}

	/*
	 * Initiate synchronous weather lookup when the user presses "Look Up Sync" button.
	 */
	public void expandCurrentWeatherSync(View v) {
		mWeatherOps.expandCurrentWeatherSync(v);
	}

	/*
	 * Initiate asynchronous weather lookup when the user presses "Look Up Async" button.
	 */
	public void expandCurrentWeatherAsync(View v) {
		mWeatherOps.expandCurrentWeatherAsync(v);
	}
}
