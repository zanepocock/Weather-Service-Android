package vandy.mooc.jsonweather;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import android.util.JsonReader;
import android.util.Log;

/**
 * Parses the Json weather data returned from the Weather Services API and returns a List of JsonWeather objects that
 * contain this data.
 */
public class WeatherJSONParser {
	/**
	 * Used for logging purposes.
	 */
	private final String TAG = this.getClass().getCanonicalName();

	/**
	 * Parse the @a inputStream and convert it into a List of JsonWeather objects.
	 */
	public JsonWeather parseJsonStream(InputStream inputStream) throws IOException {
		try (JsonReader reader = new JsonReader(new InputStreamReader(inputStream, "UTF-8"))) {
			Log.d(TAG, "Parsing the results returned as an array");

			// Handle the array returned from the Acronym Service.
			return parseJsonWeather(reader);
		}
	}

	/**
	 * Parse a Json stream and return a JsonWeather object.
	 */
	public JsonWeather parseJsonWeather(JsonReader reader) throws IOException {
		reader.beginObject();

		JsonWeather weather = new JsonWeather();
		try {
			while (reader.hasNext()) {
				String name = reader.nextName();
				switch (name) {
					case JsonWeather.sys_JSON:
						weather.setSys(parseSys(reader));
						Log.d(TAG, "reading " + name + ": " + weather.getSys());
						break;
					case JsonWeather.weather_JSON:
						weather.setWeather(parseWeathers(reader));
						Log.d(TAG, "reading " + name + ": " + weather.getWeather());
						break;
					case JsonWeather.base_JSON:
						weather.setBase(reader.nextString());
						Log.d(TAG, "reading " + name + ": " + weather.getBase());
						break;
					case JsonWeather.main_JSON:
						weather.setMain(parseMain(reader));
						Log.d(TAG, "reading " + name + ": " + weather.getMain());
						break;
					case JsonWeather.wind_JSON:
						weather.setWind(parseWind(reader));
						Log.d(TAG, "reading " + name + ": " + weather.getWind());
						break;
					case JsonWeather.dt_JSON:
						weather.setDt(reader.nextLong());
						Log.d(TAG, "reading " + name + ": " + weather.getDt());
						break;
					case JsonWeather.id_JSON:
						weather.setDt(reader.nextLong());
						Log.d(TAG, "reading " + name + ": " + weather.getId());
						break;
					case JsonWeather.name_JSON:
						weather.setName(reader.nextString());
						Log.d(TAG, "reading " + name + ": " + weather.getName());
						break;
					case JsonWeather.cod_JSON:
						weather.setCod(reader.nextLong());
						Log.d(TAG, "reading " + name + ": " + weather.getCod());
						break;
					case JsonWeather.message_JSON:
						weather.setMessage(reader.nextString());
						Log.d(TAG, "reading " + name + ": " + weather.getMessage());
						break;
					default:
						reader.skipValue();
						Log.d(TAG, "ignoring " + name);
						break;
				}
			}
		} finally {
			reader.endObject();
		}
		return weather;
	}

	/**
	 * Parse a Json stream and return a List of Weather objects.
	 */
	public List<Weather> parseWeathers(JsonReader reader) throws IOException {

		Log.d(TAG, "reading lfs elements");

		reader.beginArray();

		try {
			List<Weather> weathers = new ArrayList<Weather>();

			while (reader.hasNext()) {
				weathers.add(parseWeather(reader));
			}

			return weathers;
		} finally {
			reader.endArray();
		}
	}

	/**
	 * Parse a Json stream and return a Weather object.
	 */
	public Weather parseWeather(JsonReader reader) throws IOException {
		reader.beginObject();

		Weather weather = new Weather();
		try {
			while (reader.hasNext()) {
				String name = reader.nextName();
				switch (name) {
					case Weather.id_JSON:
						weather.setId(reader.nextLong());
						Log.d(TAG, "reading " + name + ": " + weather.getId());
						break;
					case Weather.main_JSON:
						weather.setMain(reader.nextString());
						Log.d(TAG, "reading " + name + ": " + weather.getMain());
						break;
					case Weather.description_JSON:
						weather.setDescription(reader.nextString());
						Log.d(TAG, "reading " + name + ": " + weather.getDescription());
						break;
					case Weather.icon_JSON:
						weather.setIcon(reader.nextString());
						Log.d(TAG, "reading " + name + ": " + weather.getIcon());
						break;
					default:
						reader.skipValue();
						Log.d(TAG, "ignoring " + name);
						break;
				}
			}
		} finally {
			reader.endObject();
		}
		return weather;
	}

