package habitquest.avatar.infrastructure;

public class MarketplaceCommunicationException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  public MarketplaceCommunicationException(String message, Throwable ex) {
    super(message, ex);
  }
}
