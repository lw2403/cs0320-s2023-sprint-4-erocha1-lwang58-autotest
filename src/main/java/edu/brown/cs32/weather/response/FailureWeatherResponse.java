package edu.brown.cs32.weather.response;

import edu.brown.cs32.shared.APIResponse;
import edu.brown.cs32.shared.ResultType;
import java.util.Map;

public record FailureWeatherResponse(
    ResultType result, String message, Map<String, String[]> params) implements APIResponse {}
