package habitquest.avatar.application;

import habitquest.avatar.domain.avatar.*;
import habitquest.avatar.domain.items.EquippedItems;
import habitquest.avatar.domain.items.Inventory;
import habitquest.avatar.domain.items.Item;
import habitquest.avatar.domain.stats.AvatarStats;

public interface AvatarService {

  // --- Gestione Avatar ---
  String createAvatar(String name);

  Avatar getAvatarById(String id) throws AvatarNotFoundExpection;

  void updateAvatar(String id, Avatar updatedAvatar) throws AvatarNotFoundExpection;

  void deleteAvatar(String id) throws AvatarNotFoundExpection;

  void updateName(String avatarId, String name) throws AvatarNotFoundExpection;

  // --- Query ---
  String getName(String avatarId) throws AvatarNotFoundExpection;

  Money getMoney(String avatarId) throws AvatarNotFoundExpection;

  Inventory getInventory(String avatarId) throws AvatarNotFoundExpection;

  EquippedItems getEquippedItems(String avatarId) throws AvatarNotFoundExpection;

  Experience getExperience(String avatarId) throws AvatarNotFoundExpection;

  Level getLevel(String avatarId) throws AvatarNotFoundExpection;

  AvatarHealth getHealth(String avatarId) throws AvatarNotFoundExpection;

  AvatarMana getMana(String avatarId) throws AvatarNotFoundExpection;

  AvatarStats getAvatarStats(String avatarId) throws AvatarNotFoundExpection;

  // --- Money ---
  void earnMoney(String avatarId, Integer amount) throws AvatarNotFoundExpection;

  void spendMoney(String avatarId, Integer amount) throws AvatarNotFoundExpection;

  // --- Inventory ---
  void addToInventory(String avatarId, Item item) throws AvatarNotFoundExpection;

  void removeItem(String avatarId, Item item) throws AvatarNotFoundExpection;

  void equipItem(String avatarId, Item item) throws AvatarNotFoundExpection;

  void unequipItem(String avatarId, Item item) throws AvatarNotFoundExpection;

  // --- Combat ---
  void applyDamage(String avatarId, Integer amount) throws AvatarNotFoundExpection;

  void healAvatar(String avatarId, Integer amount) throws AvatarNotFoundExpection;

  void spendMana(String avatarId, Integer amount) throws AvatarNotFoundExpection;

  void restoreMana(String avatarId, Integer amount) throws AvatarNotFoundExpection;

  // region Progression
  void grantExperience(String avatarId, Integer amount) throws AvatarNotFoundExpection;

  void increaseStrength(String avatarId) throws AvatarNotFoundExpection;

  void increaseDefense(String avatarId) throws AvatarNotFoundExpection;

  void increaseIntelligence(String avatarId) throws AvatarNotFoundExpection;
  // endregion
}
