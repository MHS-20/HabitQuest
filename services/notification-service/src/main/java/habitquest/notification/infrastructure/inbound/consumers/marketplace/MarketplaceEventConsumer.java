package habitquest.notification.infrastructure.inbound.consumers.marketplace;

import common.hexagonal.Adapter;
import habitquest.notification.application.port.out.NotificationService;
import habitquest.notification.application.port.out.UserEmailRepository;
import habitquest.notification.infrastructure.inbound.consumers.base.AvatarAwareEventConsumer;
import habitquest.notification.infrastructure.inbound.consumers.marketplace.MarketplaceMessages.*;
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
          "Purchase completed!",
          "You bought \"" + message.itemName() + "\" from the marketplace. Enjoy it!");
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
          "Item sold!",
          "You sold \"" + message.itemName() + "\" on the marketplace successfully!");
    };
  }
}
