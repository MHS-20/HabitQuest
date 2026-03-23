package habitquest.avatar.application;

public class AvatarNotFoundException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  public AvatarNotFoundException(String message) {
    super(message);
  }
}
