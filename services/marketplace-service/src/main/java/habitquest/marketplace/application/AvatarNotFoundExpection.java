package habitquest.marketplace.application;

public class AvatarNotFoundExpection extends RuntimeException {
  private static final long serialVersionUID = 1L;

  public AvatarNotFoundExpection(String message) {
    super(message);
  }
}
