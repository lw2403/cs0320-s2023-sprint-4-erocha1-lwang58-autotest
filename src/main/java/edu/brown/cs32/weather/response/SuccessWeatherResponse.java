package edu.brown.cs32.weather.response;

import edu.brown.cs32.shared.APIResponse;
import edu.brown.cs32.shared.ResultType;
import edu.brown.cs32.weather.WeatherData;
import java.util.Map;

public record SuccessWeatherResponse(
    ResultType result, WeatherData data, Map<String, String[]> params) implements APIResponse {}
