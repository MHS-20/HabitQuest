package habitquest.avatar.application.service;

import common.ddd.Id;
import habitquest.avatar.application.exceptions.AvatarNotFoundException;
import habitquest.avatar.application.exceptions.ItemNotFoundException;
import habitquest.avatar.application.port.in.AvatarCommandService;
import habitquest.avatar.application.port.out.AvatarRepository;
import habitquest.avatar.application.port.out.MarketplaceClientPort;
import habitquest.avatar.domain.avatar.*;
import habitquest.avatar.domain.events.*;
import habitquest.avatar.domain.factory.AvatarFactory;
import habitquest.avatar.domain.items.Equipment;
import habitquest.avatar.domain.items.HealthPotion;
import habitquest.avatar.domain.items.Item;
import habitquest.avatar.domain.items.ManaPotion;
import habitquest.avatar.domain.spells.Spell;
import org.springframework.stereotype.Service;

@Service
public class AvatarCommandServiceImpl implements AvatarCommandService {

  private final AvatarFactory avatarFactory;
  private final AvatarRepository avatarRepository;
  private final AvatarObserver avatarObserver;
  private final MarketplaceClientPort marketplacePort;

  public AvatarCommandServiceImpl(
      AvatarFactory avatarFactory,
      AvatarRepository avatarRepository,
      AvatarObserver avatarObserver,
      MarketplaceClientPort marketplacePort) {
    this.avatarFactory = avatarFactory;
    this.avatarRepository = avatarRepository;
    this.avatarObserver = avatarObserver;
    this.marketplacePort = marketplacePort;
  }

  private Avatar getAvatarById(Id<Avatar> id) throws AvatarNotFoundException {
    return avatarRepository.findById(id).orElseThrow(() -> new AvatarNotFoundException(id.value()));
  }

  @Override
  public Id<Avatar> createAvatar(Id<Avatar> id, String name) {
    Avatar avatar = this.avatarFactory.create(id, name);
    marketplacePort.createMarketplace(id.value());
    avatarRepository.save(avatar);
    return avatar.getId();
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
  public void updateName(Id<Avatar> avatarId, String name) throws AvatarNotFoundException {
    Avatar avatar = getAvatarById(avatarId);
    avatar.rename(name);
    avatarRepository.save(avatar);
  }

  @Override
  public void spendMoney(Id<Avatar> avatarId, Money money) throws AvatarNotFoundException {
    Avatar avatar = getAvatarById(avatarId);
    avatar.spendMoney(money);
    avatarRepository.save(avatar);
  }

  @Override
  public void earnMoney(Id<Avatar> avatarId, Money money) throws AvatarNotFoundException {
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
  public void equipItem(Id<Avatar> avatarId, Equipment item) throws AvatarNotFoundException {
    Avatar avatar = getAvatarById(avatarId);
    avatar.equipItem(item);
    avatarRepository.save(avatar);
  }

  @Override
  public void unequipItem(Id<Avatar> avatarId, Equipment item) throws AvatarNotFoundException {
    Avatar avatar = getAvatarById(avatarId);
    avatar.unequipItem(item);
    avatarRepository.save(avatar);
  }

  @Override
  public boolean applyDamage(Id<Avatar> avatarId, Damage amount) throws AvatarNotFoundException {
    Avatar avatar = getAvatarById(avatarId);
    boolean died = avatar.takeDamage(amount);
    if (died) {
      avatarObserver.notifyAvatarEvent(new Dead(avatarId));
    }
    avatarRepository.save(avatar);
    return died;
  }

  @Override
  public void useHealthPotion(Id<Avatar> avatarId, HealthPotion potion)
      throws AvatarNotFoundException {
    Avatar avatar = getAvatarById(avatarId);
    if (!avatar.getInventory().contains(potion)) {
      throw new ItemNotFoundException(potion.name());
    }
    avatar.heal(new Health(potion.power()));
    avatar.removeItemFromInventory(potion);
    avatarRepository.save(avatar);
  }

  @Override
  public void useManaPotion(Id<Avatar> avatarId, ManaPotion potion) throws AvatarNotFoundException {
    Avatar avatar = getAvatarById(avatarId);
    if (!avatar.getInventory().contains(potion)) {
      throw new ItemNotFoundException(potion.name());
    }
    avatar.restoreMana(new Mana(potion.power()));
    avatar.removeItemFromInventory(potion);
    avatarRepository.save(avatar);
  }

  @Override
  public void spendMana(Id<Avatar> avatarId, Mana amount) throws AvatarNotFoundException {
    Avatar avatar = getAvatarById(avatarId);
    avatar.spendMana(new Mana(amount.value()));
    avatarRepository.save(avatar);
  }

  @Override
  public void grantExperience(Id<Avatar> avatarId, Experience amount)
      throws AvatarNotFoundException {
    Avatar avatar = getAvatarById(avatarId);
    Level levelBefore = avatar.getLevel();
    avatar.gainExperience(amount.amount());
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
