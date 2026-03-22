package common.ddd;

import com.fasterxml.jackson.annotation.JsonValue;

public record Id<T>(String value) {

  @JsonValue
  public String value() {
    return value;
  }
}
