package habitquest.marketplace.infrastructure;

import common.hexagonal.Adapter;
import habitquest.marketplace.application.MarketplaceLogger;
import habitquest.marketplace.application.MarketplaceNotifier;
import habitquest.marketplace.domain.events.ItemBought;
import habitquest.marketplace.domain.events.ItemSold;
import java.time.Instant;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.messaging.MessagingException;
import org.springframework.stereotype.Component;

@Adapter
@Component
public class MarketplaceNotifierImpl implements MarketplaceNotifier {

  static final String ITEM_BOUGHT_BINDING = "marketplace.item-bought";
  static final String ITEM_SOLD_BINDING = "marketplace.item-sold";

  private final StreamBridge streamBridge;
  private final MarketplaceLogger log;

  public MarketplaceNotifierImpl(StreamBridge streamBridge, MarketplaceLogger log) {
    this.streamBridge = streamBridge;
    this.log = log;
  }

  @Override
  public void notifyItemBought(ItemBought event) {
    ItemBoughtMessage message =
        new ItemBoughtMessage(
            event.marketplaceId().value(),
            event.itemName(),
            event.avatarId().value(),
            Instant.now());

    log.info(message, "Publishing ItemBought event");

    try {
      boolean sent = streamBridge.send(ITEM_BOUGHT_BINDING, message);
      if (!sent) {
        log.error(message, "Failed to publish ItemBought event", null);
      }
    } catch (MessagingException ex) {
      log.error(message, "Failed to publish ItemBought event", ex);
    }
  }

  @Override
  public void notifyItemSold(ItemSold event) {
    ItemSoldMessage message =
        new ItemSoldMessage(
            event.marketplaceId().value(),
            event.itemName(),
            event.avatarId().value(),
            Instant.now());

    log.info(message, "Publishing ItemSold event");

    try {
      boolean sent = streamBridge.send(ITEM_SOLD_BINDING, message);
      if (!sent) {
        log.error(message, "Failed to publish ItemSold event", null);
      }
    } catch (MessagingException ex) {
      log.error(message, "Failed to publish ItemSold event", ex);
    }
  }

  // ─── Message records (the actual Kafka payload) ──────────────────────────────
  public record ItemBoughtMessage(
      String marketplaceId, String itemName, String avatarId, Instant occurredOn) {}

  public record ItemSoldMessage(
      String marketplaceId, String itemName, String avatarId, Instant occurredOn) {}
}
