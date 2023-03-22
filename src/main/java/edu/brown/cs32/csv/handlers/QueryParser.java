package edu.brown.cs32.csv.handlers;

import edu.brown.cs32.shared.APIFailureException;
import edu.brown.cs32.shared.ResultType;
import java.util.ArrayList;
import java.util.List;

/** Class to parse query expression and perform query search */
public class QueryParser {

  private static String expression;
  List<BasicQuery> basicQueries = new ArrayList<>();
  private static String queryMode;
  private String compositeQuery = "";
  private int compIndex = 0;
  private int queryIndex = 0;

  /**
   * @param rawQuery passed from command line arguments
   * @param queryMode "exact" or "fuzzy", default to "exact" if not specified
   * @param header header row parsed from CSVParser to assist query search
   * @throws APIFailureException pass on to SearchCsvRouter
   */
  public QueryParser(String rawQuery, String queryMode, List<String> header)
      throws APIFailureException {
    if (!rawQuery.contains("@")) {
      throw new APIFailureException(
          ResultType.error_bad_request,
          "query parameter needs to be value@column or OP(q1,q2), with OP: AND, OR, NOT.");
    }
    this.queryMode = queryMode;
    expression =
        rawQuery
            .replaceAll("AND", "&")
            .replaceAll("OR", "|")
            .replaceAll("NOT", "!")
            //            .replaceAll(" in ", "@")
            //            .replaceAll(", ", ",")
            .replaceAll("%20", " ");
    parseExpression(expression, header);
  }

  /**
   * Invoked in the constructor of QueryParser Parse query expression into a list of basic queries
   * and an expression of composite query
   *
   * @param expression parsed query expression, i.e. AND(X@Y)
   * @param header header row parsed from CSVParser to assist query search
   * @throws APIFailureException pass on to QueryParser
   */
  private void parseExpression(String expression, List<String> header) throws APIFailureException {
    int left = 0;
    int bound = expression.length();
    List<Character> operators = List.of('&', '|', '!', '(', ')', ',');
    while (left < bound) {
      if (operators.contains(expression.charAt(left))) {
        compositeQuery += expression.charAt(left);
        left++;
        continue;
      }
      if (left == bound) {
        break;
      }
      int right = left + 1;
      while (right < bound && !operators.contains(expression.charAt(right))) {
        right++;
      }
      basicQueries.add(new BasicQuery(expression.substring(left, right), header, queryMode));
      compositeQuery += 'Q'; // swap X@Y with Query mark in compositeQueryExpression
      left = right;
    }
  }

  /** Invoked after search query is performed to reset parsing indices */
  public void resetQuery() {
    this.compIndex = 0;
    this.queryIndex = 0;
  }

  /**
   * Invoked when querying each row of the csv Calls other query methods to perform recursive
   * querying
   *
   * @param row current row to query
   * @return if the current row is accepted by the query
   */
  public Boolean parseCompositeQuery(List<String> row) {
    Boolean result = false;
    switch (compositeQuery.charAt(compIndex)) {
      case 'Q': // use compIndex to evaluate the specific basic query
        compIndex++;
        result = basicQueries.get(queryIndex++).evaluateQuery(row);
        break;
      case '&':
        compIndex++;
        result = parseAndQuery(row);
        break;
      case '|':
        compIndex++;
        result = parseOrQuery(row);
        break;
      case '!':
        compIndex++;
        result = parseNotQuery(row);
        break;
    }
    return result;
  }

  /**
   * Invoked in parseCompositeQuery()
   *
   * @param row current row to query
   * @return if the current row is accepted by this basic query
   */
  private Boolean parseAndQuery(List<String> row) {
    ++compIndex;
    Boolean result = parseCompositeQuery(row);
    while (compIndex < compositeQuery.length() && compositeQuery.charAt(compIndex) != ')') {
      if (compositeQuery.charAt(compIndex) == ',') {
        ++compIndex;
      } else {
        result &= parseCompositeQuery(row);
      }
    }
    ++compIndex;
    return result;
  }

  /**
   * Invoked in parseCompositeQuery()
   *
   * @param row current row to query
   * @return if the current row is accepted by this basic query
   */
  private Boolean parseOrQuery(List<String> row) {
    ++compIndex;
    Boolean result = parseCompositeQuery(row);
    while (compIndex < compositeQuery.length() && compositeQuery.charAt(compIndex) != ')') {
      if (compositeQuery.charAt(compIndex) == ',') {
        ++compIndex;
      } else {
        result |= parseCompositeQuery(row);
      }
    }
    ++compIndex;
    return result;
  }

  /**
   * Invoked in parseCompositeQuery()
   *
   * @param row current row to query
   * @return if the current row is accepted by this basic query
   */
  private Boolean parseNotQuery(List<String> row) {
    ++compIndex;
    Boolean result = parseCompositeQuery(row);
    ++compIndex;
    return !result;
  }

  /**
   * Invoked in QueryParserTest to verify index resetting
   *
   * @return compIndex
   */
  public int getCompIndex() {
    return compIndex;
  }

  /**
   * Invoked in QueryParserTest to verify index resetting
   *
   * @return queryIndex
   */
  public int getQueryIndex() {
    return queryIndex;
  }

  /**
   * Invoked in QueryParserTest to verify expression parsing
   *
   * @return parsed query expression in string, i.e. AND(Q,Q)
   */
  public String getCompositeQuery() {
    return compositeQuery;
  }
}
