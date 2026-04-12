package habitquest.avatar.application;

import common.ddd.Id;
import common.hexagonal.InBoundPort;
import habitquest.avatar.domain.avatar.*;
import habitquest.avatar.domain.items.EquippedItems;
import habitquest.avatar.domain.items.Item;
import habitquest.avatar.domain.stats.AvatarStats;
import java.util.List;

@InBoundPort
public interface AvatarService {

  // --- Avatar ---
  Id<Avatar> createAvatar(Id<Avatar> id, String name);

  Avatar getAvatarById(Id<Avatar> id) throws AvatarNotFoundException;

  void deleteAvatar(Id<Avatar> id) throws AvatarNotFoundException;

  void updateName(Id<Avatar> avatarId, String name) throws AvatarNotFoundException;

  List<Avatar> searchAvatars(AvatarSearchRequest criteria);

  void addPendingInvite(Id<Avatar> avatarId, Invite invite) throws AvatarNotFoundException;

  void acceptInvite(Id<Avatar> avatarId, Id<Invite> inviteId) throws AvatarNotFoundException;

  // --- Query ---
  String getName(Id<Avatar> avatarId) throws AvatarNotFoundException;

  Money getMoney(Id<Avatar> avatarId) throws AvatarNotFoundException;

  List<Item> getInventory(Id<Avatar> avatarId) throws AvatarNotFoundException;

  EquippedItems getEquippedItems(Id<Avatar> avatarId) throws AvatarNotFoundException;

  Experience getExperience(Id<Avatar> avatarId) throws AvatarNotFoundException;

  Level getLevel(Id<Avatar> avatarId) throws AvatarNotFoundException;

  AvatarHealth getHealth(Id<Avatar> avatarId) throws AvatarNotFoundException;

  AvatarMana getMana(Id<Avatar> avatarId) throws AvatarNotFoundException;

  AvatarStats getAvatarStats(Id<Avatar> avatarId) throws AvatarNotFoundException;

  // --- Money ---
  void earnMoney(Id<Avatar> avatarId, Integer amount) throws AvatarNotFoundException;

  void spendMoney(Id<Avatar> avatarId, Integer amount) throws AvatarNotFoundException;

  // --- Inventory ---
  void addToInventory(Id<Avatar> avatarId, Item item) throws AvatarNotFoundException;

  void removeItem(Id<Avatar> avatarId, Item item) throws AvatarNotFoundException;

  void equipItem(Id<Avatar> avatarId, Item item) throws AvatarNotFoundException;

  void unequipItem(Id<Avatar> avatarId, Item item) throws AvatarNotFoundException;

  // --- Combat ---
  boolean applyDamage(Id<Avatar> avatarId, Integer amount) throws AvatarNotFoundException;

  void useHealthPotion(Id<Avatar> avatarId, String potionName) throws AvatarNotFoundException;

  void useManaPotion(Id<Avatar> avatarId, String potionName) throws AvatarNotFoundException;

  void healAvatar(Id<Avatar> avatarId, Integer amount) throws AvatarNotFoundException;

  void spendMana(Id<Avatar> avatarId, Integer amount) throws AvatarNotFoundException;

  void restoreMana(Id<Avatar> avatarId, Integer amount) throws AvatarNotFoundException;

  // --- Progression ---
  void grantExperience(Id<Avatar> avatarId, Integer amount) throws AvatarNotFoundException;

  void increaseStrength(Id<Avatar> avatarId) throws AvatarNotFoundException;

  void increaseDefense(Id<Avatar> avatarId) throws AvatarNotFoundException;

  void increaseIntelligence(Id<Avatar> avatarId) throws AvatarNotFoundException;
}
