package habitquest.edge.infrastructure;

public class AvatarCreationException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  public AvatarCreationException(String message) {
    super(message);
  }

  public AvatarCreationException(String message, Throwable cause) {
    super(message, cause);
  }
}
