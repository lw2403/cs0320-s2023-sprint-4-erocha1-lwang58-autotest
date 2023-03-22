package edu.brown.cs32.weather;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import com.squareup.moshi.Moshi;
import edu.brown.cs32.IntegrationTestSetUpTearDown;
import edu.brown.cs32.csv.response.CsvMessageResponse;
import java.io.IOException;
import java.net.HttpURLConnection;
import okio.Buffer;
import org.junit.jupiter.api.Test;

public class ServerUrlNotFoundIntegrationTest extends IntegrationTestSetUpTearDown {
  @Test
  public void testWebRequestUrlNotFound() {
    try {
      HttpURLConnection clientConnection = tryRequest("/xxxx");
      // Get an OK response (the *connection* worked, the *API* provides an error response)
      assertEquals(200, clientConnection.getResponseCode());
      // Now we need to see whether we've got the expected Json response.
      // SoupAPIUtilities handles ingredient lists, but that's not what we've got here.
      Moshi moshi = new Moshi.Builder().build();
      // We'll use okio's Buffer class here
      CsvMessageResponse response =
          moshi
              .adapter(CsvMessageResponse.class)
              .fromJson(new Buffer().readFrom(clientConnection.getInputStream()));
      assertEquals("error_bad_json", response.result().toString());
      assertEquals("Unexpected endpoints.", response.message());
      clientConnection.disconnect();
    } catch (IOException e) {
      fail(e);
    }
  }
}
