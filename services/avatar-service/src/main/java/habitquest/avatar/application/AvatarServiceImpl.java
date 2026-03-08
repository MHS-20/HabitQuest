package habitquest.avatar.application;

import common.hexagonal.Adapter;
import habitquest.avatar.domain.avatar.*;
import habitquest.avatar.domain.events.*;
import habitquest.avatar.domain.factory.AvatarFactory;
import habitquest.avatar.domain.items.EquippedItems;
import habitquest.avatar.domain.items.Inventory;
import habitquest.avatar.domain.items.Item;
import habitquest.avatar.domain.stats.AvatarStats;
import org.springframework.stereotype.Service;

@Adapter
@Service
public class AvatarServiceImpl implements AvatarService {

  private final AvatarFactory avatarFactory;
  private final AvatarRepository avatarRepository;
  private final AvatarObserver avatarObserver;

  public AvatarServiceImpl(
      AvatarFactory avatarFactory,
      AvatarRepository avatarRepository,
      AvatarObserver avatarObserver) {
    this.avatarFactory = avatarFactory;
    this.avatarRepository = avatarRepository;
    this.avatarObserver = avatarObserver;
  }

  @Override
  public String createAvatar(String name) {
    Avatar avatar = this.avatarFactory.create(name);
    avatarRepository.save(avatar);
    return avatar.getId();
  }

  @Override
  public Avatar getAvatarById(String id) throws AvatarNotFoundExpection {
    return avatarRepository.findById(id).orElseThrow(() -> new AvatarNotFoundExpection(id));
  }

  @Override
  public void updateAvatar(String id, Avatar updatedAvatar) throws AvatarNotFoundExpection {
    avatarRepository.findById(id).orElseThrow(() -> new AvatarNotFoundExpection(id));
    avatarRepository.save(updatedAvatar);
  }

  @Override
  public void deleteAvatar(String id) throws AvatarNotFoundExpection {
    avatarRepository.findById(id).orElseThrow(() -> new AvatarNotFoundExpection(id));
    avatarRepository.deleteById(id);
  }

  @Override
  public String getName(String avatarId) throws AvatarNotFoundExpection {
    return getAvatarById(avatarId).getName();
  }

  @Override
  public Money getMoney(String avatarId) throws AvatarNotFoundExpection {
    return getAvatarById(avatarId).getMoney();
  }

  @Override
  public Inventory getInventory(String avatarId) throws AvatarNotFoundExpection {
    return getAvatarById(avatarId).getInventory();
  }

  @Override
  public EquippedItems getEquippedItems(String avatarId) throws AvatarNotFoundExpection {
    return getAvatarById(avatarId).getEquippedItems();
  }

  @Override
  public Experience getExperience(String avatarId) throws AvatarNotFoundExpection {
    return getAvatarById(avatarId).getLevel().currentExperience();
  }

  @Override
  public Level getLevel(String avatarId) throws AvatarNotFoundExpection {
    return getAvatarById(avatarId).getLevel();
  }

  @Override
  public AvatarHealth getHealth(String avatarId) throws AvatarNotFoundExpection {
    return getAvatarById(avatarId).getHealth();
  }

  @Override
  public AvatarMana getMana(String avatarId) throws AvatarNotFoundExpection {
    return getAvatarById(avatarId).getMana();
  }

  @Override
  public AvatarStats getAvatarStats(String avatarId) throws AvatarNotFoundExpection {
    return getAvatarById(avatarId).getAvatarStats();
  }

  @Override
  public void updateName(String avatarId, String name) throws AvatarNotFoundExpection {
    Avatar avatar = getAvatarById(avatarId);
    avatar.rename(name);
    avatarRepository.save(avatar);
  }

  @Override
  public void spendMoney(String avatarId, Integer money) throws AvatarNotFoundExpection {
    Avatar avatar = getAvatarById(avatarId);
    avatar.spendMoney(money);
    avatarRepository.save(avatar);
  }

