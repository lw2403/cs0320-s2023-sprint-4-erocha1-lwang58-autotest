package edu.brown.cs32.csv.routers;

import edu.brown.cs32.csv.handlers.CSVParser;
import edu.brown.cs32.csv.handlers.QueryParser;
import edu.brown.cs32.csv.response.CsvDataResponse;
import edu.brown.cs32.shared.APIFailureException;
import edu.brown.cs32.shared.ResultType;
import edu.brown.cs32.weather.response.FailureWeatherResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import spark.Request;
import spark.Response;
import spark.Route;

public class SearchCsvRouter implements Route, CsvRouter {
  private Set<CSVParser> csvParserSet;

  public SearchCsvRouter(Set<CSVParser> csvParserSet) {
    //    System.out.println(csvParser.getNumRows());
    this.csvParserSet = csvParserSet;
  }

  /**
   * Handles searchcsv endpoint Sets success response if no APIFailureException caught Sets failure
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
    String query = request.queryParams("query");
    String mode = request.queryParams("mode");
    try {
      // check params first
      checkMode(mode);
      checkQuery(query);
      CSVParser csvParser = getCsvParser(csvParserSet.iterator());
      List<List<String>> result = findSearchResult(query, mode, csvParser);
      String successResponse =
          new CsvDataResponse(ResultType.success, result, parameters, csvParser.getHeader())
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

  /**
   * Finds the valid rows that passes the search
   *
   * @param query query string, i.e. basic: value@columnIndex, value@columnName, value@any,
   *     composite: AND(q1,q2), OR(q1,q2), NOT(q1,q2)
   * @param mode "fuzzy" or "exact",
   * @param csvParser passed from handle
   * @return valid rows in 2d array of strings
   * @throws APIFailureException pass on to handle
   */
  private List<List<String>> findSearchResult(String query, String mode, CSVParser csvParser)
      throws APIFailureException {
    QueryParser queryParser = new QueryParser(query, mode, csvParser.getHeader());
    List<List<String>> result = new ArrayList<>();
    List<List<String>> rows = csvParser.getRows();
    for (List<String> row : rows) {
      Boolean rowIsGood = queryParser.parseCompositeQuery(row);
      queryParser.resetQuery();
      if (rowIsGood) result.add(row);
    }
    return result;
  }

  /**
   * Checks if query parameter is null or follows value@column format
   *
   * @param query parameter from http request
   * @throws APIFailureException pass on to handle
   */
  private void checkQuery(String query) throws APIFailureException {

    if (query == null) {
      throw new APIFailureException(
          ResultType.error_bad_request,
          "query parameter is missing, should be basic query \"value@column\" "
              + "or composite query \"AND(q1,q2)\", \"OR(q1,q2)\", \"NOT(q)\".");
    }
    if (query == "not") {
      throw new APIFailureException(
          ResultType.error_bad_request,
          "query parameter is invalid, should be basic query \"value@column\" "
              + "or composite query \"AND(q1,q2)\", \"OR(q1,q2)\", \"NOT(q)\".");
    }
  }

  /**
   * Checks if mode parameter is null or valid
   *
   * @param mode "fuzzy" or "exact"
   * @throws APIFailureException pass on to handle
   */
  private void checkMode(String mode) throws APIFailureException {
    if (Objects.equals(mode, "fuzzy") | Objects.equals(mode, "exact")) return;
    if (mode == null) {
      throw new APIFailureException(
          ResultType.error_bad_request,
          "mode parameter is missing, should be \"fuzzy\" or \"exact\".");
    } else {
      throw new APIFailureException(
          ResultType.error_bad_request,
          "mode parameter is invalid, should be \"fuzzy\" or \"exact\".");
    }
  }
}
