package habitquest.avatar.application;

import common.hexagonal.Adapter;
import habitquest.avatar.domain.avatar.*;
import habitquest.avatar.domain.events.*;
import habitquest.avatar.domain.factory.AvatarFactory;
import habitquest.avatar.domain.items.EquippedItems;
import habitquest.avatar.domain.items.Inventory;
import habitquest.avatar.domain.stats.AvatarStats;

@Adapter
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
    return getAvatarById(avatarId).getExperience();
  }

  @Override
  public Level getLevel(String avatarId) throws AvatarNotFoundExpection {
    return getAvatarById(avatarId).getLevel();
  }

  @Override
  public Health getHealth(String avatarId) throws AvatarNotFoundExpection {
    return getAvatarById(avatarId).getHealth();
  }

  @Override
  public Mana getMana(String avatarId) throws AvatarNotFoundExpection {
    return getAvatarById(avatarId).getMana();
  }

  @Override
  public AvatarStats getAvatarStats(String avatarId) throws AvatarNotFoundExpection {
    return getAvatarById(avatarId).getAvatarStats();
  }

  @Override
  public void updateName(String avatarId, String name) throws AvatarNotFoundExpection {
    Avatar avatar = getAvatarById(avatarId);
    avatar.setName(name);
    avatarRepository.save(avatar);
  }

  @Override
  public void updateMoney(String avatarId, Money money) throws AvatarNotFoundExpection {
    Avatar avatar = getAvatarById(avatarId);
    avatar.setMoney(money);
    avatarRepository.save(avatar);
  }

  @Override
  public void updateInventory(String avatarId, Inventory inventory) throws AvatarNotFoundExpection {
    Avatar avatar = getAvatarById(avatarId);
    avatar.setInventory(inventory);
    avatarRepository.save(avatar);
  }

  @Override
  public void updateEquippedItems(String avatarId, EquippedItems equippedItems)
      throws AvatarNotFoundExpection {
    Avatar avatar = getAvatarById(avatarId);
    avatar.setEquippedItems(equippedItems);
    avatarRepository.save(avatar);
  }

  @Override
  public void updateExperience(String avatarId, Experience experience)
      throws AvatarNotFoundExpection {
    Avatar avatar = getAvatarById(avatarId);
    avatar.setExperience(experience);
    if (avatar.getLevel().canLevelUp(experience)) {
      this.updateLevel(avatarId);
      avatarObserver.notifyAvaterEvent(new LevelUpped(avatar.getLevel()));
    }
    avatarRepository.save(avatar);
  }

  private void updateLevel(String avatarId) throws AvatarNotFoundExpection {
    Avatar avatar = getAvatarById(avatarId);
    avatar.setLevel(
        new Level(
            avatar.getLevel().levelNumber() + 1,
            new Experience(avatar.getExperience().amount() * 2)));
    avatarRepository.save(avatar);
  }

  @Override
  public void updateHealth(String avatarId, Health health) throws AvatarNotFoundExpection {
    Avatar avatar = getAvatarById(avatarId);
    avatar.setHealth(health);
    if (health.isDead()) {
      this.die(avatarId);
      avatarObserver.notifyAvaterEvent(new Dead(health));
    }
    avatarRepository.save(avatar);
  }

  private void die(String avatarId) throws AvatarNotFoundExpection {
    Avatar avatar = getAvatarById(avatarId);
    avatar.setHealth(new Health(avatar.getHealth().max(), avatar.getHealth().max()));
    avatar.setMana(new Mana(avatar.getMana().max(), avatar.getMana().max()));
    avatar.setExperience(new Experience(0));
    avatar.setMoney(avatar.getMoney().subtract(new Money(avatar.getMoney().amount() / 10)));
  }

  @Override
  public void updateMana(String avatarId, Mana mana) throws AvatarNotFoundExpection {
    Avatar avatar = getAvatarById(avatarId);
    avatar.setMana(mana);
    avatarRepository.save(avatar);
  }

  @Override
  public void updateAvatarStats(String avatarId, AvatarStats avatarStats)
      throws AvatarNotFoundExpection {
    Avatar avatar = getAvatarById(avatarId);
    avatar.setAvatarStats(avatarStats);
    avatarRepository.save(avatar);
    avatarObserver.notifyAvaterEvent(new SkillPointAssigned(avatarStats));
  }
}
