package habitquest.avatar.application;

import common.ddd.Id;
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

  Inventory getInventory(Id<Avatar> avatarId) throws AvatarNotFoundException;

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

  void healAvatar(Id<Avatar> avatarId, Integer amount) throws AvatarNotFoundException;

  void spendMana(Id<Avatar> avatarId, Integer amount) throws AvatarNotFoundException;

  void restoreMana(Id<Avatar> avatarId, Integer amount) throws AvatarNotFoundException;

  // --- Progression ---
  void grantExperience(Id<Avatar> avatarId, Integer amount) throws AvatarNotFoundException;

  void increaseStrength(Id<Avatar> avatarId) throws AvatarNotFoundException;

  void increaseDefense(Id<Avatar> avatarId) throws AvatarNotFoundException;

  void increaseIntelligence(Id<Avatar> avatarId) throws AvatarNotFoundException;

  // --- String overloads for backward compatibility ---
  default String createAvatar(String id, String name) {
    return createAvatar(new Id<>(id), name).value();
  }

  default Avatar getAvatarById(String id) throws AvatarNotFoundException {
    return getAvatarById(new Id<>(id));
  }

  default void deleteAvatar(String id) throws AvatarNotFoundException {
    deleteAvatar(new Id<>(id));
  }

  default void updateName(String avatarId, String name) throws AvatarNotFoundException {
    updateName(new Id<>(avatarId), name);
  }

  default String getName(String avatarId) throws AvatarNotFoundException {
    return getName(new Id<>(avatarId));
  }

  default Money getMoney(String avatarId) throws AvatarNotFoundException {
    return getMoney(new Id<>(avatarId));
  }

  default Inventory getInventory(String avatarId) throws AvatarNotFoundException {
    return getInventory(new Id<>(avatarId));
  }

  default EquippedItems getEquippedItems(String avatarId) throws AvatarNotFoundException {
    return getEquippedItems(new Id<>(avatarId));
  }

  default Experience getExperience(String avatarId) throws AvatarNotFoundException {
    return getExperience(new Id<>(avatarId));
  }

  default Level getLevel(String avatarId) throws AvatarNotFoundException {
    return getLevel(new Id<>(avatarId));
  }

  default AvatarHealth getHealth(String avatarId) throws AvatarNotFoundException {
    return getHealth(new Id<>(avatarId));
  }

  default AvatarMana getMana(String avatarId) throws AvatarNotFoundException {
    return getMana(new Id<>(avatarId));
  }

  default AvatarStats getAvatarStats(String avatarId) throws AvatarNotFoundException {
    return getAvatarStats(new Id<>(avatarId));
  }

  default void earnMoney(String avatarId, Integer amount) throws AvatarNotFoundException {
    earnMoney(new Id<>(avatarId), amount);
  }

  default void spendMoney(String avatarId, Integer amount) throws AvatarNotFoundException {
    spendMoney(new Id<>(avatarId), amount);
  }

  default void addToInventory(String avatarId, Item item) throws AvatarNotFoundException {
    addToInventory(new Id<>(avatarId), item);
  }

  default void removeItem(String avatarId, Item item) throws AvatarNotFoundException {
    removeItem(new Id<>(avatarId), item);
  }

  default void equipItem(String avatarId, Item item) throws AvatarNotFoundException {
    equipItem(new Id<>(avatarId), item);
  }

  default void unequipItem(String avatarId, Item item) throws AvatarNotFoundException {
    unequipItem(new Id<>(avatarId), item);
  }

  default boolean applyDamage(String avatarId, Integer amount) throws AvatarNotFoundException {
    return applyDamage(new Id<>(avatarId), amount);
  }

  default void healAvatar(String avatarId, Integer amount) throws AvatarNotFoundException {
    healAvatar(new Id<>(avatarId), amount);
  }

  default void spendMana(String avatarId, Integer amount) throws AvatarNotFoundException {
    spendMana(new Id<>(avatarId), amount);
  }

  default void restoreMana(String avatarId, Integer amount) throws AvatarNotFoundException {
    restoreMana(new Id<>(avatarId), amount);
  }

  default void grantExperience(String avatarId, Integer amount) throws AvatarNotFoundException {
    grantExperience(new Id<>(avatarId), amount);
  }

  default void increaseStrength(String avatarId) throws AvatarNotFoundException {
    increaseStrength(new Id<>(avatarId));
  }

  default void increaseDefense(String avatarId) throws AvatarNotFoundException {
    increaseDefense(new Id<>(avatarId));
  }

  default void increaseIntelligence(String avatarId) throws AvatarNotFoundException {
    increaseIntelligence(new Id<>(avatarId));
  }
}
