package edu.brown.cs32.weather;

import static org.junit.jupiter.api.Assertions.*;

import edu.brown.cs32.weather.handlers.WeatherFinder;
import java.net.URL;
import org.junit.jupiter.api.Test;

public class NwsApiUnitTest {
  @Test
  public void testReturnJsonObject01() {
    WeatherFinder finder = new WeatherFinder();
    assertDoesNotThrow(
        () -> {
          finder.getJsonFromUrl(new URL("https://api.weather.gov/points/35,-78"));
        });
  }

  @Test
  public void testReturnJsonObject02() {
    WeatherFinder finder = new WeatherFinder();
    assertDoesNotThrow(
        () -> {
          finder.getJsonFromUrl(new URL("https://api.weather.gov/gridpoints/MHX/10,32/forecast"));
        });
  }
}
