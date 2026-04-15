package habitquest.avatar.application.service;

import common.ddd.Id;
import habitquest.avatar.application.exceptions.AvatarNotFoundException;
import habitquest.avatar.application.port.in.AvatarQueryService;
import habitquest.avatar.application.port.out.AvatarRepository;
import habitquest.avatar.domain.avatar.*;
import habitquest.avatar.domain.items.Equipment;
import habitquest.avatar.domain.items.Item;
import habitquest.avatar.domain.stats.*;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class AvatarQueryServiceImpl implements AvatarQueryService {

  private final AvatarRepository avatarRepository;

  public AvatarQueryServiceImpl(AvatarRepository avatarRepository) {
    this.avatarRepository = avatarRepository;
  }

  @Override
  public Avatar getAvatarById(Id<Avatar> id) throws AvatarNotFoundException {
    return avatarRepository.findById(id).orElseThrow(() -> new AvatarNotFoundException(id.value()));
  }

  @Override
  public List<Avatar> searchAvatars(AvatarSearchQuery criteria) {
    return avatarRepository.search(criteria);
  }

  @Override
  public String getName(Id<Avatar> avatarId) throws AvatarNotFoundException {
    return getAvatarById(avatarId).getName();
  }

  @Override
  public Money getMoney(Id<Avatar> avatarId) throws AvatarNotFoundException {
    return getAvatarById(avatarId).getMoney();
  }

  @Override
  public List<Item> getInventory(Id<Avatar> avatarId) throws AvatarNotFoundException {
    return getAvatarById(avatarId).getInventory();
  }

  @Override
  public List<Equipment> getEquippedItems(Id<Avatar> avatarId) throws AvatarNotFoundException {
    return getAvatarById(avatarId).getEquippedItems();
  }

  @Override
  public Experience getExperience(Id<Avatar> avatarId) throws AvatarNotFoundException {
    return getAvatarById(avatarId).getLevel().currentExperience();
  }

  @Override
  public Level getLevel(Id<Avatar> avatarId) throws AvatarNotFoundException {
    return getAvatarById(avatarId).getLevel();
  }

  @Override
  public AvatarHealth getHealth(Id<Avatar> avatarId) throws AvatarNotFoundException {
    return getAvatarById(avatarId).getHealth();
  }

  @Override
  public AvatarMana getMana(Id<Avatar> avatarId) throws AvatarNotFoundException {
    return getAvatarById(avatarId).getMana();
  }

  @Override
  public AvatarStats getAvatarStats(Id<Avatar> avatarId) throws AvatarNotFoundException {
    return getAvatarById(avatarId).getAvatarStats();
  }
}
