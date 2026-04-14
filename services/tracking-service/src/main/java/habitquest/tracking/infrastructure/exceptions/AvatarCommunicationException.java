package habitquest.tracking.infrastructure.exceptions;

public class AvatarCommunicationException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public AvatarCommunicationException(String message, Throwable cause) {
    super(message, cause);
  }

  public AvatarCommunicationException(String message) {
    super(message);
  }
}
