package habitquest.marketplace.domain.exceptions;

public class ItemNotFoundException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  public ItemNotFoundException(String itemName) {
    super(itemName);
  }
}
