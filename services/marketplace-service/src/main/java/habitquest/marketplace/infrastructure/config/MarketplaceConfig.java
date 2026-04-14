package habitquest.marketplace.infrastructure.config;

import habitquest.marketplace.domain.factory.MarketplaceFactory;
import habitquest.marketplace.domain.factory.UUIDGenerator;
import habitquest.marketplace.domain.items.ItemCatalog;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MarketplaceConfig {

  @Bean
  public MarketplaceFactory marketplaceFactory() {
    return new MarketplaceFactory(new UUIDGenerator());
  }

  @Bean
  public ItemCatalog itemCatalog() {
    return new ItemCatalog();
  }
}
