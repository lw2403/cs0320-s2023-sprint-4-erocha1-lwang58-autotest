package edu.brown.cs32.csv.routers;

import edu.brown.cs32.csv.handlers.CSVParser;
import edu.brown.cs32.shared.APIFailureException;
import edu.brown.cs32.shared.ResultType;
import java.util.Iterator;
import java.util.NoSuchElementException;

public interface CsvRouter {

  /**
   * Gets csvParser from Iterator<CSVParser>
   *
   * @param iterator iterator passed from server
   * @return CSVParser
   * @throws APIFailureException pass on to handle
   */
  default CSVParser getCsvParser(Iterator<CSVParser> iterator) throws APIFailureException {
    try {
      CSVParser csvParser = iterator.next();
      return csvParser;
    } catch (NoSuchElementException e) {
      throw new APIFailureException(ResultType.error_datasource, "No csv is loaded.");
    }
  }
}
