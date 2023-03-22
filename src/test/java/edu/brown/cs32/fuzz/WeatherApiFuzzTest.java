package edu.brown.cs32.fuzz;

import com.squareup.moshi.Moshi;
import edu.brown.cs32.IntegrationTestSetUpTearDown;
import edu.brown.cs32.shared.ResultType;
import edu.brown.cs32.weather.response.SuccessWeatherResponse;
import java.net.HttpURLConnection;
import java.util.List;
import okio.Buffer;
import org.junit.jupiter.api.Test;

public class WeatherApiFuzzTest extends IntegrationTestSetUpTearDown {
  @Test
  public void testUrl() {
    List<String> urls = GenRandomWeatherRequestHelper.genUrls(100, 20, 4, 4, 2);
    // List<String> urls = GenRandomWeatherRequestHelper.genUrls(100,0,0,0,0);
    int successCount = 0;
    int server500Count = 0;
    for (String url : urls) {
      boolean isSuccess = false;
      String result = "";
      try {
        HttpURLConnection clientConnection = tryRequest(url);
        // Get an OK response (the *connection* worked, the *API* provides an error response)
        if (clientConnection.getResponseCode() != 200) {
          isSuccess = false;
          server500Count++;
          result = "server return " + clientConnection.getResponseCode();
        } else {
          // Now we need to see whether we've got the expected Json response.
          // SoupAPIUtilities handles ingredient lists, but that's not what we've got here.
          Moshi moshi = new Moshi.Builder().build();
          // We'll use okio's Buffer class here
          SuccessWeatherResponse response =
              moshi
                  .adapter(SuccessWeatherResponse.class)
                  .fromJson(new Buffer().readFrom(clientConnection.getInputStream()));
          if (response.result() == ResultType.success) {
            isSuccess = true;
            result = "success";
          } else {
            isSuccess = false;
            result = response.result() + "";
          }
        }
        clientConnection.disconnect();
      } catch (Exception e) {
        // e.printStackTrace();
        result = e.getMessage();
      }
      System.out.println(url + ".test result:" + result + ".");
      if (isSuccess) successCount++;
    }
    System.out.println(
        "total:"
            + urls.size()
            + ". success:"
            + successCount
            + ". fail:"
            + (urls.size() - successCount)
            + ".");
  }
}
