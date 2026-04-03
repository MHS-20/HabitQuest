package habitquest.tracking.infrastructure;

public class QuestCommunicationException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  public QuestCommunicationException(String message, Throwable cause) {
    super(message, cause);
  }
}
