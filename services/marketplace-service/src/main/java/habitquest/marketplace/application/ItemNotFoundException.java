package habitquest.marketplace.application;

public class ItemNotFoundException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    public ItemNotFoundException(String markerplaceId, String itemName) {
    super(markerplaceId + itemName);
  }
}
