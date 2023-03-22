package edu.brown.cs32.server;

import static spark.Spark.after;

import edu.brown.cs32.csv.handlers.CSVParser;
import edu.brown.cs32.csv.response.CsvMessageResponse;
import edu.brown.cs32.csv.routers.LoadCsvRouter;
import edu.brown.cs32.csv.routers.SearchCsvRouter;
import edu.brown.cs32.csv.routers.ViewCsvRouter;
import edu.brown.cs32.shared.ResultType;
import edu.brown.cs32.weather.WeatherRouter;
import java.util.HashSet;
import java.util.Set;
import spark.Spark;

/**
 * Top-level class for this sprint - Contains the server() method which starts Spark and runs the
 * various handlers - Restricts access control to expected endpoints - Routes to different
 * actionRouter to handle request and response - returns error_bad_json result if unexpected
 * endpoint found
 */
public class Server {
  public static void main(String[] args) {
    Spark.port(3232);
    // add header to response
    after(
        (request, response) -> {
          response.header("Access-Control-Allow-Origin", "*");
          // response.header("Access-Control-Allow-Origin", "loadcsv");
          // response.header("Access-Control-Allow-Origin", "viewcsv");
          // response.header("Access-Control-Allow-Origin", "searchcsv");
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
    System.out.println("Server started.");
  }
}
