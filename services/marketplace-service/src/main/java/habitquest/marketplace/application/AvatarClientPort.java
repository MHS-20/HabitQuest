package habitquest.marketplace.application;

import common.hexagonal.OutBoundPort;
import habitquest.marketplace.domain.Money;
import habitquest.marketplace.domain.items.Item;

@OutBoundPort
public interface AvatarClientPort {
  void spendMoney(String avatarId, Money price) throws AvatarCommunicationException;

  void earnMoney(String avatarId, Money price) throws AvatarCommunicationException;

  void addItemToInventory(String avatarId, Item item) throws AvatarCommunicationException;

  void removeItemFromInventory(String avatarId, Item item) throws AvatarCommunicationException;
}
