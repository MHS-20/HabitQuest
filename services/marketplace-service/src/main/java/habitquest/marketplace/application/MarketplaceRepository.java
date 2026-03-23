package habitquest.marketplace.application;

import common.ddd.Id;
import common.ddd.Repository;
import common.hexagonal.OutBoundPort;
import habitquest.marketplace.domain.Marketplace;
import java.util.Optional;

@OutBoundPort
public interface MarketplaceRepository extends Repository {
  void save(Marketplace marketplace);

  Optional<Marketplace> findById(Id<Marketplace> id);

  void deleteById(Id<Marketplace> id);
}
