package habitquest.marketplace.application;

public class MarketplaceNotFoundException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    public MarketplaceNotFoundException(String message) {
        super(message);
    }
}
