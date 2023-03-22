package edu.brown.cs32.weather.request;

import java.time.OffsetDateTime;
import java.util.Objects;

public record WeatherRequest(String latitude, String longitude, OffsetDateTime dateTime) {

  /**
   * Overrides equals to check for content rather than identity
   *
   * @param o the reference object with which to compare.
   * @return
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof WeatherRequest)) return false;
    WeatherRequest that = (WeatherRequest) o;
    return Objects.equals(latitude, that.latitude)
        && Objects.equals(longitude, that.longitude)
        && Objects.equals(dateTime, that.dateTime);
  }

  /**
   * Overrides hashCode to hash the 3 parameters
   *
   * @return
   */
  @Override
  public int hashCode() {
    return Objects.hash(longitude, latitude, dateTime);
  }
}
