package habitquest.avatar.application.exceptions;

public class MarketplaceCommunicationException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  public MarketplaceCommunicationException(String message, Throwable ex) {
    super(message, ex);
  }
}
