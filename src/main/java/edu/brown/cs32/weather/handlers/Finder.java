package edu.brown.cs32.weather.handlers;

import edu.brown.cs32.shared.APIFailureException;
import edu.brown.cs32.shared.ResultType;
import edu.brown.cs32.weather.WeatherData;
import edu.brown.cs32.weather.request.WeatherRequest;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Scanner;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/** Finder interface that takes care of findWeather both in deployment and mocking. */
public interface Finder {

  /**
   * find WeatherData based on correctly shaped WeatherRequest
   *
   * @param req WeatherRequest containing lat, lon, dt
   * @return WeatherData containing temp, unit, retrievalDatetime
   * @throws APIFailureException pass on to the router to generate error response
   */
  default WeatherData findWeather(WeatherRequest req) throws APIFailureException {
    // get dateTimeIndex based on NWS documentation
    int halfDaysOffset = getHalfDayOffset(req.dateTime());
    if (halfDaysOffset < 0 || halfDaysOffset > 13) {
      throw new APIFailureException(
          ResultType.error_bad_request,
          "InputDateTime is out of bounds, should be within 7 ahead of the currentDateTime.");
    }
    try {
      // fetching metadata
      URL metadataUrl =
          new URL(
              String.format(
                  "https://api.weather.gov/points/%s,%s", req.latitude(), req.longitude()));
      JSONObject metadataJson = getJsonFromUrl(metadataUrl);
      // fetching forecast
      URL forecastUrl =
          new URL(metadataJson.getJSONObject("properties").get("forecast").toString());
      JSONObject forecastJson = getJsonFromUrl(forecastUrl);
      // fetching details
      JSONArray periodsJson = forecastJson.getJSONObject("properties").getJSONArray("periods");
      JSONObject detailsJson = periodsJson.getJSONObject(halfDaysOffset);
      // fetching all the desired results
      String retrievalDateTime = OffsetDateTime.now().toString();
      String temperature = detailsJson.get("temperature").toString();
      String temperatureUnit = detailsJson.get("temperatureUnit").toString();
      // returning successful weather data
      return new WeatherData(temperature, temperatureUnit, retrievalDateTime);
    } catch (MalformedURLException | NullPointerException e) {
      throw new APIFailureException(
          ResultType.error_internal, "Failed to get forecast for the input coordinates.");
    } catch (JSONException e) {
      throw new APIFailureException(
          ResultType.error_internal, "Failed to get json object while accessing forecast.");
    } catch (IOException e) {
      throw new APIFailureException(
          ResultType.error_datasource, "Failed to establish connection to NWS API.");
    }
  }

  /**
   * Default getter that call NWS API and gets json object
   *
   * @param url NWS API URL
   * @return json object from the url
   * @throws IOException pass on to findWeather
   * @throws JSONException pass on to findWeather
   * @throws APIFailureException pass on to findWeather
   */
  default JSONObject getJsonFromUrl(URL url)
      throws IOException, JSONException, APIFailureException {
    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    conn.setRequestMethod("GET"); // GET
    // Set the Accept header to indicate that we want JSON response
    conn.setRequestProperty("Accept", "application/json");
    // Check the response code and read the response body as a JSON string
    int responseCode = conn.getResponseCode();
    if (responseCode == HttpURLConnection.HTTP_OK) {
      Scanner scanner = new Scanner(conn.getInputStream());
      String responseBody = scanner.useDelimiter("\\A").next();
      // Parse the JSON response into a JSONObject
      JSONObject jsonObject = new JSONObject(responseBody);
      conn.disconnect();
      return jsonObject;
    }
    conn.disconnect();
    return null;
  }

  /**
   * Gets the number of half day offsets, i.e. n half-days from current time
   *
   * @param inputDateTime from http request parameters
   * @return number of half-days offsets
   */
  default int getHalfDayOffset(OffsetDateTime inputDateTime) {
    // Get the current dateTime
    OffsetDateTime currentDateTime = OffsetDateTime.now();
    // Calculate the difference in days between the input dateTime and the current dateTime
    int halfDaysDiff = Math.toIntExact(currentDateTime.until(inputDateTime, ChronoUnit.HALF_DAYS));
    // Check if the input dateTime is within the next 7 days of the current dateTime
    return halfDaysDiff;
  }

  /**
   * placeholder used for testing cache with mocking
   *
   * @return
   */
  default int getCallCount() {
    return 0;
  }
}
