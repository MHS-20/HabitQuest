package habitquest.avatar.application;

import common.hexagonal.OutBoundPort;

@OutBoundPort
public interface MarketplaceClientPort {
  void createMarketplace(String avatarId) throws MarketplaceCommunicationException;
}
