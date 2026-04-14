package habitquest.quest.application.exceptions;

public class AvatarRewardException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  public AvatarRewardException(String message, Throwable cause) {
    super(message, cause);
  }
}
