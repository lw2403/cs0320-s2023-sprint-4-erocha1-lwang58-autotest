package edu.brown.cs32.weather;

import static org.junit.jupiter.api.Assertions.*;

import edu.brown.cs32.shared.APIFailureException;
import edu.brown.cs32.weather.handlers.CachedWeatherFinder;
import edu.brown.cs32.weather.handlers.Finder;
import edu.brown.cs32.weather.handlers.MockWeatherFinder;
import edu.brown.cs32.weather.request.WeatherRequest;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.Test;

public class CachedWeatherFinderTest {
  /** UseMockWeatherFinder test cache if it correctly loads weatherData. */
  @Test
  public void testCache() {
    WeatherRequest requestA =
        new WeatherRequest("35", "-78", OffsetDateTime.parse("2023-03-05T06:00:00-06:00"));
    WeatherData expectedRespA = new WeatherData("41", "F", "2023-03-03T17:24:03.236140-05:00");
    WeatherRequest requestB = new WeatherRequest("10", "-78", OffsetDateTime.now());
    WeatherData expectedRespB = new WeatherData("74", "F", "2023-03-03T17:24:03.236140-05:00");
    Finder weatherFinder = new MockWeatherFinder(0);
    Finder cacheWeatherFinder = new CachedWeatherFinder(weatherFinder);
    try {
      // first request, will call and cache.
      WeatherData actualResp = cacheWeatherFinder.findWeather(requestA);
      assertEquals(expectedRespA.temperature(), actualResp.temperature());
      assertEquals(2, weatherFinder.getCallCount());
      // same request, use cache.
      WeatherData actualResp2 = cacheWeatherFinder.findWeather(requestA);
      assertEquals(expectedRespA.temperature(), actualResp2.temperature());
      assertEquals(2, weatherFinder.getCallCount());
      // same request, use cache.
      WeatherData actualResp3 = cacheWeatherFinder.findWeather(requestB);
      assertEquals(expectedRespB.temperature(), actualResp3.temperature());
      assertEquals(4, weatherFinder.getCallCount());
    } catch (APIFailureException e) {
      fail(e);
    }
  }

  /** UseMockWeatherFinder, test if exceptions are handled correctly. */
  @Test
  public void testCachedWithException() {
    WeatherRequest request = new WeatherRequest("-1000", "1000", OffsetDateTime.now());
    //    WeatherData response = new WeatherData("12.8", "F", "2023-03-05T06:00:00-06:00");
    Finder weatherFinder = new MockWeatherFinder();
    Finder cacheWeatherFinder = new CachedWeatherFinder(weatherFinder);
    assertThrows(
        APIFailureException.class,
        () -> {
          cacheWeatherFinder.findWeather(request);
        });
    assertEquals(1, weatherFinder.getCallCount());
    assertThrows(
        APIFailureException.class,
        () -> {
          cacheWeatherFinder.findWeather(request);
        });
    assertEquals(2, weatherFinder.getCallCount());
  }

  @Test
  public void testFindWeatherWithMockNwsApi() {
    WeatherRequest request = new WeatherRequest("35", "-78", OffsetDateTime.now());
    WeatherData expectedResp = new WeatherData("74", "F", "2023-03-05T06:00:00-06:00");
    Finder weatherFinder = new MockWeatherFinder();
    CachedWeatherFinder cacheWeatherFinder = new CachedWeatherFinder(weatherFinder);
    try {
      // first request, will call and cache.
      WeatherData actualResp = cacheWeatherFinder.findWeather(request);
      assertEquals(expectedResp.temperature(), actualResp.temperature());
      assertEquals(expectedResp.temperatureUnit(), actualResp.temperatureUnit());
      assertEquals(2, weatherFinder.getCallCount());
      // second,] same request, use cache.
      WeatherData actualResp2 = cacheWeatherFinder.findWeather(request);
      assertEquals(actualResp, actualResp2);
      assertEquals(2, weatherFinder.getCallCount());
    } catch (APIFailureException e) {
      fail(e);
    }
  }
}
