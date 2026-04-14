package habitquest.marketplace.application.port.out;

import common.hexagonal.OutBoundPort;
import habitquest.marketplace.domain.events.ItemBought;
import habitquest.marketplace.domain.events.ItemSold;

@OutBoundPort
public interface MarketplaceNotifier {
  void notifyItemBought(ItemBought e);

  void notifyItemSold(ItemSold e);
}
