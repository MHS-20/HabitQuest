package habitquest.quest.application;

public class QuestNotFoundException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  public QuestNotFoundException(String message) {
    super(message);
  }
}
