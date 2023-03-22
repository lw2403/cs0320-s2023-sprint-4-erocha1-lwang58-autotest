package edu.brown.cs32.shared;

import com.google.gson.Gson;

public interface APIResponse {
  public default String serialize() {
    Gson gson = new Gson();
    return gson.toJson(this);
  }
}
