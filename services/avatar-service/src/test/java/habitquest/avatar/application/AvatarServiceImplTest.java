package habitquest.avatar.application;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import common.ddd.Id;
import habitquest.avatar.domain.avatar.*;
import habitquest.avatar.domain.events.*;
import habitquest.avatar.domain.factory.AvatarFactory;
import habitquest.avatar.domain.items.EquippedItems;
import habitquest.avatar.domain.items.Inventory;
import habitquest.avatar.domain.items.Item;
import habitquest.avatar.domain.items.Weapon;
import habitquest.avatar.domain.spells.Spell;
import habitquest.avatar.domain.stats.AvatarStats;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("AvatarServiceImpl")
class AvatarServiceImplTest {

  private static final String AVATAR_ID = "avatar-1";
  private static final String AVATAR_NAME = "Hero";
  private static final String INVENTORY_ID = "inv-1";
  private static final String EQUIPPED_ID = "equip-1";
  private static final String STATS_ID = "stats-1";
  private static final String UNKNOWN_ID = "missing";
  private static final String BAD_ID = "bad";

  @Mock private AvatarFactory avatarFactory;
  @Mock private AvatarRepository avatarRepository;
  @Mock private AvatarObserver avatarObserver;

  private AvatarServiceImpl service;

  /** A fresh avatar wired to the same IDs every test uses. */
  private Avatar stubAvatar() {
    return new Avatar(
        new Id<>(AVATAR_ID),
        AVATAR_NAME,
        new Money(0),
        new Inventory(new Id<>(INVENTORY_ID)),
        new EquippedItems(new Id<>(EQUIPPED_ID)),
        new Level(1, new Experience(0), new Experience(100)),
        new AvatarHealth(new Health(100), new Health(100)),
        new AvatarMana(new Mana(50), new Mana(50)),
        new AvatarStats(new Id<>(STATS_ID), 10, 10, 10),
        new ArrayList<>());
  }

  /**
   * An avatar that starts at level 4, one level-up away from level 5 (the first spell tier). XP
   * required to reach level 5 is set to 100.
   */
  private Avatar stubAvatarAtLevel4() {
    return new Avatar(
        new Id<>(AVATAR_ID),
        AVATAR_NAME,
        new Money(0),
        new Inventory(new Id<>(INVENTORY_ID)),
        new EquippedItems(new Id<>(EQUIPPED_ID)),
        new Level(4, new Experience(0), new Experience(100)),
        new AvatarHealth(new Health(100), new Health(100)),
        new AvatarMana(new Mana(50), new Mana(50)),
        new AvatarStats(new Id<>(STATS_ID), 10, 10, 10),
        new ArrayList<>());
  }

  @BeforeEach
  void setUp() {
    service = new AvatarServiceImpl(avatarFactory, avatarRepository, avatarObserver);
  }

  // ─── createAvatar ────────────────────────────────────────────────────────────

  @Nested
  @DisplayName("createAvatar")
  class CreateAvatar {

    @Test
    @DisplayName("delegates to factory, saves, and returns the new id")
    void createsAndReturnsId() {
      Avatar avatar = stubAvatar();
      when(avatarFactory.create(new Id<>(AVATAR_ID), AVATAR_NAME)).thenReturn(avatar);
      when(avatarRepository.save(avatar)).thenReturn(avatar);

      Id<Avatar> id = service.createAvatar(new Id<>(AVATAR_ID), AVATAR_NAME);

      assertThat(id.value()).isEqualTo(AVATAR_ID);
      verify(avatarRepository).save(avatar);
    }
  }

  // ─── getAvatar ───────────────────────────────────────────────────────────

  @Nested
  @DisplayName("getAvatarById")
  class GetAvatarById {

    @Test
    @DisplayName("returns the avatar when found")
    void found() throws AvatarNotFoundException {
      Avatar avatar = stubAvatar();
      when(avatarRepository.findById(new Id<>(AVATAR_ID))).thenReturn(Optional.of(avatar));

      assertThat(service.getAvatarById(new Id<>(AVATAR_ID))).isSameAs(avatar);
    }

    @Test
    @DisplayName("throws AvatarNotFoundExpection when id is unknown")
    void notFound() {
      when(avatarRepository.findById(new Id<>(UNKNOWN_ID))).thenReturn(Optional.empty());

      assertThatThrownBy(() -> service.getAvatarById(new Id<>(UNKNOWN_ID)))
          .isInstanceOf(AvatarNotFoundException.class);
    }
  }

