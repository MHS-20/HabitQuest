package habitquest.avatar.application.port.in;

import common.ddd.Id;
import common.hexagonal.InBoundPort;
import habitquest.avatar.application.exceptions.AvatarNotFoundException;
import habitquest.avatar.domain.avatar.*;
import habitquest.avatar.domain.avatar.Avatar;
import habitquest.avatar.domain.avatar.Invite;
import habitquest.avatar.domain.items.Equipment;
import habitquest.avatar.domain.items.HealthPotion;
import habitquest.avatar.domain.items.Item;
import habitquest.avatar.domain.items.ManaPotion;

@InBoundPort
public interface AvatarCommandService {

  Id<Avatar> createAvatar(Id<Avatar> id, String name);

  void addPendingInvite(Id<Avatar> avatarId, Invite invite) throws AvatarNotFoundException;

  void acceptInvite(Id<Avatar> avatarId, Id<Invite> inviteId) throws AvatarNotFoundException;

  void deleteAvatar(Id<Avatar> id) throws AvatarNotFoundException;

  void updateName(Id<Avatar> avatarId, String name) throws AvatarNotFoundException;

  void spendMoney(Id<Avatar> avatarId, Money money) throws AvatarNotFoundException;

  void earnMoney(Id<Avatar> avatarId, Money money) throws AvatarNotFoundException;

  void addToInventory(Id<Avatar> avatarId, Item item) throws AvatarNotFoundException;

  void removeItem(Id<Avatar> avatarId, Item item) throws AvatarNotFoundException;

  void equipItem(Id<Avatar> avatarId, Equipment item) throws AvatarNotFoundException;

  void unequipItem(Id<Avatar> avatarId, Equipment item) throws AvatarNotFoundException;

  boolean applyDamage(Id<Avatar> avatarId, Damage amount) throws AvatarNotFoundException;

  void useHealthPotion(Id<Avatar> avatarId, HealthPotion potion) throws AvatarNotFoundException;

  void useManaPotion(Id<Avatar> avatarId, ManaPotion potion) throws AvatarNotFoundException;

  void spendMana(Id<Avatar> avatarId, Mana amount) throws AvatarNotFoundException;

  void grantExperience(Id<Avatar> avatarId, Experience amount) throws AvatarNotFoundException;

  void increaseStrength(Id<Avatar> avatarId) throws AvatarNotFoundException;

  void increaseDefense(Id<Avatar> avatarId) throws AvatarNotFoundException;

  void increaseIntelligence(Id<Avatar> avatarId) throws AvatarNotFoundException;
}
