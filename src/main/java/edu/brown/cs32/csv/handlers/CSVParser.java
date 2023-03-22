package edu.brown.cs32.csv.handlers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/** Class to parse given CSV reader, either csvFileReader or csvStringReader */
public class CSVParser {
  private final List<List<String>> table;
  private final List<String> header;
  private final List<List<String>> rows;
  private final List<List<String>> cols;
  private final int numRows;
  private final int numCols;
  private final List<String> typeErrors = new ArrayList<>();

  /**
   * @param hasHeader if input csv has a header
   * @param reader csvReader
   */
  public CSVParser(Boolean hasHeader, Reader reader) {
    table = new ArrayList<>();
    Boolean isHeader = hasHeader ? true : false;
    try {
      BufferedReader bufferedReader = new BufferedReader(reader);
      while (bufferedReader.ready()) {
        String line = bufferedReader.readLine();
        // line 32 to resolve last line being null when reading csv string
        if (Objects.equals(null, line)) break;
        List<String> row = Arrays.asList(line.split(","));
        table.add(row);
        // line 33-41 responsible for parsing csv row into desired objects for user story 3
        if (isHeader) {
          isHeader = false;
          continue;
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    // setting row and column dimensions
    numRows = hasHeader ? table.size() - 1 : table.size();
    numCols = table.get(0).size();
    // setting header
    header = hasHeader ? table.get(0) : new ArrayList<String>(numCols);
    // setting columns from table
    rows = hasHeader ? table.subList(1, numRows + 1) : table;
    cols = new ArrayList<>();
    if (typeErrors.size() == 0) {
      for (int i = 0; i < numCols; i++) {
        List<String> col = new ArrayList<String>();
        for (List<String> row : rows) {
          col.add(row.get(i));
        }
        cols.add(col);
      }
    }
  }

  /**
   * For User Story 3
   *
   * @return a list of error messages when creating T objects
   */
  public List<String> getTypeErrors() {
    return typeErrors;
  }

  /**
   * @return header of the csv input if hasHeader else a list of empty strings
   */
  public List<String> getHeader() {
    return header;
  }

  /**
   * @return number of rows of data from csv input
   */
  public int getNumRows() {
    return numRows;
  }

  /**
   * @return number of columns of data from csv input
   */
  public int getNumCols() {
    return numCols;
  }

  /**
   * @return rows of data from csv input, i.e. with header removed
   */
  public List<List<String>> getRows() {
    return rows;
  }

  public List<List<String>> getTable() {
    return table;
  }

  /**
   * @return columns of data from csv input, i.e. rows transposed
   */
  public List<List<String>> getColumns() {
    return cols;
  }
}
