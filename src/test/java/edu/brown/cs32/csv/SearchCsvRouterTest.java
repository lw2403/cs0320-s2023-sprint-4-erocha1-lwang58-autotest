package edu.brown.cs32.csv;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import com.squareup.moshi.Moshi;
import edu.brown.cs32.IntegrationTestSetUpTearDown;
import edu.brown.cs32.csv.response.CsvDataResponse;
import edu.brown.cs32.csv.response.CsvMessageResponse;
import edu.brown.cs32.shared.ResultType;
import java.io.IOException;
import java.net.HttpURLConnection;
import okio.Buffer;
import org.junit.jupiter.api.Test;

public class SearchCsvRouterTest extends IntegrationTestSetUpTearDown {
  /** Test if searchcsv endpoint is correctly routed. * */
  @Test
  public void testWebRequestLoadCsv() {
    try {
      HttpURLConnection conn = tryRequest("/searchcsv");
      assertEquals(200, conn.getResponseCode());
      conn.disconnect();
    } catch (IOException e) {
      fail(e);
    }
  }
  /** Test if searchcsv handles error when no csv is loaded. * */
  @Test
  public void testWebRequestSearchCsvFileNotLoaded() {
    try {
      HttpURLConnection conn = tryRequest("/searchcsv?query=Sol@ProperName&mode=fuzzy");
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

  /**
   * Helper function that runs loadcsv before each searchcsv
   *
   * @param filePath String of desired csv filepath
   * @param header "true" or "false" indicating if currentCsv has a header
   */
  private void loadCsv(String filePath, Boolean header) {
    try {
      HttpURLConnection conn = tryRequest("/loadcsv?filepath=" + filePath + "&header=" + header);
      assertEquals(200, conn.getResponseCode());
      Moshi moshi = new Moshi.Builder().build();
      CsvMessageResponse response =
          moshi
              .adapter(CsvMessageResponse.class)
              .fromJson(new Buffer().readFrom(conn.getInputStream()));
      conn.disconnect();
    } catch (IOException e) {
      fail(e);
    }
  }

  /** Test if searchcsv handles no parameters at all. * */
  @Test
  public void testSearchCsvFailWithNoParams() {
    loadCsv("stars/ten-star.csv", true);
    try {
      HttpURLConnection conn = tryRequest("/searchcsv");
      assertEquals(200, conn.getResponseCode());
      Moshi moshi = new Moshi.Builder().build();
      CsvDataResponse response =
          moshi
              .adapter(CsvDataResponse.class)
              .fromJson(new Buffer().readFrom(conn.getInputStream()));
      assertEquals(ResultType.error_bad_request, response.result());
      conn.disconnect();
    } catch (IOException e) {
      fail(e);
    }
  }

  /** Test if searchcsv handles missing mode parameter. * */
  @Test
  public void testSearchCsvFailWithNoModeParam() {
    loadCsv("stars/ten-star.csv", true);
    try {
      HttpURLConnection conn = tryRequest("/searchcsv?query=Rigel%20Kentaurus@1");
      assertEquals(200, conn.getResponseCode());
      Moshi moshi = new Moshi.Builder().build();
      CsvDataResponse response =
          moshi
              .adapter(CsvDataResponse.class)
              .fromJson(new Buffer().readFrom(conn.getInputStream()));

      assertEquals(ResultType.error_bad_request, response.result());
      conn.disconnect();
    } catch (IOException e) {
      fail(e);
    }
  }

  /** Test if searchcsv handles missing query parameter. * */
  @Test
  public void testSearchCsvFailWithNoQueryParam() {
    loadCsv("stars/ten-star.csv", true);
    try {
      HttpURLConnection conn = tryRequest("/searchcsv?mode=fuzzy");
      assertEquals(200, conn.getResponseCode());
      Moshi moshi = new Moshi.Builder().build();
      CsvDataResponse response =
          moshi
              .adapter(CsvDataResponse.class)
              .fromJson(new Buffer().readFrom(conn.getInputStream()));

      assertEquals(ResultType.error_bad_request, response.result());
      conn.disconnect();
    } catch (IOException e) {
      fail(e);
    }
  }

  /** Test if searchcsv handles incorrect column name. * */
  @Test
  public void testSearchCsvFailWithErrorColumnName() {
    loadCsv("stars/ten-star.csv", true);
    try {
      HttpURLConnection conn = tryRequest("/searchcsv?query=Sol@ProperNameXXX&mode=fuzzy");
      assertEquals(200, conn.getResponseCode());
      Moshi moshi = new Moshi.Builder().build();
      CsvDataResponse response =
          moshi
              .adapter(CsvDataResponse.class)
              .fromJson(new Buffer().readFrom(conn.getInputStream()));
      assertEquals(ResultType.error_bad_request, response.result());
      conn.disconnect();
    } catch (IOException e) {
      fail(e);
    }
  }

  /** Test if searchcsv handles incorrect column index. * */
  @Test
  public void testSearchCsvFailWithErrorColumnIndex() {
    loadCsv("stars/ten-star.csv", true);
    try {
      HttpURLConnection conn = tryRequest("/searchcsv?query=Sol@500&mode=fuzzy");
      assertEquals(200, conn.getResponseCode());
      Moshi moshi = new Moshi.Builder().build();
      CsvDataResponse response =
          moshi
              .adapter(CsvDataResponse.class)
              .fromJson(new Buffer().readFrom(conn.getInputStream()));
      assertEquals(ResultType.error_bad_request, response.result());
      conn.disconnect();
    } catch (IOException e) {
      fail(e);
    }
  }

  /** Test if searchcsv handles incorrect query expression. * */
  @Test
  public void testSearchCsvFailWithErrorExpression() {
    loadCsv("stars/ten-star.csv", true);
    try {
      HttpURLConnection conn =
          tryRequest("/searchcsv?query=lalalala-wrong-params_lalalala&mode=fuzzy");
      assertEquals(200, conn.getResponseCode());
      Moshi moshi = new Moshi.Builder().build();
      CsvDataResponse response =
          moshi
              .adapter(CsvDataResponse.class)
              .fromJson(new Buffer().readFrom(conn.getInputStream()));
      // System.out.println(response);
      assertEquals(ResultType.error_bad_request, response.result());
      conn.disconnect();
    } catch (IOException e) {
      e.printStackTrace();
      fail(e);
    }
  }

  /** Test if output rows are correct with a valid basic query. * */
  @Test
  public void testSearchCsvSuccess() {
    loadCsv("stars/ten-star.csv", true);
    try {
      HttpURLConnection conn = tryRequest("/searchcsv?query=Rigel%20Kentaurus@1&mode=fuzzy");
      // Get an OK response (the *connection* worked, the *API* provides an error response)
      assertEquals(200, conn.getResponseCode());
      Moshi moshi = new Moshi.Builder().build();
      CsvDataResponse response =
          moshi
              .adapter(CsvDataResponse.class)
              .fromJson(new Buffer().readFrom(conn.getInputStream()));
      assertEquals(ResultType.success, response.result());
      assertEquals(2, response.data().size());
      assertEquals("Rigel Kentaurus B", response.data().get(0).get(1));
      conn.disconnect();
    } catch (IOException e) {
      fail(e);
    }
  }

  /** Test if output rows are correct with a valid composite query. * */
  @Test
  public void testSearchCsvSuccess2() {
    loadCsv("stars/ten-star.csv", true);
    try {
      HttpURLConnection conn =
          tryRequest("/searchcsv?query=AND(Rigel%20Kentaurus@1,71454@0)&mode=fuzzy");
      assertEquals(200, conn.getResponseCode());
      Moshi moshi = new Moshi.Builder().build();
      CsvDataResponse response =
          moshi
              .adapter(CsvDataResponse.class)
              .fromJson(new Buffer().readFrom(conn.getInputStream()));
      // System.out.println(response);
      assertEquals(ResultType.success, response.result());
      assertEquals(1, response.data().size());
      assertEquals("Rigel Kentaurus B", response.data().get(0).get(1));
      conn.disconnect();
    } catch (IOException e) {
      fail(e);
    }
  }
}
