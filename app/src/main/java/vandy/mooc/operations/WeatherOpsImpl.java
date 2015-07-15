package vandy.mooc.operations;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;

import vandy.mooc.R;
import vandy.mooc.activities.MainActivity;
import vandy.mooc.aidl.WeatherCall;
import vandy.mooc.aidl.WeatherData;
import vandy.mooc.aidl.WeatherRequest;
import vandy.mooc.aidl.WeatherResults;
import vandy.mooc.services.WeatherServiceAsync;
import vandy.mooc.services.WeatherServiceSync;
import vandy.mooc.utils.GenericServiceConnection;
import vandy.mooc.utils.Utils;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Implementation of all weather-related operations defined by WeatherOps interface.
 */
public class WeatherOpsImpl implements WeatherOps {
	/**
	 * Debugging tag used by the Android logger.
	 */
	protected final String TAG = getClass().getSimpleName();

	/**
	 * Used to enable garbage collection.
	 */
	protected WeakReference<MainActivity> mActivity;

	/**
	 * Weather entered by the user.
	 */
	protected WeakReference<EditText> mQuery;

	private WeakReference<TextView> mCached;

	private WeakReference<TextView> mCity;

	private WeakReference<TextView> mTemperature;

	private WeakReference<TextView> mHumidity;

	private WeakReference<TextView> mPresure;

	private WeakReference<TextView> mWind;

	private WeakReference<ImageView> mIcon;

	/**
	 * List of results to display (if any).
	 */
	protected WeatherData mResults;

	/**
	 * This GenericServiceConnection is used to receive results
	 * after binding to WeatherServiceSync Service using bindService().
	 */
	private GenericServiceConnection<WeatherCall> mServiceConnectionSync;

	/**
	 * This GenericServiceConnection is used to receive results
	 * after binding to WeatherServiceAsync Service using bindService().
	 */
	private GenericServiceConnection<WeatherRequest> mServiceConnectionAsync;

	/**
	 * Handler used to post runnables to UI from mWeatherResults callback methods.
	 * Stops dependency on the Activity, which may be destroyed in the UI Thread
	 * during a runtime configuration change.
	 */
	private final Handler mDisplayHandler = new Handler();

	/**
	 * The implementation of the WeatherResults AIDL Interface, which will be passed
	 * to the Weather Web service using WeatherRequest.expandWeather() method.
	 *
	 * WeatherResults.Stub is Invoker of the  Broker Pattern by implementing sendResults().
	 */
	private final WeatherResults.Stub mWeatherResults = new WeatherResults.Stub() {
		/**
		 * This method is invoked by the WeatherServiceAsync to
		 * return results to WeatherActivity.
		 */
		@Override
		public void sendResults(final WeatherData weatherData) throws RemoteException {
			// Since the Android Binder framework dispatches this
			// method in a background Thread we need to explicitly
			// post a runnable containing the results to the UI
			// Thread, where it's displayed.  We use the
			// mDisplayHandler to avoid a dependency on the
			// Activity, which may be destroyed in the UI Thread
			// during a runtime configuration change.
			mDisplayHandler.post(new Runnable() {
				@Override
				public void run() {
					displayResults(weatherData);
					if (mProgress != null) {
						mProgress.dismiss();
						mProgress = null;
					}
				}
			});
		}

		/**
		 * This method is invoked by the WeatherServiceAsync to
		 * return errors to WeatherActivity.
		 */
		@Override
		public void sendError(final String reason) throws RemoteException {
			// Since the Android Binder framework dispatches this
			// method in a background Thread we need to explicitly
			// post a runnable containing the results to the UI
			// Thread, where it's displayed. We use the
			// mDisplayHandler to avoid a dependency on the
			// Activity, which may be destroyed in the UI Thread
			// during a runtime configuration change.
			mDisplayHandler.post(new Runnable() {
				@Override
				public void run() {
					if (mProgress != null) {
						mProgress.dismiss();
						mProgress = null;
					}
					Utils.showToast(mActivity.get(), reason);
				}
			});
		}
	};

	private ProgressDialog mProgress;

	/**
	 * Constructor initialises the fields.
	 */
	public WeatherOpsImpl(MainActivity activity) {
		// Initialise the WeakReference.
		mActivity = new WeakReference<>(activity);

		// Finish the initialisation steps.
		initializeViewFields();
		initializeNonViewFields();
	}

