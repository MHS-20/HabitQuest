package habitquest.guild.infrastructure;

public class AvatarCommunicationException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  public AvatarCommunicationException(String message, Throwable cause) {
    super(message, cause);
  }
}
