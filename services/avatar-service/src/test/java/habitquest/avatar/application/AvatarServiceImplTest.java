package habitquest.avatar.application;

import static habitquest.avatar.AvatarFixtures.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import common.ddd.Id;
import habitquest.avatar.domain.avatar.*;
import habitquest.avatar.domain.events.*;
import habitquest.avatar.domain.factory.AvatarFactory;
import habitquest.avatar.domain.spells.Spell;
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
  private static final String BAD_ID = "bad";

  @Mock private AvatarFactory avatarFactory;
  @Mock private AvatarRepository avatarRepository;
  @Mock private AvatarObserver avatarObserver;
  @Mock private MarketplaceClientPort marketplacePort;

  private AvatarServiceImpl service;

  @BeforeEach
  void setUp() {
    service =
        new AvatarServiceImpl(avatarFactory, avatarRepository, avatarObserver, marketplacePort);
  }

  // ─── createAvatar ────────────────────────────────────────────────────────────

  @Nested
  @DisplayName("createAvatar")
  class CreateAvatar {

    @Test
    @DisplayName("delegates to factory, saves, and returns the new id")
    void createsAndReturnsId() {
      Avatar avatar = mutableAvatar();
      when(avatarFactory.create(AVATAR_ID, AVATAR_NAME)).thenReturn(avatar);
      when(avatarRepository.save(avatar)).thenReturn(avatar);

      Id<Avatar> id = service.createAvatar(AVATAR_ID, AVATAR_NAME);

      assertThat(id.value()).isEqualTo(AVATAR_1);
      verify(avatarRepository).save(avatar);
    }
  }

  // ─── getAvatarById ───────────────────────────────────────────────────────────

  @Nested
  @DisplayName("getAvatarById")
  class GetAvatarById {

    @Test
    @DisplayName("returns the avatar when found")
    void found() throws AvatarNotFoundException {
      Avatar avatar = mutableAvatar();
      when(avatarRepository.findById(AVATAR_ID)).thenReturn(Optional.of(avatar));

      assertThat(service.getAvatarById(AVATAR_ID)).isSameAs(avatar);
    }

    @Test
    @DisplayName("throws AvatarNotFoundException when id is unknown")
    void notFound() {
      when(avatarRepository.findById(UNKNOWN_ID)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> service.getAvatarById(UNKNOWN_ID))
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
      when(avatarRepository.findById(AVATAR_ID)).thenReturn(Optional.of(mutableAvatar()));

      service.deleteAvatar(AVATAR_ID);

      verify(avatarRepository).deleteById(AVATAR_ID);
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
      Avatar avatar = mutableAvatar();
      when(avatarRepository.findById(AVATAR_ID)).thenReturn(Optional.of(avatar));

      service.updateName(AVATAR_ID, "Mage");

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
      Avatar avatar = mutableAvatar();
      when(avatarRepository.findById(AVATAR_ID)).thenReturn(Optional.of(avatar));

      service.earnMoney(AVATAR_ID, 50);

      assertThat(avatar.getMoney().amount()).isEqualTo(50);
      verify(avatarRepository).save(avatar);
    }

    @Test
    @DisplayName("spendMoney decreases money and saves")
    void spendMoney() throws AvatarNotFoundException {
      Avatar avatar = mutableAvatar();
      avatar.earnMoney(100);
      when(avatarRepository.findById(AVATAR_ID)).thenReturn(Optional.of(avatar));

      service.spendMoney(AVATAR_ID, 40);

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
      Avatar avatar = mutableAvatar();
      when(avatarRepository.findById(AVATAR_ID)).thenReturn(Optional.of(avatar));

      service.applyDamage(AVATAR_ID, 10);

      assertThat(avatar.getHealth().current().value()).isEqualTo(90);
      verify(avatarObserver, never()).notifyAvatarEvent(any(Dead.class));
      verify(avatarRepository).save(avatar);
    }

    @Test
    @DisplayName("fires Dead event when avatar dies from damage")
    void damageLethal() throws AvatarNotFoundException {
      Avatar avatar = mutableAvatar();
      avatar.earnMoney(200);
      when(avatarRepository.findById(AVATAR_ID)).thenReturn(Optional.of(avatar));

      service.applyDamage(AVATAR_ID, 9999);

      ArgumentCaptor<AvatarEvent> captor = ArgumentCaptor.forClass(AvatarEvent.class);
      verify(avatarObserver).notifyAvatarEvent(captor.capture());
      assertThat(captor.getValue()).isInstanceOf(Dead.class);
      // .value() restituisce la stringa: confrontiamo con AVATAR_1, non AVATAR_ID (che è Id<>)
      assertThat(((Dead) captor.getValue()).avatarId().value()).isEqualTo(AVATAR_1);
    }
  }

  // ─── healAvatar ──────────────────────────────────────────────────────────────

  @Nested
  @DisplayName("healAvatar")
  class HealAvatar {

    @Test
    @DisplayName("increases health and saves")
    void heals() throws AvatarNotFoundException {
      Avatar avatar = mutableAvatar();
      avatar.takeDamage(40);
      when(avatarRepository.findById(AVATAR_ID)).thenReturn(Optional.of(avatar));

      service.healAvatar(AVATAR_ID, 20);

      assertThat(avatar.getHealth().current().value()).isEqualTo(80);
      verify(avatarRepository).save(avatar);
    }
  }

  // ─── Mana ────────────────────────────────────────────────────────────────────

  @Nested
  @DisplayName("mana operations")
  class ManaOperations {

    @Test
    @DisplayName("spendMana reduces mana and saves")
    void spendMana() throws AvatarNotFoundException {
      Avatar avatar = mutableAvatar();
      when(avatarRepository.findById(AVATAR_ID)).thenReturn(Optional.of(avatar));

      service.spendMana(AVATAR_ID, 10);

      assertThat(avatar.getMana().amount().value()).isEqualTo(40);
      verify(avatarRepository).save(avatar);
    }

    @Test
    @DisplayName("restoreMana increases mana and saves")
    void restoreMana() throws AvatarNotFoundException {
      Avatar avatar = mutableAvatar();
      avatar.spendMana(20);
      when(avatarRepository.findById(AVATAR_ID)).thenReturn(Optional.of(avatar));

      service.restoreMana(AVATAR_ID, 10);

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
      Avatar avatar = mutableAvatar();
      when(avatarRepository.findById(AVATAR_ID)).thenReturn(Optional.of(avatar));

      service.grantExperience(AVATAR_ID, 50);

      verify(avatarObserver, never()).notifyAvatarEvent(any(LevelUpped.class));
      verify(avatarRepository).save(avatar);
    }

    @Test
    @DisplayName("fires LevelUpped event when avatar crosses the threshold")
    void levelUp() throws AvatarNotFoundException {
      Avatar avatar = mutableAvatar();
      when(avatarRepository.findById(AVATAR_ID)).thenReturn(Optional.of(avatar));

      // DEFAULT_XP_TO_NEXT (100) è esattamente la soglia per passare al livello 2
      service.grantExperience(AVATAR_ID, DEFAULT_XP_TO_NEXT);

      ArgumentCaptor<AvatarEvent> captor = ArgumentCaptor.forClass(AvatarEvent.class);
      verify(avatarObserver).notifyAvatarEvent(captor.capture());
      assertThat(captor.getValue()).isInstanceOf(LevelUpped.class);
      assertThat(((LevelUpped) captor.getValue()).newLevel().levelNumber()).isEqualTo(2);
    }

    @Test
    @DisplayName("does NOT fire NewSpellLearned when the new level has no associated spell")
    void noSpellOnLevelWithoutSpell() throws AvatarNotFoundException {
      Avatar avatar = mutableAvatar();
      when(avatarRepository.findById(AVATAR_ID)).thenReturn(Optional.of(avatar));

      service.grantExperience(AVATAR_ID, DEFAULT_XP_TO_NEXT);

      verify(avatarObserver, never()).notifyAvatarEvent(any(NewSpellLearned.class));
    }

    @Test
    @DisplayName("fires NewSpellLearned and teaches spell when avatar reaches a spell-tier level")
    void spellLearnedOnSpellTierLevelUp() throws AvatarNotFoundException {
      Avatar avatar = avatarAtLevel4();
      when(avatarRepository.findById(AVATAR_ID)).thenReturn(Optional.of(avatar));

      service.grantExperience(AVATAR_ID, DEFAULT_XP_TO_NEXT);
      assertThat(avatar.getSpells()).contains(Spell.FIREBALL);

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
      Avatar avatar = avatarAtLevel4();
      when(avatarRepository.findById(AVATAR_ID)).thenReturn(Optional.of(avatar));

      service.grantExperience(AVATAR_ID, DEFAULT_XP_TO_NEXT);

      verify(avatarRepository, times(2)).save(avatar);
    }
  }

  // ─── Stats ───────────────────────────────────────────────────────────────────

  @Nested
  @DisplayName("increaseStrength")
  class IncreaseStrength {

    @Test
    @DisplayName("fires SkillPointAssigned for strength and saves")
    void firesEvent() throws AvatarNotFoundException {
      Avatar avatar = mutableAvatar();
      when(avatarRepository.findById(AVATAR_ID)).thenReturn(Optional.of(avatar));

      service.increaseStrength(AVATAR_ID);

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
      Avatar avatar = mutableAvatar();
      when(avatarRepository.findById(AVATAR_ID)).thenReturn(Optional.of(avatar));

      service.increaseDefense(AVATAR_ID);

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
      Avatar avatar = mutableAvatar();
      when(avatarRepository.findById(AVATAR_ID)).thenReturn(Optional.of(avatar));

      service.increaseIntelligence(AVATAR_ID);

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

    // SWORD e SHIELD vengono da AvatarFixtures.* — non serve ridichiarare item locali

    @Test
    @DisplayName("addToInventory adds item and saves")
    void addToInventory() throws AvatarNotFoundException {
      Avatar avatar = mutableAvatar();
      when(avatarRepository.findById(AVATAR_ID)).thenReturn(Optional.of(avatar));

      service.addToInventory(AVATAR_ID, SWORD);

      assertThat(avatar.getInventory().getItems()).contains(SWORD);
      verify(avatarRepository).save(avatar);
    }

    @Test
    @DisplayName("removeItem removes item and saves")
    void removeItem() throws AvatarNotFoundException {
      Avatar avatar = mutableAvatar();
      avatar.addItemToInventory(SWORD);
      when(avatarRepository.findById(AVATAR_ID)).thenReturn(Optional.of(avatar));

      service.removeItem(AVATAR_ID, SWORD);

      assertThat(avatar.getInventory().getItems()).doesNotContain(SWORD);
      verify(avatarRepository).save(avatar);
    }

    @Test
    @DisplayName("equipItem moves item to equipped slots and saves")
    void equipItem() throws AvatarNotFoundException {
      Avatar avatar = mutableAvatar();
      avatar.addItemToInventory(SWORD);
      when(avatarRepository.findById(AVATAR_ID)).thenReturn(Optional.of(avatar));

      service.equipItem(AVATAR_ID, SWORD);

      assertThat(avatar.getEquippedItems().getItems()).contains(SWORD);
      assertThat(avatar.getInventory().getItems()).doesNotContain(SWORD);
      verify(avatarRepository).save(avatar);
    }

    @Test
    @DisplayName("unequipItem moves item back to inventory and saves")
    void unequipItem() throws AvatarNotFoundException {
      Avatar avatar = mutableAvatar();
      avatar.addItemToInventory(SWORD);
      avatar.equipItem(SWORD);
      when(avatarRepository.findById(AVATAR_ID)).thenReturn(Optional.of(avatar));

      service.unequipItem(AVATAR_ID, SWORD);

      assertThat(avatar.getInventory().getItems()).contains(SWORD);
      assertThat(avatar.getEquippedItems().getItems()).doesNotContain(SWORD);
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
      Avatar avatar = mutableAvatar();
      when(avatarRepository.findById(AVATAR_ID)).thenReturn(Optional.of(avatar));

      assertThat(service.getName(AVATAR_ID)).isEqualTo(AVATAR_NAME);
    }

    @Test
    @DisplayName("getMoney delegates to avatar.getMoney()")
    void getMoney() throws AvatarNotFoundException {
      Avatar avatar = mutableAvatar();
      when(avatarRepository.findById(AVATAR_ID)).thenReturn(Optional.of(avatar));

      assertThat(service.getMoney(AVATAR_ID).amount()).isZero();
    }

    @Test
    @DisplayName("getLevel delegates to avatar.getLevel()")
    void getLevel() throws AvatarNotFoundException {
      Avatar avatar = mutableAvatar();
      when(avatarRepository.findById(AVATAR_ID)).thenReturn(Optional.of(avatar));

      assertThat(service.getLevel(AVATAR_ID).levelNumber()).isEqualTo(1);
    }

    @Test
    @DisplayName("getHealth delegates to avatar.getHealth()")
    void getHealth() throws AvatarNotFoundException {
      Avatar avatar = mutableAvatar();
      when(avatarRepository.findById(AVATAR_ID)).thenReturn(Optional.of(avatar));

      assertThat(service.getHealth(AVATAR_ID).current().value()).isEqualTo(DEFAULT_HEALTH);
    }

    @Test
    @DisplayName("getMana delegates to avatar.getMana()")
    void getMana() throws AvatarNotFoundException {
      Avatar avatar = mutableAvatar();
      when(avatarRepository.findById(AVATAR_ID)).thenReturn(Optional.of(avatar));

      assertThat(service.getMana(AVATAR_ID).amount().value()).isEqualTo(DEFAULT_MANA);
    }

    @Test
    @DisplayName("getExperience returns the current XP from the level")
    void getExperience() throws AvatarNotFoundException {
      Avatar avatar = mutableAvatar();
      avatar.gainExperience(30);
      when(avatarRepository.findById(AVATAR_ID)).thenReturn(Optional.of(avatar));

      assertThat(service.getExperience(AVATAR_ID).amount()).isEqualTo(30);
    }

    @Test
    @DisplayName("getAvatarStats delegates to avatar.getAvatarStats()")
    void getAvatarStats() throws AvatarNotFoundException {
      Avatar avatar = mutableAvatar();
      when(avatarRepository.findById(AVATAR_ID)).thenReturn(Optional.of(avatar));

      assertThat(service.getAvatarStats(AVATAR_ID)).isSameAs(avatar.getAvatarStats());
    }
  }

  // ─── Search ──────────────────────────────────────────────────────────────────

  @Test
  @DisplayName("searchAvatars delegates to repository with the given criteria")
  void searchAvatars() {
    Avatar avatar = mutableAvatar();
    AvatarSearchRequest criteria = new AvatarSearchRequest(AVATAR_NAME, 1, 10);
    when(avatarRepository.search(criteria)).thenReturn(List.of(avatar));

    assertThat(service.searchAvatars(criteria)).containsExactly(avatar);
    verify(avatarRepository).search(criteria);
  }
}
