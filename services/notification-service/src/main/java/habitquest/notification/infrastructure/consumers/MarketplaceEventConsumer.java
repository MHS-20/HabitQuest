package habitquest.notification.infrastructure.consumers;

import common.hexagonal.Adapter;
import habitquest.notification.infrastructure.EventConsumer;
import habitquest.notification.infrastructure.NotificationService;
import java.time.Instant;
import java.util.function.Consumer;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Adapter
@Component
public class MarketplaceEventConsumer implements EventConsumer {

  private final NotificationService notificationService;

  public MarketplaceEventConsumer(NotificationService notificationService) {
    this.notificationService = notificationService;
  }

  @Bean
  public Consumer<ItemBoughtMessage> marketplaceItemBought() {
    return message -> {
      logger()
          .info(
              "Received ItemBought: marketplaceId={}, itemName={}, avatarId={}",
              message.marketplaceId(),
              message.itemName(),
              message.avatarId());
      notificationService.send("Hai acquistato \"" + message.itemName() + "\" dal marketplace!");
    };
  }

  @Bean
  public Consumer<ItemSoldMessage> marketplaceItemSold() {
    return message -> {
      logger()
          .info(
              "Received ItemSold: marketplaceId={}, itemName={}, avatarId={}",
              message.marketplaceId(),
              message.itemName(),
              message.avatarId());
      notificationService.send("Hai venduto \"" + message.itemName() + "\" sul marketplace!");
    };
  }

  public record ItemBoughtMessage(
      String marketplaceId, String itemName, String avatarId, Instant occurredOn) {}

  public record ItemSoldMessage(
      String marketplaceId, String itemName, String avatarId, Instant occurredOn) {}
}