	/**
	 * Initialise the View fields, which are all stored
	 * as WeakReferences for garbage collection purposes.
	 */
	private void initializeViewFields() {
		// Get references to the UI components.
		MainActivity mainActivity = mActivity.get();

		mainActivity.setContentView(R.layout.main_activity);

		// Store the EditText that holds the urls entered by the user
		// (if any).
		mQuery = new WeakReference<>((EditText) mainActivity.findViewById(R.id.edtLocation));
		mCached = new WeakReference<>((TextView) mainActivity.findViewById(R.id.cached));
		mCity = new WeakReference<>((TextView) mainActivity.findViewById(R.id.city));
		mTemperature = new WeakReference<>((TextView) mainActivity.findViewById(R.id.temperature));
		mHumidity = new WeakReference<>((TextView) mainActivity.findViewById(R.id.humidity));
		mPresure = new WeakReference<>((TextView) mainActivity.findViewById(R.id.presure));
		mWind = new WeakReference<>((TextView) mainActivity.findViewById(R.id.wind));
		mIcon = new WeakReference<>((ImageView) mainActivity.findViewById(R.id.icon));

		// Display results, if any (may not due to runtime configuration change).
		if (mResults != null) {
			displayResults(mResults);
		}
	}

	/**
	 * (Re)initialise the non-view fields (GenericServiceConnection objects).
	 */
	private void initializeNonViewFields() {
		mServiceConnectionSync = new GenericServiceConnection<WeatherCall>(WeatherCall.class);

		mServiceConnectionAsync = new GenericServiceConnection<WeatherRequest>(WeatherRequest.class);
	}

	/**
	 * Initiate the service binding protocol.
	 */
	@Override
	public void bindService() {
		Log.d(TAG, "calling bindService()");

		// Launch the Weather Bound Services if they aren't already
		// running via a call to bindService(), which binds this
		// activity to the WeatherService if they aren't already bound.
		if (mServiceConnectionSync.getInterface() == null) {
			mActivity.get().getApplicationContext().bindService(WeatherServiceSync.makeIntent(mActivity.get()), mServiceConnectionSync, Context.BIND_AUTO_CREATE);
		}

		if (mServiceConnectionAsync.getInterface() == null) {
			mActivity.get().getApplicationContext().bindService(WeatherServiceAsync.makeIntent(mActivity.get()), mServiceConnectionAsync, Context.BIND_AUTO_CREATE);
		}
	}

	/**
	 * Initiate the service unbinding protocol.
	 */
	@Override
	public void unbindService() {
		if (mActivity.get().isChangingConfigurations()) {
			Log.d(TAG, "Configuration change - unbindService() not called");
		} else {
			Log.d(TAG, "Calling unbindService()");

			// Unbind the Async Service if it is connected.
			if (mServiceConnectionAsync.getInterface() != null) {
				mActivity.get().getApplicationContext().unbindService(mServiceConnectionAsync);
			}

			// Unbind the Sync Service if it is connected.
			if (mServiceConnectionSync.getInterface() != null) {
				mActivity.get().getApplicationContext().unbindService(mServiceConnectionSync);
			}
		}
	}

	/**
	 * Called after a runtime configuration change occurs to finish the initialisation steps.
	 */
	@Override
	public void onConfigurationChange(MainActivity activity) {
		Log.d(TAG, "onConfigurationChange() called");

		if (mProgress != null) {
			mProgress.dismiss();
			mProgress = null;
		}

		// Reset the mActivity WeakReference.
		mActivity = new WeakReference<>(activity);

		// (Re)initialise all the View fields.
		initializeViewFields();
	}

	/*
	 * Initiate the asynchronous weather lookup when the user presses "Look Up Async".
	 */
	@Override
	public void expandCurrentWeatherAsync(View v) {
		final WeatherRequest weatherRequest = mServiceConnectionAsync.getInterface();

		if (weatherRequest != null) {
			// Get the weather entered by the user.
			final String weather = mQuery.get().getText().toString();

			resetDisplay();
			mProgress = ProgressDialog.show(mActivity.get(), "Loading Weather Async", weather, true);

			try {
				// Invoke a one-way AIDL call, which does not block
				// the client.  The results are returned via the
				// sendResults() method of the mWeatherResults
				// callback object, which runs in a Thread from the
				// Thread pool managed by the Binder framework.
				weatherRequest.getCurrentWeather(weather, mWeatherResults);
			} catch (RemoteException e) {
				Log.e(TAG, "RemoteException:" + e.getMessage());
			}
		} else {
			Log.d(TAG, "weatherRequest was null.");
		}
	}

