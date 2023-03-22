package edu.brown.cs32.weather;

import edu.brown.cs32.shared.APIFailureException;
import edu.brown.cs32.shared.ResultType;
import edu.brown.cs32.weather.handlers.CachedWeatherFinder;
import edu.brown.cs32.weather.handlers.WeatherFinder;
import edu.brown.cs32.weather.request.WeatherRequest;
import edu.brown.cs32.weather.response.FailureWeatherResponse;
import edu.brown.cs32.weather.response.SuccessWeatherResponse;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.Map;
import spark.Request;
import spark.Response;
import spark.Route;

public class WeatherRouter implements Route {
  public WeatherRouter() {}

  /**
   * Handles weather endpoint Sets success response if no APIFailureException caught Sets failure
   * response otherwise
   *
   * @param request The request object providing information about the HTTP request
   * @param response The response object providing functionality for modifying the response
   * @return null
   */
  @Override
  public Object handle(Request request, Response response) {
    response.header("Content-Type", "application/json");
    // fetching parameters
    Map<String, String[]> parameters = request.queryMap().toMap();
    String latitude = request.queryParams("lat");
    String longitude = request.queryParams("lon");
    String dateTimeString = request.queryParams("dt");
    try {
      // checking parameters
      checkLatitude(latitude);
      checkLongitude(longitude);
      checkDateTime(dateTimeString);
      // finding weather from NWS API
      CachedWeatherFinder weatherFinder = new CachedWeatherFinder(new WeatherFinder());
      WeatherRequest weatherRequest =
          new WeatherRequest(latitude, longitude, OffsetDateTime.parse(dateTimeString));
      WeatherData weatherData = weatherFinder.findWeather(weatherRequest);
      // appending success response
      String successResponse =
          new SuccessWeatherResponse(ResultType.success, weatherData, parameters).serialize();
      response.body(successResponse);
    } catch (APIFailureException e) {
      // appending failure response
      String failureResponse =
          new FailureWeatherResponse(e.getResultType(), e.getMessage(), parameters).serialize();
      response.body(failureResponse);
    }
    return null;
  }

  /**
   * Checks if latitude parameter is null or NaN
   *
   * @param lat parameter from http request
   * @throws APIFailureException thrown if lat is null or Nan
   */
  private void checkLatitude(String lat) throws APIFailureException {
    if (lat == null) {
      throw new APIFailureException(ResultType.error_bad_request, "Latitude is not provided.");
    }
    try {
      Double.parseDouble(lat);
    } catch (NumberFormatException e) {
      throw new APIFailureException(ResultType.error_bad_request, "Latitude is not a number.");
    }
  }

  /**
   * Checks if longitude parameter is null or NaN
   *
   * @param lon parameter from http request
   * @throws APIFailureException thrown if lon is null or Nan
   */
  private void checkLongitude(String lon) throws APIFailureException {
    if (lon == null) {
      throw new APIFailureException(ResultType.error_bad_request, "Longitude is not provided.");
    }
    try {
      Double.parseDouble(lon);
    } catch (NumberFormatException e) {
      throw new APIFailureException(ResultType.error_bad_request, "Longitude is not a number.");
    }
  }

  /**
   * Checks if dateTime parameter is null or not in the correct format
   *
   * @param dt datetime prameter from http request
   * @throws APIFailureException thrown if dt is null or not in the correct format
   */
  private void checkDateTime(String dt) throws APIFailureException {
    if (dt == null) return;
    try {
      OffsetDateTime.parse(dt);
    } catch (DateTimeParseException e) {
      throw new APIFailureException(
          ResultType.error_bad_request, "Datetime is not in a valid format.");
    }
  }
}
