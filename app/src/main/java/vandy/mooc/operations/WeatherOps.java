package vandy.mooc.operations;

import vandy.mooc.activities.MainActivity;
import android.view.View;

/**
 * This class defines weather-related operations.
 */
public interface WeatherOps {
	/**
	 * Initiate service binding protocol.
	 */
	public void bindService();

	/**
	 * Initiate the service unbinding protocol.
	 */
	public void unbindService();

	/*
	 * Initiate the synchronous weather lookup when the user presses
	 * the "Look Up Sync" button.
	 */
	public void expandCurrentWeatherSync(View v);

	/*
	 * Initiate the asynchronous weather lookup when the user presses
	 * the "Look Up Async" button.
	 */
	public void expandCurrentWeatherAsync(View v);

	/**
	 * Called after a runtime configuration change occurs to finish the initialization steps.
	 */
	public void onConfigurationChange(MainActivity activity);
}
