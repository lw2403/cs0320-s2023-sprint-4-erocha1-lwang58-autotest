package edu.brown.cs32.fuzz;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GenRandomWeatherRequestHelper {
  private static final String ALPHABET =
      "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ!%@, ";
  // private static final String ALPHA =
  // "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

  private static double randomBetween(double minValue, double maxValue) {
    Random random = new Random();
    return random.nextDouble() * (maxValue - minValue) + minValue;
  }

  private static String genGoodLatitude() {
    return String.format("%.4f", randomBetween(35 - 1, 35 + 1));
  }

  private static String genGoodLongitude() {
    return String.format("%.4f", randomBetween(-78 - 1, -78 + 1));
  }

  private static String genBadLatitude() {
    return genRandomAlphaStr(6);
  }

  private static String genBadLongitude() {
    return genRandomAlphaStr(6);
  }

  private static String genRandomAlphaStr(int length) {
    Random random = new Random();
    StringBuilder result = new StringBuilder();
    for (int i = 0; i < length; i++) {
      int index = random.nextInt(ALPHABET.length());
      result.append(ALPHABET.charAt(index));
    }
    return result.toString();
  }

  public static List<String> genUrls(
      int goodCount, int randomBadCount, int empty1, int empty2, int bothEmpty) {
    String baseUrl = "weather";
    ArrayList<String> list =
        new ArrayList<>(goodCount + randomBadCount + empty1 + empty2 + bothEmpty);
    String dt = "&dt=" + OffsetDateTime.now(ZoneId.of("-06:00"));
    for (int i = 0; i < goodCount; i++) {
      String lat = genGoodLatitude();
      String lon = genGoodLongitude();
      String params = String.format("lat=%s&lon=%s", lat, lon);
      list.add(baseUrl + "?" + params + dt);
    }
    for (int i = 0; i < randomBadCount; i++) {
      String lat = genBadLatitude();
      String lon = genBadLongitude();
      String params = String.format("lat=%s&lon=%s", lat, lon);
      list.add(baseUrl + "?" + params + dt);
    }
    for (int i = 0; i < empty1; i++) {
      // String lat = genLatitude();
      String lon = genGoodLongitude();
      String params = String.format("lon=%s", lon);
      list.add(baseUrl + "?" + params + dt);
    }
    for (int i = 0; i < empty2; i++) {
      String lat = genGoodLatitude();
      // String lon = genGoodLongitude();
      String params = String.format("lat=%s", lat);
      list.add(baseUrl + "?" + params + dt);
    }
    for (int i = 0; i < bothEmpty; i++) {
      // String lat = genGoodLatitude();
      // String lon = genGoodLongitude();
      String params = String.format("");
      list.add(baseUrl + "?" + params + dt);
    }

    return list;
  }
}
