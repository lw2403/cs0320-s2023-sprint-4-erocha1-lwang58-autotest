package edu.brown.cs32.csv.routers;

import edu.brown.cs32.csv.handlers.CSVParser;
import edu.brown.cs32.csv.response.CsvDataResponse;
import edu.brown.cs32.shared.APIFailureException;
import edu.brown.cs32.shared.ResultType;
import edu.brown.cs32.weather.response.FailureWeatherResponse;
import java.util.Map;
import java.util.Set;
import spark.Request;
import spark.Response;
import spark.Route;

public class ViewCsvRouter implements Route, CsvRouter {
  private Set<CSVParser> csvParserSet;

  public ViewCsvRouter(Set<CSVParser> csvParserSet) {
    this.csvParserSet = csvParserSet;
  }

  /**
   * Handles viewcsv endpoint Sets success response if no APIFailureException caught Sets failure
   * response otherwise
   *
   * @param request The request object providing information about the HTTP request
   * @param response The response object providing functionality for modifying the response
   * @return null
   */
  @Override
  public Object handle(Request request, Response response) {
    response.header("Content-Type", "application/json");
    // fetching parameters
    Map<String, String[]> parameters = request.queryMap().toMap();
    try {
      if (!parameters.isEmpty())
        throw new APIFailureException(
            ResultType.error_bad_request, "viewcsv should not have any parameters.");
      CSVParser csvParser = getCsvParser(csvParserSet.iterator());
      String successResponse =
          new CsvDataResponse(
                  ResultType.success, csvParser.getTable(), parameters, csvParser.getHeader())
              .serialize();
      response.body(successResponse);
    } catch (APIFailureException e) {
      // appending failure response
      String failureResponse =
          new FailureWeatherResponse(e.getResultType(), e.getMessage(), parameters).serialize();
      response.body(failureResponse);
    }
    return null;
  }
}
