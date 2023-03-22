package edu.brown.cs32.csv.handlers;

import edu.brown.cs32.shared.APIFailureException;
import edu.brown.cs32.shared.ResultType;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/** Class to perform basic query search */
public class BasicQuery {
  final String target;
  final int colIndex;
  private static String queryMode;

  /**
   * @param queryExpression of basic query, i.e. X@Y
   * @param header to assist query search in specified column
   * @param queryMode to determine exact search or fuzzy search
   * @throws APIFailureException pass on QueryParser
   */
  public BasicQuery(String queryExpression, List<String> header, String queryMode)
      throws APIFailureException {
    this.queryMode = queryMode;
    List<String> items = Arrays.asList(queryExpression.split("@"));
    target = items.get(0);
    String column = items.get(1);
    if (Objects.equals(column, "any")) {
      // if column is not specified
      colIndex = -1;
    } else if (isNumeric(column)) {
      // if column is specified by index
      colIndex = Integer.parseInt(column);
      if (header != null && !header.isEmpty() && (colIndex >= header.size() || colIndex < 0)) {
        // if columnIndex is out of bounds
        throw new APIFailureException(
            ResultType.error_bad_request,
            String.format(
                "columnIndex is out of bounds, should be within 0 and %s.", header.size()));
      }
    } else {
      // if column is specified by name
      colIndex = header.indexOf(column);
      if (colIndex == -1) {
        // if columnName does not exist
        throw new APIFailureException(
            ResultType.error_bad_request,
            String.format("%s is not a valid columnName in header.", column));
      }
    }
  }

  /**
   * @param row current row to query
   * @return true if the current row is accepted by the query
   */
  public Boolean evaluateQuery(List<String> row) {
    Boolean result = false;
    if (Objects.equals(colIndex, -1)) {
      // searching in all columns
      for (String value : row) {
        if (Objects.equals(queryMode, "exact")) {
          if (Objects.equals(value, target)) {
            result = true;
            break;
          }
        } else {
          if (value.contains(target)) {
            result = true;
            break;
          }
        }
      }
    } else {
      // searching in the specified column
      if (Objects.equals(queryMode, "exact")) {
        result = Objects.equals(row.get(colIndex), target);
      } else {
        result = row.get(colIndex).contains(target);
      }
    }
    return result;
  }

  /**
   * Helper method to determine if specified column is name or index
   *
   * @param strNum column input
   * @return true if column is index
   */
  private static boolean isNumeric(String strNum) {
    if (strNum == null) {
      return false;
    }
    try {
      double d = Double.parseDouble(strNum);
    } catch (NumberFormatException e) {
      return false;
    }
    return true;
  }
}
