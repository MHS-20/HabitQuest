package habitquest.avatar.application;

import common.hexagonal.InBoundPort;
import habitquest.avatar.domain.avatar.*;
import habitquest.avatar.domain.items.EquippedItems;
import habitquest.avatar.domain.items.Inventory;
import habitquest.avatar.domain.stats.AvatarStats;

@InBoundPort
public interface AvatarService {

  Avatar createAvatar(String name);

  Avatar getAvatarById(String id) throws AvatarNotFoundExpection;

  Avatar updateAvatar(String id, Avatar updatedAvatar) throws AvatarNotFoundExpection;

  void deleteAvatar(String id) throws AvatarNotFoundExpection;

  // getters
  String getName(String avatarId) throws AvatarNotFoundExpection;

  Money getMoney(String avatarId) throws AvatarNotFoundExpection;

  Inventory getInventory(String avatarId) throws AvatarNotFoundExpection;

  EquippedItems getEquippedItems(String avatarId) throws AvatarNotFoundExpection;

  Experience getExperience(String avatarId) throws AvatarNotFoundExpection;

  Level getLevel(String avatarId) throws AvatarNotFoundExpection;

  Health getHealth(String avatarId) throws AvatarNotFoundExpection;

  Mana getMana(String avatarId) throws AvatarNotFoundExpection;

  AvatarStats getAvatarStats(String avatarId) throws AvatarNotFoundExpection;

  // Updaters
  Avatar updateName(String avatarId, String name) throws AvatarNotFoundExpection;

  Avatar updateMoney(String avatarId, Money money) throws AvatarNotFoundExpection;

  Avatar updateInventory(String avatarId, Inventory inventory) throws AvatarNotFoundExpection;

  Avatar updateEquippedItems(String avatarId, EquippedItems equippedItems)
      throws AvatarNotFoundExpection;

  Avatar updateExperience(String avatarId, Experience experience) throws AvatarNotFoundExpection;

  Avatar updateLevel(String avatarId, Level level) throws AvatarNotFoundExpection;

  Avatar updateHealth(String avatarId, Health health) throws AvatarNotFoundExpection;

  Avatar updateMana(String avatarId, Mana mana) throws AvatarNotFoundExpection;

  Avatar updateAvatarStats(String avatarId, AvatarStats avatarStats) throws AvatarNotFoundExpection;
}
