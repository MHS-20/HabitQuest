package habitquest.edge.domain;

@SuppressWarnings("PMD.MissingStaticMethodInNonInstantiatableClass")
public final class UserExceptions {

  private UserExceptions() {}

  public static class UserAlreadyExistsException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public UserAlreadyExistsException(String email) {
      super("User already exists with email: " + email);
    }
  }

  public static class UserNotFoundException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public UserNotFoundException(String email) {
      super("User not found with email: " + email);
    }
  }

  public static class InvalidCredentialsException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public InvalidCredentialsException() {
      super("Invalid credentials");
    }
  }
}
