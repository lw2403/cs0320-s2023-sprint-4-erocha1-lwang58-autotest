package edu.brown.cs32.csv.routers;

import edu.brown.cs32.csv.handlers.CSVParser;
import edu.brown.cs32.csv.response.CsvMessageResponse;
import edu.brown.cs32.shared.APIFailureException;
import edu.brown.cs32.shared.ResultType;
import edu.brown.cs32.weather.response.FailureWeatherResponse;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import spark.Request;
import spark.Response;
import spark.Route;

public class LoadCsvRouter implements Route, CsvRouter {
  private static final String dirPath = "data/";
  //  private static final String dirPath = "data\\";
  private final Set<CSVParser> csvParserset;
  //  private CSVParser csvParser;

  public LoadCsvRouter(Set<CSVParser> csvParserSet) {
    this.csvParserset = csvParserSet;
  }

  /**
   * Handles loadcsv endpoint Sets success response if no APIFailureException caught Sets failure
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
    String filePath = request.queryParams("filepath");
    String hasHeader = request.queryParams("header");
    try {
      checkFilePath(filePath);
      checkHasHeader(hasHeader);
      try {
        Reader reader = new FileReader(dirPath + filePath);
        csvParserset.clear();
        CSVParser csvParser;
        csvParserset.add(csvParser = new CSVParser("true".equals(hasHeader.toLowerCase()), reader));
        // appending success response
        String successResponse =
            new CsvMessageResponse(
                    ResultType.success,
                    "Successfully loaded CSV from file path '" + filePath + "'.",
                    parameters,
                    csvParser.getHeader())
                .serialize();
        response.body(successResponse);
      } catch (IOException e) {
        throw new APIFailureException(
            ResultType.error_bad_request, "Failed to read CSV from file path " + filePath + ".");
      }
    } catch (APIFailureException e) {
      // appending failure response
      String failureResponse =
          new FailureWeatherResponse(e.getResultType(), e.getMessage(), parameters).serialize();
      response.body(failureResponse);
    }
    return null;
  }

  /**
   * Checks if filePath is null, does not exist or attempts to access upward directory
   *
   * @param filePath from http request params
   * @throws APIFailureException pass on to handle
   */
  private void checkFilePath(String filePath) throws APIFailureException {
    if (filePath == null || filePath == "") {
      throw new APIFailureException(
          ResultType.error_bad_request, "Error: file path parameter is not provided.");
    }
    if (filePath.contains("..")) {
      throw new APIFailureException(
          ResultType.error_bad_request,
          "Error: file path insecure, no upwards directory level allowed.");
    }
    // check file is exists
    if (!Files.exists(Paths.get(dirPath, filePath))) {
      throw new APIFailureException(
          ResultType.error_datasource, "Error: file path to '" + filePath + "' does not exist.");
    }
  }

  /**
   * Checks if header parameter is null or ill-formed
   *
   * @param hasHeader from http request params, should be "true" or "false"
   * @throws APIFailureException pass on to handle
   */
  private void checkHasHeader(String hasHeader) throws APIFailureException {
    if (Objects.equals(hasHeader, "true") | Objects.equals(hasHeader, "false")) return;
    throw new APIFailureException(
        ResultType.error_bad_request, "header parameter is invalid, must be true or false.");
  }
}
