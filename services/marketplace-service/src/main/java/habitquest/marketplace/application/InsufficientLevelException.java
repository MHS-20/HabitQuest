package habitquest.marketplace.application;

public class InsufficientLevelException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  public InsufficientLevelException(String message) {
    super(message);
  }
}
