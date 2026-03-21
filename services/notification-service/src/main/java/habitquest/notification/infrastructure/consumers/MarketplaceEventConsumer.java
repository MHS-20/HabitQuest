package habitquest.notification.infrastructure.consumers;

import common.hexagonal.Adapter;
import habitquest.notification.infrastructure.notification.NotificationService;
import habitquest.notification.infrastructure.repository.UserEmailRepository;
import java.time.Instant;
import java.util.function.Consumer;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Adapter
@Component
public class MarketplaceEventConsumer extends AvatarAwareEventConsumer {

  public MarketplaceEventConsumer(
      UserEmailRepository userEmailRepository, NotificationService notificationService) {
    super(userEmailRepository, notificationService);
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
      sendToAvatar(
          message.avatarId(),
          "Acquisto completato!",
          "Hai acquistato \"" + message.itemName() + "\" dal marketplace. Buon utilizzo!");
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
      sendToAvatar(
          message.avatarId(),
          "Oggetto venduto!",
          "Hai venduto \"" + message.itemName() + "\" sul marketplace con successo!");
    };
  }

  public record ItemBoughtMessage(
      String marketplaceId, String itemName, String avatarId, Instant occurredOn) {}

  public record ItemSoldMessage(
      String marketplaceId, String itemName, String avatarId, Instant occurredOn) {}
}
