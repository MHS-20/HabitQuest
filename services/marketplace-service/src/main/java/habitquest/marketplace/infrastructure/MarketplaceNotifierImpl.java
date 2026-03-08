package habitquest.marketplace.infrastructure;

import common.hexagonal.Adapter;
import habitquest.marketplace.application.MarketplaceNotifier;
import habitquest.marketplace.domain.events.ItemBought;
import habitquest.marketplace.domain.events.ItemSold;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Component;

@Adapter
@Component
public class MarketplaceNotifierImpl implements MarketplaceNotifier {

  private static final Logger LOG = LoggerFactory.getLogger(MarketplaceNotifierImpl.class);

  static final String ITEM_BOUGHT_BINDING = "marketplace-item-bought-out-0";
  static final String ITEM_SOLD_BINDING = "marketplace-item-sold-out-0";

  private final StreamBridge streamBridge;

  public MarketplaceNotifierImpl(StreamBridge streamBridge) {
    this.streamBridge = streamBridge;
  }

  @Override
  public void notifyItemBought(ItemBought event) {
    ItemBoughtMessage message =
        new ItemBoughtMessage(
            event.marketplaceId(), event.itemName(), event.avatarId(), Instant.now());

    LOG.info(
        "Publishing ItemBought event: marketplaceId={}, itemName={}, avatarId={}",
        message.marketplaceId(),
        message.itemName(),
        message.avatarId());

    boolean sent = streamBridge.send(ITEM_BOUGHT_BINDING, message);
    if (!sent) {
      LOG.error(
          "Failed to publish ItemBought event for item '{}' and avatar '{}'",
          message.itemName(),
          message.avatarId());
    }
  }

  @Override
  public void notifyItemSold(ItemSold event) {
    ItemSoldMessage message =
        new ItemSoldMessage(
            event.marketplaceId(), event.itemName(), event.avatarId(), Instant.now());

    LOG.info(
        "Publishing ItemSold event: marketplaceId={}, itemName={}, avatarId={}",
        message.marketplaceId(),
        message.itemName(),
        message.avatarId());

    boolean sent = streamBridge.send(ITEM_SOLD_BINDING, message);
    if (!sent) {
      LOG.error(
          "Failed to publish ItemSold event for item '{}' and avatar '{}'",
          message.itemName(),
          message.avatarId());
    }
  }

  // ─── Message records (the actual Kafka payload) ──────────────────────────────

  public record ItemBoughtMessage(
      String marketplaceId, String itemName, String avatarId, Instant occurredOn) {}

  public record ItemSoldMessage(
      String marketplaceId, String itemName, String avatarId, Instant occurredOn) {}
}