  // ─── deleteAvatar ────────────────────────────────────────────────────────────
  @Nested
  @DisplayName("deleteAvatar")
  class DeleteAvatar {

    @Test
    @DisplayName("calls deleteById after confirming existence")
    void deletes() throws AvatarNotFoundException {
      when(avatarRepository.findById(new Id<>(AVATAR_ID))).thenReturn(Optional.of(stubAvatar()));

      service.deleteAvatar(new Id<>(AVATAR_ID));

      verify(avatarRepository).deleteById(new Id<>(AVATAR_ID));
    }

    @Test
    @DisplayName("throws when id not found")
    void throwsWhenNotFound() {
      when(avatarRepository.findById(new Id<>(BAD_ID))).thenReturn(Optional.empty());

      assertThatThrownBy(() -> service.deleteAvatar(new Id<>(BAD_ID)))
          .isInstanceOf(AvatarNotFoundException.class);
    }
  }

  // ─── updateName ──────────────────────────────────────────────────────────────

  @Nested
  @DisplayName("updateName")
  class UpdateName {

    @Test
    @DisplayName("renames the avatar and persists it")
    void renames() throws AvatarNotFoundException {
      Avatar avatar = stubAvatar();
      when(avatarRepository.findById(new Id<>(AVATAR_ID))).thenReturn(Optional.of(avatar));

      service.updateName(new Id<>(AVATAR_ID), "Mage");

      assertThat(avatar.getName()).isEqualTo("Mage");
      verify(avatarRepository).save(avatar);
    }
  }

  // ─── Money ───────────────────────────────────────────────────────────────────

  @Nested
  @DisplayName("money operations")
  class MoneyOperations {

    @Test
    @DisplayName("earnMoney increases money and saves")
    void earnMoney() throws AvatarNotFoundException {
      Avatar avatar = stubAvatar();
      when(avatarRepository.findById(new Id<>(AVATAR_ID))).thenReturn(Optional.of(avatar));

      service.earnMoney(new Id<>(AVATAR_ID), 50);

      assertThat(avatar.getMoney().amount()).isEqualTo(50);
      verify(avatarRepository).save(avatar);
    }

    @Test
    @DisplayName("spendMoney decreases money and saves")
    void spendMoney() throws AvatarNotFoundException {
      Avatar avatar = stubAvatar();
      // give the avatar some money first via the domain method
      avatar.earnMoney(100);
      when(avatarRepository.findById(new Id<>(AVATAR_ID))).thenReturn(Optional.of(avatar));

      service.spendMoney(new Id<>(AVATAR_ID), 40);

      assertThat(avatar.getMoney().amount()).isEqualTo(60);
      verify(avatarRepository).save(avatar);
    }
  }

  // ─── Combat ──────────────────────────────────────────────────────────────────

  @Nested
  @DisplayName("applyDamage")
  class ApplyDamage {

    @Test
    @DisplayName("reduces avatar health and saves without firing Dead event when alive")
    void damageSurvived() throws AvatarNotFoundException {
      Avatar avatar = stubAvatar();
      when(avatarRepository.findById(new Id<>(AVATAR_ID))).thenReturn(Optional.of(avatar));

      service.applyDamage(new Id<>(AVATAR_ID), 10);

      assertThat(avatar.getHealth().current().value()).isEqualTo(90);
      verify(avatarObserver, never()).notifyAvatarEvent(any(Dead.class));
      verify(avatarRepository).save(avatar);
    }

    @Test
    @DisplayName("fires Dead event when avatar dies from damage")
    void damageLethal() throws AvatarNotFoundException {
      Avatar avatar = stubAvatar();
      avatar.earnMoney(200); // prevent money from going negative on death
      when(avatarRepository.findById(new Id<>(AVATAR_ID))).thenReturn(Optional.of(avatar));

      service.applyDamage(new Id<>(AVATAR_ID), 9999);

      ArgumentCaptor<AvatarEvent> captor = ArgumentCaptor.forClass(AvatarEvent.class);
      verify(avatarObserver).notifyAvatarEvent(captor.capture());
      assertThat(captor.getValue()).isInstanceOf(Dead.class);
      assertThat(((Dead) captor.getValue()).avatarId().value()).isEqualTo(AVATAR_ID);
    }
  }

  @Nested
  @DisplayName("healAvatar")
  class HealAvatar {

