package habitquest.marketplace.application;

import common.ddd.Repository;
import common.hexagonal.OutBoundPort;
import habitquest.marketplace.domain.Marketplace;

@OutBoundPort
public interface MarketplaceRepository extends Repository{
    void save(Marketplace marketplace);
    Marketplace findById(String id);
}