	/*
	 * Initiate the synchronous weather lookup when the user presses "Look Up Sync".
	 */
	@Override
	public void expandCurrentWeatherSync(View v) {
		final WeatherCall weatherCall = mServiceConnectionSync.getInterface();

		if (weatherCall != null) {
			// Get the weather entered by the user.
			final String weather = mQuery.get().getText().toString();

			resetDisplay();

			// Use an anonymous AsyncTask to download the Weather data
			// in a separate thread and then display any results in
			// the UI thread.
			new AsyncTask<String, Void, WeatherData>() {
				/**
				 * Weather we're trying to expand.
				 */
				private String mWeather;

				@Override
				protected void onPreExecute() {
					mProgress = ProgressDialog.show(mActivity.get(), "Loading Weather Sync", weather, true);
				}

				/**
				 * Retrieve the expanded weather results via a synchronous two-way method call,
				 * which runs in a background thread to avoid blocking the UI thread.
				 */
				@Override
				protected WeatherData doInBackground(String... weathers) {
					try {
						mWeather = weathers[0];
						return weatherCall.getCurrentWeather(mWeather);
					} catch (RemoteException e) {
						e.printStackTrace();
					}
					return null;
				}

				/**
				 * Display the results in the UI Thread.
				 */
				@Override
				protected void onPostExecute(WeatherData weatherData) {
					if (weatherData != null) {
						displayResults(weatherData);
					} else {
						Utils.showToast(mActivity.get(), "no expansions for " + mWeather + " found");
					}
					if (mProgress != null) {
						mProgress.dismiss();
						mProgress = null;
					}
				}
				// Execute the AsyncTask to expand the weather without blocking the caller.
			}.execute(weather);
		} else {
			Log.d(TAG, "mWeatherCall was null.");
		}
	}

	/**
	 * Display the results to the screen.
	 *
	 * @param results List of Results to be displayed.
	 */
	private void displayResults(WeatherData results) {
		mResults = results;

		if (mResults != null) {

			mCached.get().setText("Cached result: " + mResults.mCached);
			mCity.get().setText("City: " + mResults.mName);
			mIcon.get().setImageResource(getIconId(mResults.mIcon));
			mTemperature.get().setText("Temperature: " + mResults.mTemp + "� C");
			mHumidity.get().setText("Humidity: " + mResults.mHumidity + " %");
			mPresure.get().setText("Pressure: " + mResults.mPressure + " hPa");
			mWind.get().setText("Wind: " + mResults.mSpeed + " km/h " + getWindDirection(mResults.mDeg));
		}
	}

	private int getIconId(String icon) {
		try {
			Field field = R.drawable.class.getField("icon_" + icon);
			return (int) field.get(null);
		} catch (NoSuchFieldException | IllegalAccessException | IllegalArgumentException e) {
			e.printStackTrace();
		}
		return 0;
	}

	private String getWindDirection(double deg) {
		/*
		N	348.75 - 11.25
		NNE	11.25 - 33.75
		NE	33.75 - 56.25
		ENE	56.25 - 78.75
		E	78.75 - 101.25
		ESE	101.25 - 123.75
		SE	123.75 - 146.25
		SSE	146.25 - 168.75
		S	168.75 - 191.25
		SSW	191.25 - 213.75
		SW	213.75 - 236.25
		WSW	236.25 - 258.75
		W	258.75 - 281.25
		WNW	281.25 - 303.75
		NW	303.75 - 326.25
		NNW	326.25 - 348.75
		 */

		if (between(deg, 348.75, 11.25)) {
			return "N";
		}
		if (between(deg, 11.25, 33.75)) {
			return "NNE";
		}
		if (between(deg, 33.75, 56.25)) {
			return "NE";
		}
		if (between(deg, 56.25, 78.75)) {
			return "ENE";
		}
		if (between(deg, 78.75, 101.25)) {
			return "E";
		}
		if (between(deg, 101.25, 123.75)) {
			return "ESE";
		}
		if (between(deg, 123.75, 146.25)) {
			return "SE";
		}
		if (between(deg, 146.25, 168.75)) {
			return "SSE";
		}
		if (between(deg, 168.75, 191.25)) {
			return "S";
		}
		if (between(deg, 191.25, 213.75)) {
			return "SSW";
		}
		if (between(deg, 213.75, 236.25)) {
			return "SW";
		}
		if (between(deg, 236.25, 258.75)) {
			return "WSW";
		}
		if (between(deg, 258.75, 281.25)) {
			return "W";
		}
		if (between(deg, 281.25, 303.75)) {
			return "WNW";
		}
		if (between(deg, 303.75, 326.25)) {
			return "NW";
		}
		if (between(deg, 326.25, 348.75)) {
			return "NNW";
		}
		return "";
	}

	private boolean between(double deg, double start, double end) {
		return deg >= start && deg <= end;
	}

	/**
	 * Reset the display prior to attempting to expand a new weather.
	 */
	private void resetDisplay() {
		Utils.hideKeyboard(mActivity.get(), mQuery.get().getWindowToken());
		mResults = null;
		if (mProgress != null) {
			mProgress.dismiss();
			mProgress = null;
		}

		mCached.get().setText("");
		mCity.get().setText("");
		mTemperature.get().setText("");
		mHumidity.get().setText("");
		mPresure.get().setText("");
		mWind.get().setText("");
		mIcon.get().setImageResource(0);

	}
}
