package edu.brown.cs32.weather;

import static org.junit.jupiter.api.Assertions.*;

import edu.brown.cs32.shared.APIFailureException;
import edu.brown.cs32.weather.handlers.Finder;
import edu.brown.cs32.weather.handlers.WeatherFinder;
import edu.brown.cs32.weather.request.WeatherRequest;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.Test;

public class WeatherFinderUnitTest {
  @Test
  public void testWithMockData() {
    WeatherRequest request =
        new WeatherRequest(
            "35", "-78", OffsetDateTime.parse("2023-03-08T22:19:51.022539200+08:00"));
    WeatherData response = new WeatherData("58", "F", "2023-03-08T22:19:51.022539200+08:00");
    Finder weatherFinder = new WeatherFinder();
    try {
      // first request,will call and cache.
      WeatherData responseActual = weatherFinder.findWeather(request);
      assertEquals(response.temperature(), responseActual.temperature());
      assertEquals(response.temperatureUnit(), responseActual.temperatureUnit());
    } catch (APIFailureException e) {
      fail(e);
    }
  }

  @Test
  public void testWithOutMockData() {
    WeatherRequest request = new WeatherRequest("35", "-78", OffsetDateTime.now());
    Finder weatherFinder = new WeatherFinder();
    assertDoesNotThrow(
        () -> {
          weatherFinder.findWeather(request);
        });
  }

  @Test
  public void testWithOutMockDataThrowException() {
    WeatherRequest request = new WeatherRequest("xxx", "-78", OffsetDateTime.now());
    Finder weatherFinder = new WeatherFinder();
    assertThrows(
        APIFailureException.class,
        () -> {
          WeatherData responseActual = weatherFinder.findWeather(request);
          System.out.println(responseActual);
        });
  }

  @Test
  public void testWithOutMockDataThrowException2() {
    WeatherRequest request = new WeatherRequest("35", "xxx", OffsetDateTime.now());
    Finder weatherFinder = new WeatherFinder();
    assertThrows(
        APIFailureException.class,
        () -> {
          WeatherData responseActual = weatherFinder.findWeather(request);
          System.out.println(responseActual);
        });
  }
}