    @Test
    @DisplayName("increases health and saves")
    void heals() throws AvatarNotFoundException {
      Avatar avatar = stubAvatar();
      avatar.takeDamage(40);
      when(avatarRepository.findById(new Id<>(AVATAR_ID))).thenReturn(Optional.of(avatar));

      service.healAvatar(new Id<>(AVATAR_ID), 20);

      assertThat(avatar.getHealth().current().value()).isEqualTo(80);
      verify(avatarRepository).save(avatar);
    }
  }

  @Nested
  @DisplayName("mana operations")
  class ManaOperations {

    @Test
    @DisplayName("spendMana reduces mana and saves")
    void spendMana() throws AvatarNotFoundException {
      Avatar avatar = stubAvatar();
      when(avatarRepository.findById(new Id<>(AVATAR_ID))).thenReturn(Optional.of(avatar));

      service.spendMana(new Id<>(AVATAR_ID), 10);

      assertThat(avatar.getMana().amount().value()).isEqualTo(40);
      verify(avatarRepository).save(avatar);
    }

    @Test
    @DisplayName("restoreMana increases mana and saves")
    void restoreMana() throws AvatarNotFoundException {
      Avatar avatar = stubAvatar();
      avatar.spendMana(20);
      when(avatarRepository.findById(new Id<>(AVATAR_ID))).thenReturn(Optional.of(avatar));

      service.restoreMana(new Id<>(AVATAR_ID), 10);

      assertThat(avatar.getMana().amount().value()).isEqualTo(40);
      verify(avatarRepository).save(avatar);
    }
  }

  // ─── Progression ─────────────────────────────────────────────────────────────

  @Nested
  @DisplayName("grantExperience")
  class GrantExperience {

    @Test
    @DisplayName("does NOT fire LevelUpped event when threshold is not reached")
    void noLevelUp() throws AvatarNotFoundException {
      Avatar avatar = stubAvatar();
      when(avatarRepository.findById(new Id<>(AVATAR_ID))).thenReturn(Optional.of(avatar));

      service.grantExperience(new Id<>(AVATAR_ID), 50);

      verify(avatarObserver, never()).notifyAvatarEvent(any(LevelUpped.class));
      verify(avatarRepository).save(avatar);
    }

    @Test
    @DisplayName("fires LevelUpped event when avatar crosses the threshold")
    void levelUp() throws AvatarNotFoundException {
      Avatar avatar = stubAvatar();
      when(avatarRepository.findById(new Id<>(AVATAR_ID))).thenReturn(Optional.of(avatar));
      service.grantExperience(new Id<>(AVATAR_ID), 100);
      ArgumentCaptor<AvatarEvent> captor = ArgumentCaptor.forClass(AvatarEvent.class);
      verify(avatarObserver).notifyAvatarEvent(captor.capture());
      assertThat(captor.getValue()).isInstanceOf(LevelUpped.class);
      assertThat(((LevelUpped) captor.getValue()).newLevel().levelNumber()).isEqualTo(2);
    }

    @Test
    @DisplayName("does NOT fire NewSpellLearned when the new level has no associated spell")
    void noSpellOnLevelWithoutSpell() throws AvatarNotFoundException {
      Avatar avatar = stubAvatar();
      when(avatarRepository.findById(new Id<>(AVATAR_ID))).thenReturn(Optional.of(avatar));
      service.grantExperience(new Id<>(AVATAR_ID), 100);
      verify(avatarObserver, never()).notifyAvatarEvent(any(NewSpellLearned.class));
    }

    @Test
    @DisplayName("fires NewSpellLearned and teaches spell when avatar reaches a spell-tier level")
    void spellLearnedOnSpellTierLevelUp() throws AvatarNotFoundException {
      Avatar avatar = stubAvatarAtLevel4();
      when(avatarRepository.findById(new Id<>(AVATAR_ID))).thenReturn(Optional.of(avatar));

      service.grantExperience(new Id<>(AVATAR_ID), 100);
      assertThat(avatar.getSpells()).contains(Spell.FIREBALL);

      // Two events must have been published: LevelUpped + NewSpellLearned
      ArgumentCaptor<AvatarEvent> captor = ArgumentCaptor.forClass(AvatarEvent.class);
      verify(avatarObserver, times(2)).notifyAvatarEvent(captor.capture());
      List<AvatarEvent> events = captor.getAllValues();
      assertThat(events).anySatisfy(e -> assertThat(e).isInstanceOf(LevelUpped.class));
      assertThat(events)
          .anySatisfy(
              e -> {
                assertThat(e).isInstanceOf(NewSpellLearned.class);
                assertThat(((NewSpellLearned) e).spell()).isEqualTo(Spell.FIREBALL);
              });
    }

