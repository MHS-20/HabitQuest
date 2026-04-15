package habitquest.avatar.application.port.in;

import common.ddd.Id;
import common.hexagonal.InBoundPort;
import habitquest.avatar.application.exceptions.AvatarNotFoundException;
import habitquest.avatar.application.service.AvatarSearchQuery;
import habitquest.avatar.domain.avatar.*;
import habitquest.avatar.domain.items.Equipment;
import habitquest.avatar.domain.items.Item;
import habitquest.avatar.domain.stats.AvatarStats;
import java.util.List;

@InBoundPort
public interface AvatarQueryService {

  Avatar getAvatarById(Id<Avatar> id) throws AvatarNotFoundException;

  List<Avatar> searchAvatars(AvatarSearchQuery criteria);

  String getName(Id<Avatar> avatarId) throws AvatarNotFoundException;

  Money getMoney(Id<Avatar> avatarId) throws AvatarNotFoundException;

  List<Item> getInventory(Id<Avatar> avatarId) throws AvatarNotFoundException;

  List<Equipment> getEquippedItems(Id<Avatar> avatarId) throws AvatarNotFoundException;

  Experience getExperience(Id<Avatar> avatarId) throws AvatarNotFoundException;

  Level getLevel(Id<Avatar> avatarId) throws AvatarNotFoundException;

  AvatarHealth getHealth(Id<Avatar> avatarId) throws AvatarNotFoundException;

  AvatarMana getMana(Id<Avatar> avatarId) throws AvatarNotFoundException;

  AvatarStats getAvatarStats(Id<Avatar> avatarId) throws AvatarNotFoundException;
}
