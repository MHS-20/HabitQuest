package habitquest.guild.domain.guild;

public class UnauthorizedGuildOperationException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  public UnauthorizedGuildOperationException(String requestorId, String operation) {
    super(
        "Member '"
            + requestorId
            + "' is not authorized to perform '"
            + operation
            + "' in this guild");
  }
}
