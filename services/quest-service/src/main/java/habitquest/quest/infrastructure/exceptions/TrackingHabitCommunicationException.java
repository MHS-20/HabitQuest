package habitquest.quest.infrastructure.exceptions;

public class TrackingHabitCommunicationException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  public TrackingHabitCommunicationException(String message, Throwable cause) {
    super(message, cause);
  }
}
