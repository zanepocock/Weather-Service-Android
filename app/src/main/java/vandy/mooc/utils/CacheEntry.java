package vandy.mooc.utils;

import vandy.mooc.aidl.WeatherData;

public class CacheEntry {

	private final long time;
	private final WeatherData data;

	public CacheEntry(long time, WeatherData data) {
		this.time = time;
		this.data = data;
	}

	public long getTimestamp() {
		return time;
	}

	public WeatherData getData() {
		return data;
	}

}
