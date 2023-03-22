package edu.brown.cs32.csv.response;

import edu.brown.cs32.shared.APIResponse;
import edu.brown.cs32.shared.ResultType;
import java.util.List;
import java.util.Map;

public record CsvDataResponse(
    ResultType result, List<List<String>> data, Map<String, String[]> params, List<String> header)
    implements APIResponse {}
