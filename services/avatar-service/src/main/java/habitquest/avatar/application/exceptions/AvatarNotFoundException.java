package habitquest.avatar.application.exceptions;

public class AvatarNotFoundException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  public AvatarNotFoundException(String message) {
    super(message);
  }
}
