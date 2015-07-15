# Weather-Service-Android
Completed for Dr. Schmidt's 2015 Android Communication MOOC

The primary purpose of this assignment was to develop a complete Weather Service app, containing a WeatherActivity that uses two Android Bound Services--one implementing a synchronous AIDL-based two-way mechanism (WeatherServiceSync) and one implementing an asynchronous AIDL-based two-way mechanism (WeatherServiceAsync)--to obtain weather information from a Weather
Service web service.

The Bound Services required AIDL interfaces to specify the methods they support, which provide a location obtained via the WeatherActivity as a String (e.g. "Nashville, TN") and return a WeatherData object to the WeatherActivity that can be used to display various current weather-related information for that location.

 To avoid blocking the UI Thread in the WeatherActivity, the synchronous two-way mechanism uses an AsyncTask. 
 
Since weather data doesn't change instantaeously, the WeatherServiceSync and WeatherServiceAsync implement some of the caching pattern, whereby a call by WeatherActivity to getCurrentWeather() retrieves weather data from the WeatherService web service only every 10 seconds, regardless of the number of times getCurrentWeather() is called by the WeatherActivity. Any calls during the intervening time period are serviced from the cache.

To obtain weather-related information, the Bound Services parse the JSON data returned from making an HTTP GET request via the <a href="http://openweathermap.org/api">Open Weather Map API</a>. An example of the JSON data returned by API can be seen by opening <a href="http://api.openweathermap.org/data/2.5/weather?q=Nashville,TN">this link</a> in your browser.
