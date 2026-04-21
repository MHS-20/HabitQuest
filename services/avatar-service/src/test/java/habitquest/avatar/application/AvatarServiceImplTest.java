package habitquest.avatar.application;

import static habitquest.avatar.AvatarFixtures.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import common.ddd.Id;
import habitquest.avatar.application.exceptions.AvatarNotFoundException;
import habitquest.avatar.application.port.out.AvatarRepository;
import habitquest.avatar.application.port.out.MarketplaceClientPort;
import habitquest.avatar.application.service.AvatarCommandServiceImpl;
import habitquest.avatar.application.service.AvatarQueryServiceImpl;
import habitquest.avatar.application.service.AvatarSearchQuery;
import habitquest.avatar.domain.avatar.*;
import habitquest.avatar.domain.events.*;
import habitquest.avatar.domain.factory.AvatarFactory;
import habitquest.avatar.domain.spells.Spell;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
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

  @InjectMocks private AvatarCommandServiceImpl commandService;
  @InjectMocks private AvatarQueryServiceImpl queryService;

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

      Id<Avatar> id = commandService.createAvatar(AVATAR_ID, AVATAR_NAME);

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

      assertThat(queryService.getAvatarById(AVATAR_ID)).isSameAs(avatar);
    }

    @Test
    @DisplayName("throws AvatarNotFoundException when id is unknown")
    void notFound() {
      when(avatarRepository.findById(UNKNOWN_ID)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> queryService.getAvatarById(UNKNOWN_ID))
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

      commandService.deleteAvatar(AVATAR_ID);

      verify(avatarRepository).deleteById(AVATAR_ID);
    }

    @Test
    @DisplayName("throws when id not found")
    void throwsWhenNotFound() {
      when(avatarRepository.findById(new Id<>(BAD_ID))).thenReturn(Optional.empty());

      assertThatThrownBy(() -> commandService.deleteAvatar(new Id<>(BAD_ID)))
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

      commandService.updateName(AVATAR_ID, "Mage");

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
      commandService.earnMoney(AVATAR_ID, new Money(50));
      assertThat(avatar.getMoney().amount()).isEqualTo(50);
      verify(avatarRepository).save(avatar);
    }

    @Test
    @DisplayName("spendMoney decreases money and saves")
    void spendMoney() throws AvatarNotFoundException {
      Avatar avatar = mutableAvatar();
      avatar.earnMoney(new Money(100));
      when(avatarRepository.findById(AVATAR_ID)).thenReturn(Optional.of(avatar));
      commandService.spendMoney(AVATAR_ID, new Money(40));
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

      commandService.applyDamage(AVATAR_ID, new Damage(10));

      assertThat(avatar.getHealth().current().value()).isEqualTo(90);
      verify(avatarObserver, never()).notifyAvatarEvent(any(Dead.class));
      verify(avatarRepository).save(avatar);
    }

    @Test
    @DisplayName("fires Dead event when avatar dies from damage")
    void damageLethal() throws AvatarNotFoundException {
      Avatar avatar = mutableAvatar();
      avatar.earnMoney(new Money(200));
      when(avatarRepository.findById(AVATAR_ID)).thenReturn(Optional.of(avatar));

      commandService.applyDamage(AVATAR_ID, new Damage(9999));

      ArgumentCaptor<AvatarEvent> captor = ArgumentCaptor.forClass(AvatarEvent.class);
      verify(avatarObserver).notifyAvatarEvent(captor.capture());
      assertThat(captor.getValue()).isInstanceOf(Dead.class);
      assertThat(((Dead) captor.getValue()).avatarId().value()).isEqualTo(AVATAR_1);
    }
  }

  // ─── use Potion ──────────────────────────────────────────────────────────────
  @Nested
  @DisplayName("useHealthPotion")
  class UseHealthPotion {

    @Test
    @DisplayName("heals avatar with potion power and saves")
    void usesHealthPotion() throws AvatarNotFoundException {
      Avatar avatar = mutableAvatar();
      avatar.takeDamage(new Damage(40));
      when(avatarRepository.findById(AVATAR_ID)).thenReturn(Optional.of(avatar));
      commandService.useHealthPotion(AVATAR_ID, HEALTH_POTION);
      assertThat(avatar.getHealth().current().value()).isEqualTo(80);
      assertThat(avatar.getInventory()).doesNotContain(HEALTH_POTION);
      verify(avatarRepository).save(avatar);
    }
  }

  @Nested
  @DisplayName("useManaPotion")
  class UseManaPotion {

    @Test
    @DisplayName("restores mana with potion power and saves")
    void usesManaPotion() throws AvatarNotFoundException {
      Avatar avatar = mutableAvatar();
      avatar.spendMana(new Mana(20));
      when(avatarRepository.findById(AVATAR_ID)).thenReturn(Optional.of(avatar));
      commandService.useManaPotion(AVATAR_ID, MANA_POTION);
      assertThat(avatar.getMana().amount().value()).isEqualTo(40);
      assertThat(avatar.getInventory()).doesNotContain(MANA_POTION);
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

      commandService.grantExperience(AVATAR_ID, new Experience(50));

      verify(avatarObserver, never()).notifyAvatarEvent(any(LevelUpped.class));
      verify(avatarRepository).save(avatar);
    }

    @Test
    @DisplayName("fires LevelUpped event when avatar crosses the threshold")
    void levelUp() throws AvatarNotFoundException {
      Avatar avatar = mutableAvatar();
      when(avatarRepository.findById(AVATAR_ID)).thenReturn(Optional.of(avatar));
      commandService.grantExperience(AVATAR_ID, XP_TO_NEXT);

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
      commandService.grantExperience(AVATAR_ID, XP_TO_NEXT);
      verify(avatarObserver, never()).notifyAvatarEvent(any(NewSpellLearned.class));
    }

    @Test
    @DisplayName("fires NewSpellLearned and teaches spell when avatar reaches a spell-tier level")
    void spellLearnedOnSpellTierLevelUp() throws AvatarNotFoundException {
      Avatar avatar = avatarAtLevel4();
      when(avatarRepository.findById(AVATAR_ID)).thenReturn(Optional.of(avatar));

      commandService.grantExperience(AVATAR_ID, XP_TO_NEXT);
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
      commandService.grantExperience(AVATAR_ID, XP_TO_NEXT);
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
      commandService.increaseStrength(AVATAR_ID);
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
      commandService.increaseDefense(AVATAR_ID);
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
      commandService.increaseIntelligence(AVATAR_ID);
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

    @Test
    @DisplayName("addToInventory adds item and saves")
    void addToInventory() throws AvatarNotFoundException {
      Avatar avatar = mutableAvatar();
      when(avatarRepository.findById(AVATAR_ID)).thenReturn(Optional.of(avatar));
      commandService.addToInventory(AVATAR_ID, SWORD);
      assertThat(avatar.getInventory()).contains(SWORD);
      verify(avatarRepository).save(avatar);
    }

    @Test
    @DisplayName("removeItem removes item and saves")
    void removeItem() throws AvatarNotFoundException {
      Avatar avatar = mutableAvatar();
      avatar.addItemToInventory(SWORD);
      when(avatarRepository.findById(AVATAR_ID)).thenReturn(Optional.of(avatar));
      commandService.removeItem(AVATAR_ID, SWORD);
      assertThat(avatar.getInventory()).doesNotContain(SWORD);
      verify(avatarRepository).save(avatar);
    }

    @Test
    @DisplayName("equipItem moves item to equipped slots and saves")
    void equipItem() throws AvatarNotFoundException {
      Avatar avatar = mutableAvatar();
      avatar.addItemToInventory(SWORD);
      when(avatarRepository.findById(AVATAR_ID)).thenReturn(Optional.of(avatar));
      commandService.equipItem(AVATAR_ID, SWORD);
      assertThat(avatar.getEquippedItems()).contains(SWORD);
      assertThat(avatar.getInventory()).doesNotContain(SWORD);
      verify(avatarRepository).save(avatar);
    }

    @Test
    @DisplayName("unequipItem moves item back to inventory and saves")
    void unequipItem() throws AvatarNotFoundException {
      Avatar avatar = mutableAvatar();
      avatar.addItemToInventory(SWORD);
      avatar.equipItem(SWORD);
      when(avatarRepository.findById(AVATAR_ID)).thenReturn(Optional.of(avatar));
      commandService.unequipItem(AVATAR_ID, SWORD);
      assertThat(avatar.getInventory()).contains(SWORD);
      assertThat(avatar.getEquippedItems()).doesNotContain(SWORD);
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

      assertThat(queryService.getName(AVATAR_ID)).isEqualTo(AVATAR_NAME);
    }

    @Test
    @DisplayName("getMoney delegates to avatar.getMoney()")
    void getMoney() throws AvatarNotFoundException {
      Avatar avatar = mutableAvatar();
      when(avatarRepository.findById(AVATAR_ID)).thenReturn(Optional.of(avatar));

      assertThat(queryService.getMoney(AVATAR_ID).amount()).isZero();
    }

    @Test
    @DisplayName("getLevel delegates to avatar.getLevel()")
    void getLevel() throws AvatarNotFoundException {
      Avatar avatar = mutableAvatar();
      when(avatarRepository.findById(AVATAR_ID)).thenReturn(Optional.of(avatar));

      assertThat(queryService.getLevel(AVATAR_ID).levelNumber()).isEqualTo(1);
    }

    @Test
    @DisplayName("getHealth delegates to avatar.getHealth()")
    void getHealth() throws AvatarNotFoundException {
      Avatar avatar = mutableAvatar();
      when(avatarRepository.findById(AVATAR_ID)).thenReturn(Optional.of(avatar));

      assertThat(queryService.getHealth(AVATAR_ID).current().value()).isEqualTo(DEFAULT_HEALTH);
    }

    @Test
    @DisplayName("getMana delegates to avatar.getMana()")
    void getMana() throws AvatarNotFoundException {
      Avatar avatar = mutableAvatar();
      when(avatarRepository.findById(AVATAR_ID)).thenReturn(Optional.of(avatar));

      assertThat(queryService.getMana(AVATAR_ID).amount().value()).isEqualTo(DEFAULT_MANA);
    }

    @Test
    @DisplayName("getExperience returns the current XP from the level")
    void getExperience() throws AvatarNotFoundException {
      Avatar avatar = mutableAvatar();
      avatar.gainExperience(30);
      when(avatarRepository.findById(AVATAR_ID)).thenReturn(Optional.of(avatar));

      assertThat(queryService.getExperience(AVATAR_ID).amount()).isEqualTo(30);
    }

    @Test
    @DisplayName("getAvatarStats delegates to avatar.getAvatarStats()")
    void getAvatarStats() throws AvatarNotFoundException {
      Avatar avatar = mutableAvatar();
      when(avatarRepository.findById(AVATAR_ID)).thenReturn(Optional.of(avatar));

      assertThat(queryService.getAvatarStats(AVATAR_ID)).isSameAs(avatar.getAvatarStats());
    }
  }

  // ─── Search ──────────────────────────────────────────────────────────────────

  @Test
  @DisplayName("searchAvatars delegates to repository with the given criteria")
  void searchAvatars() {
    Avatar avatar = mutableAvatar();
    AvatarSearchQuery criteria = new AvatarSearchQuery(AVATAR_NAME, 1, 10);
    when(avatarRepository.search(criteria)).thenReturn(List.of(avatar));

    assertThat(queryService.searchAvatars(criteria)).containsExactly(avatar);
    verify(avatarRepository).search(criteria);
  }
}
