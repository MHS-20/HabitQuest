package habitquest.guild.application;

public class BattleNotFoundException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  public BattleNotFoundException(String message) {
    super(message);
  }
}