    @Test
    @DisplayName("persists avatar twice when a spell is learned: once after XP, once after spell")
    void avatarSavedTwiceWhenSpellLearned() throws AvatarNotFoundException {
      Avatar avatar = stubAvatarAtLevel4();
      when(avatarRepository.findById(new Id<>(AVATAR_ID))).thenReturn(Optional.of(avatar));
      service.grantExperience(new Id<>(AVATAR_ID), 100);
      verify(avatarRepository, times(2)).save(avatar);
    }
  }

  @Nested
  @DisplayName("increaseStrength")
  class IncreaseStrength {

    @Test
    @DisplayName("fires SkillPointAssigned for strength and saves")
    void firesEvent() throws AvatarNotFoundException {
      Avatar avatar = stubAvatar();
      when(avatarRepository.findById(new Id<>(AVATAR_ID))).thenReturn(Optional.of(avatar));

      service.increaseStrength(new Id<>(AVATAR_ID));

      ArgumentCaptor<AvatarEvent> captor = ArgumentCaptor.forClass(AvatarEvent.class);
      verify(avatarObserver).notifyAvatarEvent(captor.capture());
      assertThat(captor.getValue()).isInstanceOf(SkillPointAssigned.class);
      verify(avatarRepository).save(avatar);
    }
  }

  @Nested
  @DisplayName("increaseDefense")
  class IncreaseDefense {

    @Test
    @DisplayName("fires SkillPointAssigned for defense and saves")
    void firesEvent() throws AvatarNotFoundException {
      Avatar avatar = stubAvatar();
      when(avatarRepository.findById(new Id<>(AVATAR_ID))).thenReturn(Optional.of(avatar));

      service.increaseDefense(new Id<>(AVATAR_ID));

      ArgumentCaptor<AvatarEvent> captor = ArgumentCaptor.forClass(AvatarEvent.class);
      verify(avatarObserver).notifyAvatarEvent(captor.capture());
      assertThat(captor.getValue()).isInstanceOf(SkillPointAssigned.class);
      verify(avatarRepository).save(avatar);
    }
  }

  @Nested
  @DisplayName("increaseIntelligence")
  class IncreaseIntelligence {

    @Test
    @DisplayName("fires SkillPointAssigned for intelligence and saves")
    void firesEvent() throws AvatarNotFoundException {
      Avatar avatar = stubAvatar();
      when(avatarRepository.findById(new Id<>(AVATAR_ID))).thenReturn(Optional.of(avatar));

      service.increaseIntelligence(new Id<>(AVATAR_ID));

      ArgumentCaptor<AvatarEvent> captor = ArgumentCaptor.forClass(AvatarEvent.class);
      verify(avatarObserver).notifyAvatarEvent(captor.capture());
      assertThat(captor.getValue()).isInstanceOf(SkillPointAssigned.class);
      verify(avatarRepository).save(avatar);
    }
  }

  // ─── Inventory ───────────────────────────────────────────────────────────────

  @Nested
  @DisplayName("inventory operations")
  class InventoryOperations {

    private final Item sword = new Weapon("Sword", "A blade", 15);

    @Test
    @DisplayName("addToInventory adds item and saves")
    void addToInventory() throws AvatarNotFoundException {
      Avatar avatar = stubAvatar();
      when(avatarRepository.findById(new Id<>(AVATAR_ID))).thenReturn(Optional.of(avatar));

      service.addToInventory(new Id<>(AVATAR_ID), sword);

      assertThat(avatar.getInventory().getItems()).contains(sword);
      verify(avatarRepository).save(avatar);
    }

    @Test
    @DisplayName("removeItem removes item and saves")
    void removeItem() throws AvatarNotFoundException {
      Avatar avatar = stubAvatar();
      avatar.addItemToInventory(sword);
      when(avatarRepository.findById(new Id<>(AVATAR_ID))).thenReturn(Optional.of(avatar));

      service.removeItem(new Id<>(AVATAR_ID), sword);

      assertThat(avatar.getInventory().getItems()).doesNotContain(sword);
      verify(avatarRepository).save(avatar);
    }

