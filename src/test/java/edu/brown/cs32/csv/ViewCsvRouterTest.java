package edu.brown.cs32.csv;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import com.squareup.moshi.Moshi;
import edu.brown.cs32.IntegrationTestSetUpTearDown;
import edu.brown.cs32.csv.response.CsvDataResponse;
import edu.brown.cs32.shared.ResultType;
import java.io.IOException;
import java.net.HttpURLConnection;
import okio.Buffer;
import org.junit.jupiter.api.Test;

public class ViewCsvRouterTest extends IntegrationTestSetUpTearDown {
  /** Test if viewcsv endpoint is correctly routed. * */
  @Test
  public void testWebRequestLoadCsv() {
    try {
      HttpURLConnection conn = tryRequest("/viewcsv");
      assertEquals(200, conn.getResponseCode());
      conn.disconnect();
    } catch (IOException e) {
      fail(e);
    }
  }

  /** Test if viewcsv handles error when no csv is loaded. * */
  @Test
  public void testWebRequestViewCsvFileNotLoaded() {
    try {
      HttpURLConnection conn = tryRequest("/viewcsv");
      assertEquals(200, conn.getResponseCode());
      Moshi moshi = new Moshi.Builder().build();
      CsvDataResponse response =
          moshi
              .adapter(CsvDataResponse.class)
              .fromJson(new Buffer().readFrom(conn.getInputStream()));
      assertEquals(ResultType.error_datasource, response.result());
      conn.disconnect();
    } catch (IOException e) {
      fail(e);
    }
  }

  /** Test if viewcsv successfully returns the csv.* */
  @Test
  public void testWebRequestViewCsvAfterFileLoaded() {
    try {
      HttpURLConnection conn = tryRequest("/loadcsv?filepath=stars/ten-star.csv&header=true");
      // Get an OK response (the *connection* worked, the *API* provides an error response)
      assertEquals(200, conn.getResponseCode());
      conn.disconnect();
    } catch (IOException e) {
      fail(e);
    }
    try {
      HttpURLConnection conn = tryRequest("/viewcsv");
      assertEquals(200, conn.getResponseCode());
      Moshi moshi = new Moshi.Builder().build();
      CsvDataResponse response =
          moshi
              .adapter(CsvDataResponse.class)
              .fromJson(new Buffer().readFrom(conn.getInputStream()));
      assertEquals(ResultType.success, response.result());
      assertEquals(11, response.data().size());
      conn.disconnect();
    } catch (IOException e) {
      fail(e);
    }
  }
}
