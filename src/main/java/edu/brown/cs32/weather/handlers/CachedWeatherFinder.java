package edu.brown.cs32.weather.handlers;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import edu.brown.cs32.shared.APIFailureException;
import edu.brown.cs32.weather.WeatherData;
import edu.brown.cs32.weather.request.WeatherRequest;
import java.util.concurrent.TimeUnit;

/** Cached WeatherFinder that first look in the cache and then query NWS API if not found. */
public class CachedWeatherFinder implements Finder {
  private final Finder wrappedFinder;
  private final LoadingCache<WeatherRequest, WeatherData> cache;
  // variable to catch APIFailureException inside load
  private APIFailureException apiFailureException;

  public CachedWeatherFinder(Finder toWrap) {
    this.wrappedFinder = toWrap;
    this.cache =
        CacheBuilder.newBuilder()
            .maximumSize(100)
            .expireAfterAccess(60, TimeUnit.MINUTES)
            .recordStats()
            .build(
                new CacheLoader<WeatherRequest, WeatherData>() {
                  @Override
                  public WeatherData load(WeatherRequest weatherRequest) {
                    try {
                      return wrappedFinder.findWeather(weatherRequest);
                    } catch (APIFailureException e) {
                      // updates exception variable if caught
                      apiFailureException = e;
                      return null;
                    }
                  }
                });
  }

  /**
   * Overrides findeWeather to first look in the cache rather than directly calling NWS API
   *
   * @param weatherRequest
   * @return WeatherData object containing temperature, unit, retrievalDateTIme
   * @throws APIFailureException passed from load() inside CacheLoader
   */
  @Override
  public WeatherData findWeather(WeatherRequest weatherRequest) throws APIFailureException {
    try {
      return cache.getUnchecked(weatherRequest);
    } catch (Exception e) {
      throw apiFailureException;
    }
  }
}
