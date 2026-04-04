package habitquest.avatar.application;

import common.ddd.Id;
import habitquest.avatar.domain.avatar.*;
import habitquest.avatar.domain.events.*;
import habitquest.avatar.domain.factory.AvatarFactory;
import habitquest.avatar.domain.items.*;
import habitquest.avatar.domain.spells.Spell;
import habitquest.avatar.domain.stats.AvatarStats;
import java.util.List;
import org.springframework.stereotype.Service;

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
  public Id<Avatar> createAvatar(Id<Avatar> id, String name) {
    Avatar avatar = this.avatarFactory.create(id, name);
    avatarRepository.save(avatar);
    return avatar.getId();
  }

  @Override
  public Avatar getAvatarById(Id<Avatar> id) throws AvatarNotFoundException {
    return avatarRepository.findById(id).orElseThrow(() -> new AvatarNotFoundException(id.value()));
  }

  @Override
  public List<Avatar> searchAvatars(AvatarSearchRequest criteria) {
    return avatarRepository.search(criteria);
  }

  @Override
  public void addPendingInvite(Id<Avatar> avatarId, Invite invite) throws AvatarNotFoundException {
    Avatar avatar = getAvatarById(avatarId);
    avatar.addPendingGuildInvite(invite);
    avatarRepository.save(avatar);
  }

  @Override
  public void acceptInvite(Id<Avatar> avatarId, Id<Invite> inviteId)
      throws AvatarNotFoundException {
    Avatar avatar = getAvatarById(avatarId);
    avatar.acceptGuildInvite(inviteId);
    avatarRepository.save(avatar);
  }

  @Override
  public void deleteAvatar(Id<Avatar> id) throws AvatarNotFoundException {
    avatarRepository.findById(id).orElseThrow(() -> new AvatarNotFoundException(id.value()));
    avatarRepository.deleteById(id);
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
  public Inventory getInventory(Id<Avatar> avatarId) throws AvatarNotFoundException {
    return getAvatarById(avatarId).getInventory();
  }

  @Override
  public EquippedItems getEquippedItems(Id<Avatar> avatarId) throws AvatarNotFoundException {
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

  @Override
  public void updateName(Id<Avatar> avatarId, String name) throws AvatarNotFoundException {
    Avatar avatar = getAvatarById(avatarId);
    avatar.rename(name);
    avatarRepository.save(avatar);
  }

  @Override
  public void spendMoney(Id<Avatar> avatarId, Integer money) throws AvatarNotFoundException {
    Avatar avatar = getAvatarById(avatarId);
    avatar.spendMoney(money);
    avatarRepository.save(avatar);
  }

  @Override
  public void earnMoney(Id<Avatar> avatarId, Integer money) throws AvatarNotFoundException {
    Avatar avatar = getAvatarById(avatarId);
    avatar.earnMoney(money);
    avatarRepository.save(avatar);
  }

  @Override
  public void addToInventory(Id<Avatar> avatarId, Item item) throws AvatarNotFoundException {
    Avatar avatar = getAvatarById(avatarId);
    avatar.addItemToInventory(item);
    avatarRepository.save(avatar);
  }

  @Override
  public void removeItem(Id<Avatar> avatarId, Item item) throws AvatarNotFoundException {
    Avatar avatar = getAvatarById(avatarId);
    avatar.removeItemFromInventory(item);
    avatarRepository.save(avatar);
  }

  @Override
  public void equipItem(Id<Avatar> avatarId, Item item) throws AvatarNotFoundException {
    Avatar avatar = getAvatarById(avatarId);
    avatar.equipItem(item);
    avatarRepository.save(avatar);
  }

  @Override
  public void unequipItem(Id<Avatar> avatarId, Item item) throws AvatarNotFoundException {
    Avatar avatar = getAvatarById(avatarId);
    avatar.unequipItem(item);
    avatarRepository.save(avatar);
  }

  @Override
  public boolean applyDamage(Id<Avatar> avatarId, Integer amount) throws AvatarNotFoundException {
    Avatar avatar = getAvatarById(avatarId);
    boolean died = avatar.takeDamage(amount);
    if (died) {
      avatarObserver.notifyAvatarEvent(new Dead(avatarId));
    }
    avatarRepository.save(avatar);
    return died;
  }

  @Override
  public void useHealthPotion(Id<Avatar> avatarId, String potionName)
      throws AvatarNotFoundException {
    Avatar avatar = getAvatarById(avatarId);
    HealthPotion potion =
        avatar.getInventory().getItems().stream()
            .filter(item -> item instanceof HealthPotion)
            .map(item -> (HealthPotion) item)
            .filter(p -> p.name().equals(potionName))
            .findFirst()
            .orElseThrow(
                () -> new IllegalArgumentException("No potion found with name: " + potionName));
    avatar.heal(potion.healingPower());
    avatar.removeItemFromInventory(potion);
  }

  @Override
  public void useManaPotion(Id<Avatar> avatarId, String potionName) throws AvatarNotFoundException {
    Avatar avatar = getAvatarById(avatarId);
    ManaPotion potion =
        avatar.getInventory().getItems().stream()
            .filter(item -> item instanceof ManaPotion)
            .map(item -> (ManaPotion) item)
            .filter(p -> p.name().equals(potionName))
            .findFirst()
            .orElseThrow(
                () -> new IllegalArgumentException("No potion found with name: " + potionName));
    avatar.restoreMana(potion.getValue());
    avatar.removeItemFromInventory(potion);
  }

  @Override
  public void healAvatar(Id<Avatar> avatarId, Integer amount) throws AvatarNotFoundException {
    Avatar avatar = getAvatarById(avatarId);
    avatar.heal(amount);
    avatarRepository.save(avatar);
  }

  @Override
  public void spendMana(Id<Avatar> avatarId, Integer amount) throws AvatarNotFoundException {
    Avatar avatar = getAvatarById(avatarId);
    avatar.spendMana(amount);
    avatarRepository.save(avatar);
  }

  @Override
  public void restoreMana(Id<Avatar> avatarId, Integer amount) throws AvatarNotFoundException {
    Avatar avatar = getAvatarById(avatarId);
    avatar.restoreMana(amount);
    avatarRepository.save(avatar);
  }

  @Override
  public void grantExperience(Id<Avatar> avatarId, Integer amount) throws AvatarNotFoundException {
    Avatar avatar = getAvatarById(avatarId);
    Level levelBefore = avatar.getLevel();
    avatar.gainExperience(amount);
    Level levelAfter = avatar.getLevel();
    avatarRepository.save(avatar);
    if (levelAfter.levelNumber() > levelBefore.levelNumber()) {
      avatarObserver.notifyAvatarEvent(new LevelUpped(avatarId, avatar.getLevel()));
    }

    Spell.unlockedAtLevel(levelAfter)
        .ifPresent(
            spell -> {
              avatar.learnSpell(spell);
              avatarRepository.save(avatar);
              avatarObserver.notifyAvatarEvent(new NewSpellLearned(avatarId, spell));
            });
  }

  @Override
  public void increaseStrength(Id<Avatar> avatarId) throws AvatarNotFoundException {
    Avatar avatar = getAvatarById(avatarId);
    avatar.incrementStrength();
    avatarRepository.save(avatar);
    avatarObserver.notifyAvatarEvent(
        new SkillPointAssigned(avatarId, avatar.getAvatarStats().getStrength()));
  }

  @Override
  public void increaseDefense(Id<Avatar> avatarId) throws AvatarNotFoundException {
    Avatar avatar = getAvatarById(avatarId);
    avatar.incrementDefense();
    avatarRepository.save(avatar);
    avatarObserver.notifyAvatarEvent(
        new SkillPointAssigned(avatarId, avatar.getAvatarStats().getDefense()));
  }

  @Override
  public void increaseIntelligence(Id<Avatar> avatarId) throws AvatarNotFoundException {
    Avatar avatar = getAvatarById(avatarId);
    avatar.incrementIntelligence();
    avatarRepository.save(avatar);
    avatarObserver.notifyAvatarEvent(
        new SkillPointAssigned(avatarId, avatar.getAvatarStats().getIntelligence()));
  }
}
