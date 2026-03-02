package habitquest.marketplace.application;

import common.ddd.Repository;
import common.hexagonal.OutBoundPort;
import habitquest.marketplace.domain.Marketplace;
import java.util.Optional;

@OutBoundPort
public interface MarketplaceRepository extends Repository {
  void save(Marketplace marketplace);

  Optional<Marketplace> findById(String id);

  void deleteById(String id);
}