    @Test
    @DisplayName("equipItem moves item to equipped slots and saves")
    void equipItem() throws AvatarNotFoundException {
      Avatar avatar = stubAvatar();
      avatar.addItemToInventory(sword);
      when(avatarRepository.findById(new Id<>(AVATAR_ID))).thenReturn(Optional.of(avatar));

      service.equipItem(new Id<>(AVATAR_ID), sword);

      assertThat(avatar.getEquippedItems().getItems()).contains(sword);
      assertThat(avatar.getInventory().getItems()).doesNotContain(sword);
      verify(avatarRepository).save(avatar);
    }

    @Test
    @DisplayName("unequipItem moves item back to inventory and saves")
    void unequipItem() throws AvatarNotFoundException {
      Avatar avatar = stubAvatar();
      avatar.addItemToInventory(sword);
      avatar.equipItem(sword);
      when(avatarRepository.findById(new Id<>(AVATAR_ID))).thenReturn(Optional.of(avatar));

      service.unequipItem(new Id<>(AVATAR_ID), sword);

      assertThat(avatar.getInventory().getItems()).contains(sword);
      assertThat(avatar.getEquippedItems().getItems()).doesNotContain(sword);
      verify(avatarRepository).save(avatar);
    }
  }

  // ─── Query delegates ─────────────────────────────────────────────────────────

  @Nested
  @DisplayName("query delegates")
  class QueryDelegates {

    @Test
    @DisplayName("getName delegates to avatar.getName()")
    void getName() throws AvatarNotFoundException {
      Avatar avatar = stubAvatar();
      when(avatarRepository.findById(new Id<>(AVATAR_ID))).thenReturn(Optional.of(avatar));
      assertThat(service.getName(new Id<>(AVATAR_ID))).isEqualTo(AVATAR_NAME);
    }

    @Test
    @DisplayName("getMoney delegates to avatar.getMoney()")
    void getMoney() throws AvatarNotFoundException {
      Avatar avatar = stubAvatar();
      when(avatarRepository.findById(new Id<>(AVATAR_ID))).thenReturn(Optional.of(avatar));
      assertThat(service.getMoney(new Id<>(AVATAR_ID)).amount()).isZero();
    }

    @Test
    @DisplayName("getLevel delegates to avatar.getLevel()")
    void getLevel() throws AvatarNotFoundException {
      Avatar avatar = stubAvatar();
      when(avatarRepository.findById(new Id<>(AVATAR_ID))).thenReturn(Optional.of(avatar));
      assertThat(service.getLevel(new Id<>(AVATAR_ID)).levelNumber()).isEqualTo(1);
    }

    @Test
    @DisplayName("getHealth delegates to avatar.getHealth()")
    void getHealth() throws AvatarNotFoundException {
      Avatar avatar = stubAvatar();
      when(avatarRepository.findById(new Id<>(AVATAR_ID))).thenReturn(Optional.of(avatar));
      assertThat(service.getHealth(new Id<>(AVATAR_ID)).current().value()).isEqualTo(100);
    }

    @Test
    @DisplayName("getMana delegates to avatar.getMana()")
    void getMana() throws AvatarNotFoundException {
      Avatar avatar = stubAvatar();
      when(avatarRepository.findById(new Id<>(AVATAR_ID))).thenReturn(Optional.of(avatar));
      assertThat(service.getMana(new Id<>(AVATAR_ID)).amount().value()).isEqualTo(50);
    }

    @Test
    @DisplayName("getExperience returns the current XP from the level")
    void getExperience() throws AvatarNotFoundException {
      Avatar avatar = stubAvatar();
      avatar.gainExperience(30);
      when(avatarRepository.findById(new Id<>(AVATAR_ID))).thenReturn(Optional.of(avatar));
      assertThat(service.getExperience(new Id<>(AVATAR_ID)).amount()).isEqualTo(30);
    }

    @Test
    @DisplayName("getAvatarStats delegates to avatar.getAvatarStats()")
    void getAvatarStats() throws AvatarNotFoundException {
      Avatar avatar = stubAvatar();
      when(avatarRepository.findById(new Id<>(AVATAR_ID))).thenReturn(Optional.of(avatar));
      assertThat(service.getAvatarStats(new Id<>(AVATAR_ID))).isSameAs(avatar.getAvatarStats());
    }
  }

  @Test
  @DisplayName("searchAvatars delegates to repository with the given criteria")
  void searchAvatars() {
    Avatar avatar = stubAvatar();
    AvatarSearchRequest criteria = new AvatarSearchRequest("Hero", 1, 10);
    when(avatarRepository.search(criteria)).thenReturn(List.of(avatar));

    assertThat(service.searchAvatars(criteria)).containsExactly(avatar);
    verify(avatarRepository).search(criteria);
  }
}