	/**
	 * Parse a Json stream and return a Main Object.
	 */
	public Main parseMain(JsonReader reader) throws IOException {
		reader.beginObject();

		Main main = new Main();
		try {
			while (reader.hasNext()) {
				String name = reader.nextName();
				switch (name) {
					case Main.temp_JSON:
						main.setTemp(reader.nextDouble());
						Log.d(TAG, "reading " + name + ": " + main.getTemp());
						break;
					case Main.tempMax_JSON:
						main.setTempMax(reader.nextDouble());
						Log.d(TAG, "reading " + name + ": " + main.getTempMax());
						break;
					case Main.tempMin_JSON:
						main.setTempMin(reader.nextDouble());
						Log.d(TAG, "reading " + name + ": " + main.getTempMin());
						break;
					case Main.pressure_JSON:
						main.setPressure(reader.nextDouble());
						Log.d(TAG, "reading " + name + ": " + main.getPressure());
						break;
					case Main.seaLevel_JSON:
						main.setSeaLevel(reader.nextDouble());
						Log.d(TAG, "reading " + name + ": " + main.getSeaLevel());
						break;
					case Main.grndLevel_JSON:
						main.setGrndLevel(reader.nextDouble());
						Log.d(TAG, "reading " + name + ": " + main.getGrndLevel());
						break;
					case Main.humidity_JSON:
						main.setHumidity(reader.nextLong());
						Log.d(TAG, "reading " + name + ": " + main.getHumidity());
						break;
					default:
						reader.skipValue();
						Log.d(TAG, "ignoring " + name);
						break;
				}
			}
		} finally {
			reader.endObject();
		}
		return main;
	}

	/**
	 * Parse a Json stream and return a Wind Object.
	 */
	public Wind parseWind(JsonReader reader) throws IOException {
		reader.beginObject();

		Wind wind = new Wind();
		try {
			while (reader.hasNext()) {
				String name = reader.nextName();
				switch (name) {
					case Wind.speed_JSON:
						wind.setSpeed(reader.nextDouble());
						Log.d(TAG, "reading " + name + ": " + wind.getSpeed());
						break;
					case Wind.deg_JSON:
						wind.setDeg(reader.nextDouble());
						Log.d(TAG, "reading " + name + ": " + wind.getDeg());
						break;
					default:
						reader.skipValue();
						Log.d(TAG, "ignoring " + name);
						break;
				}
			}
		} finally {
			reader.endObject();
		}
		return wind;
	}

	/**
	 * Parse a Json stream and return a Sys Object.
	 *
	 * @throws IOException
	 */
	public Sys parseSys(JsonReader reader) throws IOException {
		reader.beginObject();

		Sys sys = new Sys();
		try {
			while (reader.hasNext()) {
				String name = reader.nextName();
				switch (name) {
					case Sys.message_JSON:
						sys.setMessage(reader.nextDouble());
						Log.d(TAG, "reading " + name + ": " + sys.getMessage());
						break;
					case Sys.country_JSON:
						sys.setCountry(reader.nextString());
						Log.d(TAG, "reading " + name + ": " + sys.getCountry());
						break;
					case Sys.sunrise_JSON:
						sys.setSunrise(reader.nextLong());
						Log.d(TAG, "reading " + name + ": " + sys.getSunrise());
						break;
					case Sys.sunset_JSON:
						sys.setSunset(reader.nextLong());
						Log.d(TAG, "reading " + name + ": " + sys.getSunset());
						break;
					default:
						reader.skipValue();
						Log.d(TAG, "ignoring " + name);
						break;
				}
			}
		} finally {
			reader.endObject();
		}
		return sys;
	}
}
