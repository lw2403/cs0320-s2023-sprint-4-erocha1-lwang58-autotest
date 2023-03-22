package edu.brown.cs32;

import static spark.Spark.after;

import edu.brown.cs32.csv.handlers.CSVParser;
import edu.brown.cs32.csv.response.CsvMessageResponse;
import edu.brown.cs32.csv.routers.LoadCsvRouter;
import edu.brown.cs32.csv.routers.SearchCsvRouter;
import edu.brown.cs32.csv.routers.ViewCsvRouter;
import edu.brown.cs32.shared.ResultType;
import edu.brown.cs32.weather.WeatherRouter;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import spark.Spark;

public class IntegrationTestSetUpTearDown {
  @BeforeAll
  public static void setup_before_everything() {
    Spark.port(0);
    Logger.getLogger("").setLevel(Level.WARNING); // empty name = root logger
  }

  @BeforeEach
  public void setup() {
    after(
        (request, response) -> {
          response.header("Access-Control-Allow-Origin", "*");
          response.header("Access-Control-Allow-Methods", "*");
        });

    Set<CSVParser> csvParserSet = new HashSet<>();
    Spark.get("loadcsv", new LoadCsvRouter(csvParserSet));
    Spark.get("viewcsv", new ViewCsvRouter(csvParserSet));
    Spark.get("searchcsv", new SearchCsvRouter(csvParserSet));
    Spark.get("weather", new WeatherRouter());
    Spark.get(
        "*",
        (request, response) -> {
          response.header("Content-Type", "application/json");
          response.body(
              new CsvMessageResponse(
                      ResultType.error_bad_json,
                      "Unexpected endpoints.",
                      request.queryMap().toMap(),
                      null)
                  .serialize());
          return null;
        });
    Spark.init();
    Spark.awaitInitialization();
    // In fact, restart the entire Spark server for every test!
    // don't continue until the server is listening
  }

  @AfterEach
  public void teardown() {
    // Gracefully stop Spark listening on both endpoints
    Spark.stop();
    // don't proceed until the server is stopped
    Spark.awaitStop();
  }

  protected HttpURLConnection tryRequest(String apiCall) throws IOException {
    // Configure the connection (but don't actually send the request yet)
    URL requestURL = new URL("http://localhost:" + Spark.port() + "/" + apiCall);
    HttpURLConnection conn = (HttpURLConnection) requestURL.openConnection();
    conn.connect();
    return conn;
  }
}
