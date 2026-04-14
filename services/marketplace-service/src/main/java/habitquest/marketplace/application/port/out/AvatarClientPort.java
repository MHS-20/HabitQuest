package habitquest.marketplace.application.port.out;

import common.hexagonal.OutBoundPort;
import habitquest.marketplace.application.exceptions.AvatarCommunicationException;
import habitquest.marketplace.domain.items.Item;
import habitquest.marketplace.domain.marketplace.Money;

@OutBoundPort
public interface AvatarClientPort {
  void spendMoney(String avatarId, Money price) throws AvatarCommunicationException;

  void earnMoney(String avatarId, Money price) throws AvatarCommunicationException;

  void addItemToInventory(String avatarId, Item item) throws AvatarCommunicationException;

  void removeItemFromInventory(String avatarId, Item item) throws AvatarCommunicationException;
}