  @Override
  public void earnMoney(String avatarId, Integer money) throws AvatarNotFoundExpection {
    Avatar avatar = getAvatarById(avatarId);
    avatar.earnMoney(money);
    avatarRepository.save(avatar);
  }

  @Override
  public void addToInventory(String avatarId, Item item) throws AvatarNotFoundExpection {
    Avatar avatar = getAvatarById(avatarId);
    avatar.addItemToInventory(item);
    avatarRepository.save(avatar);
  }

  @Override
  public void removeItem(String avatarId, Item item) throws AvatarNotFoundExpection {
    Avatar avatar = getAvatarById(avatarId);
    avatar.removeItemFromInventory(item);
    avatarRepository.save(avatar);
  }

  @Override
  public void equipItem(String avatarId, Item item) throws AvatarNotFoundExpection {
    Avatar avatar = getAvatarById(avatarId);
    avatar.equipItem(item);
    avatarRepository.save(avatar);
  }

  @Override
  public void unequipItem(String avatarId, Item item) throws AvatarNotFoundExpection {
    Avatar avatar = getAvatarById(avatarId);
    avatar.unequipItem(item);
    avatarRepository.save(avatar);
  }

  @Override
  public void applyDamage(String avatarId, Integer amount) throws AvatarNotFoundExpection {
    Avatar avatar = getAvatarById(avatarId);
    avatar.takeDamage(amount);
    if (avatar.getLevel().currentExperience().amount() == 0) {
      avatarObserver.notifyAvaterEvent(new Dead(avatarId));
    }
    avatarRepository.save(avatar);
  }

  @Override
  public void healAvatar(String avatarId, Integer amount) throws AvatarNotFoundExpection {
    Avatar avatar = getAvatarById(avatarId);
    avatar.heal(amount);
    avatarRepository.save(avatar);
  }

  @Override
  public void spendMana(String avatarId, Integer amount) throws AvatarNotFoundExpection {
    Avatar avatar = getAvatarById(avatarId);
    avatar.spendMana(amount);
    avatarRepository.save(avatar);
  }

  @Override
  public void restoreMana(String avatarId, Integer amount) throws AvatarNotFoundExpection {
    Avatar avatar = getAvatarById(avatarId);
    avatar.restoreMana(amount);
    avatarRepository.save(avatar);
  }

  @Override
  public void grantExperience(String avatarId, Integer amount) throws AvatarNotFoundExpection {
    Avatar avatar = getAvatarById(avatarId);
    Level levelBefore = avatar.getLevel();
    avatar.gainExperience(amount);
    Level levelAfter = avatar.getLevel();
    avatarRepository.save(avatar);
    if (levelAfter.levelNumber() > levelBefore.levelNumber()) {
      avatarObserver.notifyAvaterEvent(new LevelUpped(avatar.getLevel()));
    }
  }

  @Override
  public void increaseStrength(String avatarId) throws AvatarNotFoundExpection {
    Avatar avatar = getAvatarById(avatarId);
    avatar.getAvatarStats().getStrength().increment();
    avatarRepository.save(avatar);
    avatarObserver.notifyAvaterEvent(new SkillPointAssigned(avatar.getAvatarStats().getStrength()));
  }

  @Override
  public void increaseDefense(String avatarId) throws AvatarNotFoundExpection {
    Avatar avatar = getAvatarById(avatarId);
    avatar.getAvatarStats().getDefense().increment();
    avatarRepository.save(avatar);
    avatarObserver.notifyAvaterEvent(new SkillPointAssigned(avatar.getAvatarStats().getDefense()));
  }

  @Override
  public void increaseIntelligence(String avatarId) throws AvatarNotFoundExpection {
    Avatar avatar = getAvatarById(avatarId);
    avatar.getAvatarStats().getIntelligence().increment();
    avatarRepository.save(avatar);
    avatarObserver.notifyAvaterEvent(
        new SkillPointAssigned(avatar.getAvatarStats().getIntelligence()));
  }
}
