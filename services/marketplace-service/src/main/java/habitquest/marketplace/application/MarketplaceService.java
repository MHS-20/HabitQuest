package habitquest.marketplace.application;

import common.hexagonal.InBoundPort;
import habitquest.marketplace.domain.Marketplace;
import habitquest.marketplace.domain.items.*;
import java.util.List;

@InBoundPort
public interface MarketplaceService {
    Marketplace getMarketplace(String marketplaceId);
    List<Item> getItems(String marketplaceId);
    void updateItems(String marketplaceId, List<Item> items);

    List<Armor> getArmors(String marketplaceId);
    void addArmors(String marketplaceId, List<Armor> armors);

    List<Weapon> getWeapons(String marketplaceId);
    void addWeapons(String marketplaceId, List<Weapon> weapons);

    List<Potion> getPotions(String marketplaceId);
    void addPotions(String marketplaceId, List<Potion> potions);

    List<HealthPotion> getHealthPotions(String marketplaceId);
    void addHealthPotions(String marketplaceId, List<HealthPotion> healthPotions);

    List<ManaPotion> getManaPotions(String marketplaceId);
    void addManaPotions(String marketplaceId, List<ManaPotion> manaPotions);
}
