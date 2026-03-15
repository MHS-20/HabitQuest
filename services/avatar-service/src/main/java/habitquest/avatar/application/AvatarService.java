package habitquest.avatar.application;

import common.hexagonal.InBoundPort;
import habitquest.avatar.domain.avatar.*;
import habitquest.avatar.domain.items.EquippedItems;
import habitquest.avatar.domain.items.Inventory;
import habitquest.avatar.domain.items.Item;
import habitquest.avatar.domain.stats.AvatarStats;
import java.util.List;

@InBoundPort
public interface AvatarService {

  // --- Avatar ---
  String createAvatar(String name);

  Avatar getAvatarById(String id) throws AvatarNotFoundException;

  void deleteAvatar(String id) throws AvatarNotFoundException;

  void updateName(String avatarId, String name) throws AvatarNotFoundException;

  List<Avatar> searchAvatars(AvatarSearchRequest criteria);

  // --- Query ---
  String getName(String avatarId) throws AvatarNotFoundException;

  Money getMoney(String avatarId) throws AvatarNotFoundException;

  Inventory getInventory(String avatarId) throws AvatarNotFoundException;

  EquippedItems getEquippedItems(String avatarId) throws AvatarNotFoundException;

  Experience getExperience(String avatarId) throws AvatarNotFoundException;

  Level getLevel(String avatarId) throws AvatarNotFoundException;

  AvatarHealth getHealth(String avatarId) throws AvatarNotFoundException;

  AvatarMana getMana(String avatarId) throws AvatarNotFoundException;

  AvatarStats getAvatarStats(String avatarId) throws AvatarNotFoundException;

  // --- Money ---
  void earnMoney(String avatarId, Integer amount) throws AvatarNotFoundException;

  void spendMoney(String avatarId, Integer amount) throws AvatarNotFoundException;

  // --- Inventory ---
  void addToInventory(String avatarId, Item item) throws AvatarNotFoundException;

  void removeItem(String avatarId, Item item) throws AvatarNotFoundException;

  void equipItem(String avatarId, Item item) throws AvatarNotFoundException;

  void unequipItem(String avatarId, Item item) throws AvatarNotFoundException;

  // --- Combat ---
  boolean applyDamage(String avatarId, Integer amount) throws AvatarNotFoundException;

  void healAvatar(String avatarId, Integer amount) throws AvatarNotFoundException;

  void spendMana(String avatarId, Integer amount) throws AvatarNotFoundException;

  void restoreMana(String avatarId, Integer amount) throws AvatarNotFoundException;

  // --- Progression ---
  void grantExperience(String avatarId, Integer amount) throws AvatarNotFoundException;

  void increaseStrength(String avatarId) throws AvatarNotFoundException;

  void increaseDefense(String avatarId) throws AvatarNotFoundException;

  void increaseIntelligence(String avatarId) throws AvatarNotFoundException;
}
