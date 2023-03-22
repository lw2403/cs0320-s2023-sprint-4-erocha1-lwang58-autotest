package edu.brown.cs32.shared;

public class APIFailureException extends Exception {
  private final ResultType resultType;
  private final String message;

  public APIFailureException(ResultType resultType, String message) {
    super();
    this.resultType = resultType;
    this.message = message;
  }

  public APIFailureException(
      ResultType resultType,
      String message,
      Throwable cause,
      boolean enableSuppression,
      boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
    this.message = message;
    this.resultType = resultType;
  }

  public ResultType getResultType() {
    return resultType;
  }

  public String getMessage() {
    return message;
  }
}
