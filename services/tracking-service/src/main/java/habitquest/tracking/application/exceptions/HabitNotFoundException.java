package habitquest.tracking.application.exceptions;

public class HabitNotFoundException extends RuntimeException {
  public static final long serialVersionUID = 1L;

  public HabitNotFoundException(String message) {
    super(message);
  }
}
