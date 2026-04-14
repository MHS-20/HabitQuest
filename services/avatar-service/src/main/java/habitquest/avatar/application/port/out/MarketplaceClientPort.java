package habitquest.avatar.application.port.out;

import common.hexagonal.OutBoundPort;
import habitquest.avatar.application.exceptions.MarketplaceCommunicationException;

@OutBoundPort
public interface MarketplaceClientPort {
  void createMarketplace(String avatarId) throws MarketplaceCommunicationException;
}
