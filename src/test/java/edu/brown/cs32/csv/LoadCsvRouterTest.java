package edu.brown.cs32.csv;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import com.squareup.moshi.Moshi;
import edu.brown.cs32.IntegrationTestSetUpTearDown;
import edu.brown.cs32.csv.response.CsvMessageResponse;
import edu.brown.cs32.shared.ResultType;
import java.io.IOException;
import java.net.HttpURLConnection;
import okio.Buffer;
import org.junit.jupiter.api.Test;

public class LoadCsvRouterTest extends IntegrationTestSetUpTearDown {
  /** Test if loadcsv endpoint is correctly routed. * */
  @Test
  public void testWebRequestLoadCsv() {
    try {
      HttpURLConnection conn = tryRequest("/loadcsv");
      assertEquals(200, conn.getResponseCode());
      conn.disconnect();
    } catch (IOException e) {
      fail(e);
    }
  }

  /** Test if loadcsv handles missing parameters correctly. * */
  @Test
  public void testLoadCsvFailWithOutParameters() {
    try {
      HttpURLConnection conn = tryRequest("/loadcsv");
      assertEquals(200, conn.getResponseCode());
      Moshi moshi = new Moshi.Builder().build();
      CsvMessageResponse response =
          moshi
              .adapter(CsvMessageResponse.class)
              .fromJson(new Buffer().readFrom(conn.getInputStream()));
      assertEquals(ResultType.error_bad_request, response.result());
      conn.disconnect();
    } catch (IOException e) {
      fail(e);
    }
  }

  /** Test if loadcsv handles missing filepath parameter correctly. * */
  @Test
  public void testLoadCsvFailWithOutFilepathParam() {
    try {
      HttpURLConnection conn = tryRequest("/loadcsv?header=false");
      assertEquals(200, conn.getResponseCode());
      Moshi moshi = new Moshi.Builder().build();
      CsvMessageResponse response =
          moshi
              .adapter(CsvMessageResponse.class)
              .fromJson(new Buffer().readFrom(conn.getInputStream()));
      assertEquals(ResultType.error_bad_request, response.result());
      conn.disconnect();
    } catch (IOException e) {
      fail(e);
    }
  }

  /** Test if loadcsv handles missing header parameter correctly. * */
  @Test
  public void testLoadCsvFailWithOutHeaderParam() {
    try {
      HttpURLConnection conn = tryRequest("/loadcsv?filepath=stars/stardata.csv");
      assertEquals(200, conn.getResponseCode());
      Moshi moshi = new Moshi.Builder().build();
      CsvMessageResponse response =
          moshi
              .adapter(CsvMessageResponse.class)
              .fromJson(new Buffer().readFrom(conn.getInputStream()));
      assertEquals(ResultType.error_bad_request, response.result());
      conn.disconnect();
    } catch (IOException e) {
      fail(e);
    }
  }

  /** Test if valid filepath is successfully loaded. * */
  @Test
  public void testLoadCsvSuccess() {
    try {
      HttpURLConnection conn = tryRequest("/loadcsv?filepath=stars/stardata.csv&header=false");
      assertEquals(200, conn.getResponseCode());
      Moshi moshi = new Moshi.Builder().build();
      CsvMessageResponse response =
          moshi
              .adapter(CsvMessageResponse.class)
              .fromJson(new Buffer().readFrom(conn.getInputStream()));
      assertEquals("success", response.result().toString());
      assertEquals("stars/stardata.csv", response.params().get("filepath")[0]);
      conn.disconnect();
    } catch (IOException e) {
      fail(e);
    }
  }

  /** Test if loading files twice correctly sets the csvParser. * */
  @Test
  public void testLoadCsvMultipleTimes() {
    try {
      Moshi moshi = new Moshi.Builder().build();
      // first successful request
      HttpURLConnection conn1 = tryRequest("/loadcsv?filepath=stars/stardata.csv&header=true");
      assertEquals(200, conn1.getResponseCode());
      CsvMessageResponse response1 =
          moshi
              .adapter(CsvMessageResponse.class)
              .fromJson(new Buffer().readFrom(conn1.getInputStream()));
      assertEquals(ResultType.success, response1.result());
      assertEquals("stars/stardata.csv", response1.params().get("filepath")[0]);
      conn1.disconnect();
      // second failure request
      HttpURLConnection conn2 = tryRequest("/loadcsv?filepath=stars/lalalalala.csv&header=false");
      assertEquals(200, conn2.getResponseCode());
      CsvMessageResponse response2 =
          moshi
              .adapter(CsvMessageResponse.class)
              .fromJson(new Buffer().readFrom(conn2.getInputStream()));
      assertEquals(ResultType.error_datasource, response2.result());
      conn2.disconnect();
      // third successful request
      HttpURLConnection conn3 =
          tryRequest("/loadcsv?filepath=stars/ten-star-headerless.csv&header=false");
      assertEquals(200, conn2.getResponseCode());
      CsvMessageResponse response3 =
          moshi
              .adapter(CsvMessageResponse.class)
              .fromJson(new Buffer().readFrom(conn3.getInputStream()));
      assertEquals(ResultType.success, response3.result());
      assertEquals("stars/ten-star-headerless.csv", response3.params().get("filepath")[0]);
      conn3.disconnect();
    } catch (IOException e) {
      fail(e);
    }
  }

  /** Test if invalid filepath give correct error result. * */
  @Test
  public void testLoadCsvFail() {
    try {
      HttpURLConnection conn = tryRequest("/loadcsv?filepath=stars/xxxx.csv&header=false");
      assertEquals(200, conn.getResponseCode());
      Moshi moshi = new Moshi.Builder().build();
      CsvMessageResponse response =
          moshi
              .adapter(CsvMessageResponse.class)
              .fromJson(new Buffer().readFrom(conn.getInputStream()));
      assertEquals(ResultType.error_datasource, response.result());
      conn.disconnect();
    } catch (IOException e) {
      fail(e);
    }
  }

  /** Test if malicious filepath parameter that tries to access upward directory is halted. * */
  @Test
  public void testLoadCsvMaliciousFilePath() {
    try {
      HttpURLConnection conn =
          tryRequest("/loadcsv?filepath=stars/../../data/stardata.csv&header=true");
      assertEquals(200, conn.getResponseCode());
      Moshi moshi = new Moshi.Builder().build();
      CsvMessageResponse response =
          moshi
              .adapter(CsvMessageResponse.class)
              .fromJson(new Buffer().readFrom(conn.getInputStream()));
      assertEquals(ResultType.error_bad_request, response.result());
      conn.disconnect();
    } catch (IOException e) {
      fail(e);
    }
  }
}
