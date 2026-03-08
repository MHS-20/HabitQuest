package habitquest.avatar.application;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import habitquest.avatar.domain.avatar.*;
import habitquest.avatar.domain.events.*;
import habitquest.avatar.domain.factory.AvatarFactory;
import habitquest.avatar.domain.items.Item;
import habitquest.avatar.domain.items.Weapon;
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

  @Mock private AvatarFactory avatarFactory;
  @Mock private AvatarRepository avatarRepository;
  @Mock private AvatarObserver avatarObserver;

  private AvatarServiceImpl service;

  /** A fresh avatar wired to the same IDs every test uses. */
  private Avatar stubAvatar() {
    return new Avatar("Hero", "avatar-1", "inv-1", "equip-1", "stats-1");
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
      when(avatarFactory.create("Hero")).thenReturn(avatar);
      when(avatarRepository.save(avatar)).thenReturn(avatar);

      String id = service.createAvatar("Hero");

      assertThat(id).isEqualTo("avatar-1");
      verify(avatarRepository).save(avatar);
    }
  }

  // ─── getAvatarById ───────────────────────────────────────────────────────────

  @Nested
  @DisplayName("getAvatarById")
  class GetAvatarById {

    @Test
    @DisplayName("returns the avatar when found")
    void found() throws AvatarNotFoundExpection {
      Avatar avatar = stubAvatar();
      when(avatarRepository.findById("avatar-1")).thenReturn(Optional.of(avatar));

      assertThat(service.getAvatarById("avatar-1")).isSameAs(avatar);
    }

    @Test
    @DisplayName("throws AvatarNotFoundExpection when id is unknown")
    void notFound() {
      when(avatarRepository.findById("missing")).thenReturn(Optional.empty());

      assertThatThrownBy(() -> service.getAvatarById("missing"))
          .isInstanceOf(AvatarNotFoundExpection.class);
    }
  }

  // ─── updateAvatar ────────────────────────────────────────────────────────────

  @Nested
  @DisplayName("updateAvatar")
  class UpdateAvatar {

    @Test
    @DisplayName("saves the updated avatar when id exists")
    void updatesWhenFound() throws AvatarNotFoundExpection {
      Avatar existing = stubAvatar();
      Avatar updated = new Avatar("Updated", "avatar-1", "inv-1", "equip-1", "stats-1");
      when(avatarRepository.findById("avatar-1")).thenReturn(Optional.of(existing));

      service.updateAvatar("avatar-1", updated);

      verify(avatarRepository).save(updated);
    }

    @Test
    @DisplayName("throws when id not found")
    void throwsWhenNotFound() {
      when(avatarRepository.findById("bad")).thenReturn(Optional.empty());

      assertThatThrownBy(() -> service.updateAvatar("bad", stubAvatar()))
          .isInstanceOf(AvatarNotFoundExpection.class);
    }
  }

  // ─── deleteAvatar ────────────────────────────────────────────────────────────

  @Nested
  @DisplayName("deleteAvatar")
  class DeleteAvatar {

    @Test
    @DisplayName("calls deleteById after confirming existence")
    void deletes() throws AvatarNotFoundExpection {
      when(avatarRepository.findById("avatar-1")).thenReturn(Optional.of(stubAvatar()));

      service.deleteAvatar("avatar-1");

      verify(avatarRepository).deleteById("avatar-1");
    }

    @Test
    @DisplayName("throws when id not found")
    void throwsWhenNotFound() {
      when(avatarRepository.findById("bad")).thenReturn(Optional.empty());

      assertThatThrownBy(() -> service.deleteAvatar("bad"))
          .isInstanceOf(AvatarNotFoundExpection.class);
    }
  }

  // ─── updateName ──────────────────────────────────────────────────────────────

  @Nested
  @DisplayName("updateName")
  class UpdateName {

    @Test
    @DisplayName("renames the avatar and persists it")
    void renames() throws AvatarNotFoundExpection {
      Avatar avatar = stubAvatar();
      when(avatarRepository.findById("avatar-1")).thenReturn(Optional.of(avatar));

      service.updateName("avatar-1", "Mage");

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
    void earnMoney() throws AvatarNotFoundExpection {
      Avatar avatar = stubAvatar();
      when(avatarRepository.findById("avatar-1")).thenReturn(Optional.of(avatar));

      service.earnMoney("avatar-1", 50);

      assertThat(avatar.getMoney().amount()).isEqualTo(50);
      verify(avatarRepository).save(avatar);
    }

    @Test
    @DisplayName("spendMoney decreases money and saves")
    void spendMoney() throws AvatarNotFoundExpection {
      Avatar avatar = stubAvatar();
      // give the avatar some money first via the domain method
      avatar.earnMoney(100);
      when(avatarRepository.findById("avatar-1")).thenReturn(Optional.of(avatar));

      service.spendMoney("avatar-1", 40);

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
    void damageSurvived() throws AvatarNotFoundExpection {
      Avatar avatar = stubAvatar();
      when(avatarRepository.findById("avatar-1")).thenReturn(Optional.of(avatar));

      service.applyDamage("avatar-1", 10);

      assertThat(avatar.getHealth().current().value()).isEqualTo(90);
      verify(avatarObserver, never()).notifyAvaterEvent(any(Dead.class));
      verify(avatarRepository).save(avatar);
    }

    @Test
    @DisplayName("fires Dead event when avatar dies from damage")
    void damageLethal() throws AvatarNotFoundExpection {
      Avatar avatar = stubAvatar();
      avatar.earnMoney(200); // prevent money from going negative on death
      when(avatarRepository.findById("avatar-1")).thenReturn(Optional.of(avatar));

      service.applyDamage("avatar-1", 9999);

      ArgumentCaptor<AvatarEvent> captor = ArgumentCaptor.forClass(AvatarEvent.class);
      verify(avatarObserver).notifyAvaterEvent(captor.capture());
      assertThat(captor.getValue()).isInstanceOf(Dead.class);
      assertThat(((Dead) captor.getValue()).avatarId()).isEqualTo("avatar-1");
    }
  }

  @Nested
  @DisplayName("healAvatar")
  class HealAvatar {

    @Test
    @DisplayName("increases health and saves")
    void heals() throws AvatarNotFoundExpection {
      Avatar avatar = stubAvatar();
      avatar.takeDamage(40);
      when(avatarRepository.findById("avatar-1")).thenReturn(Optional.of(avatar));

      service.healAvatar("avatar-1", 20);

      assertThat(avatar.getHealth().current().value()).isEqualTo(80);
      verify(avatarRepository).save(avatar);
    }
  }

  @Nested
  @DisplayName("mana operations")
  class ManaOperations {

    @Test
    @DisplayName("spendMana reduces mana and saves")
    void spendMana() throws AvatarNotFoundExpection {
      Avatar avatar = stubAvatar();
      when(avatarRepository.findById("avatar-1")).thenReturn(Optional.of(avatar));

      service.spendMana("avatar-1", 10);

      assertThat(avatar.getMana().amount().value()).isEqualTo(40);
      verify(avatarRepository).save(avatar);
    }

    @Test
    @DisplayName("restoreMana increases mana and saves")
    void restoreMana() throws AvatarNotFoundExpection {
      Avatar avatar = stubAvatar();
      avatar.spendMana(20);
      when(avatarRepository.findById("avatar-1")).thenReturn(Optional.of(avatar));

      service.restoreMana("avatar-1", 10);

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
    void noLevelUp() throws AvatarNotFoundExpection {
      Avatar avatar = stubAvatar();
      when(avatarRepository.findById("avatar-1")).thenReturn(Optional.of(avatar));

      service.grantExperience("avatar-1", 50);

      verify(avatarObserver, never()).notifyAvaterEvent(any(LevelUpped.class));
      verify(avatarRepository).save(avatar);
    }

    @Test
    @DisplayName("fires LevelUpped event when avatar crosses the threshold")
    void levelUp() throws AvatarNotFoundExpection {
      Avatar avatar = stubAvatar();
      when(avatarRepository.findById("avatar-1")).thenReturn(Optional.of(avatar));

      service.grantExperience("avatar-1", 100);

      ArgumentCaptor<AvatarEvent> captor = ArgumentCaptor.forClass(AvatarEvent.class);
      verify(avatarObserver).notifyAvaterEvent(captor.capture());
      assertThat(captor.getValue()).isInstanceOf(LevelUpped.class);
      assertThat(((LevelUpped) captor.getValue()).newLevel().levelNumber()).isEqualTo(2);
    }
  }

  @Nested
  @DisplayName("increaseStrength")
  class IncreaseStrength {

    @Test
    @DisplayName("fires SkillPointAssigned for strength and saves")
    void firesEvent() throws AvatarNotFoundExpection {
      Avatar avatar = stubAvatar();
      when(avatarRepository.findById("avatar-1")).thenReturn(Optional.of(avatar));

      service.increaseStrength("avatar-1");

      ArgumentCaptor<AvatarEvent> captor = ArgumentCaptor.forClass(AvatarEvent.class);
      verify(avatarObserver).notifyAvaterEvent(captor.capture());
      assertThat(captor.getValue()).isInstanceOf(SkillPointAssigned.class);
      verify(avatarRepository).save(avatar);
    }
  }

  @Nested
  @DisplayName("increaseDefense")
  class IncreaseDefense {

    @Test
    @DisplayName("fires SkillPointAssigned for defense and saves")
    void firesEvent() throws AvatarNotFoundExpection {
      Avatar avatar = stubAvatar();
      when(avatarRepository.findById("avatar-1")).thenReturn(Optional.of(avatar));

      service.increaseDefense("avatar-1");

      ArgumentCaptor<AvatarEvent> captor = ArgumentCaptor.forClass(AvatarEvent.class);
      verify(avatarObserver).notifyAvaterEvent(captor.capture());
      assertThat(captor.getValue()).isInstanceOf(SkillPointAssigned.class);
      verify(avatarRepository).save(avatar);
    }
  }

  @Nested
  @DisplayName("increaseIntelligence")
  class IncreaseIntelligence {

    @Test
    @DisplayName("fires SkillPointAssigned for intelligence and saves")
    void firesEvent() throws AvatarNotFoundExpection {
      Avatar avatar = stubAvatar();
      when(avatarRepository.findById("avatar-1")).thenReturn(Optional.of(avatar));

      service.increaseIntelligence("avatar-1");

      ArgumentCaptor<AvatarEvent> captor = ArgumentCaptor.forClass(AvatarEvent.class);
      verify(avatarObserver).notifyAvaterEvent(captor.capture());
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
    void addToInventory() throws AvatarNotFoundExpection {
      Avatar avatar = stubAvatar();
      when(avatarRepository.findById("avatar-1")).thenReturn(Optional.of(avatar));

      service.addToInventory("avatar-1", sword);

      assertThat(avatar.getInventory().getItems()).contains(sword);
      verify(avatarRepository).save(avatar);
    }

    @Test
    @DisplayName("removeItem removes item and saves")
    void removeItem() throws AvatarNotFoundExpection {
      Avatar avatar = stubAvatar();
      avatar.addItemToInventory(sword);
      when(avatarRepository.findById("avatar-1")).thenReturn(Optional.of(avatar));

      service.removeItem("avatar-1", sword);

      assertThat(avatar.getInventory().getItems()).doesNotContain(sword);
      verify(avatarRepository).save(avatar);
    }

    @Test
    @DisplayName("equipItem moves item to equipped slots and saves")
    void equipItem() throws AvatarNotFoundExpection {
      Avatar avatar = stubAvatar();
      avatar.addItemToInventory(sword);
      when(avatarRepository.findById("avatar-1")).thenReturn(Optional.of(avatar));

      service.equipItem("avatar-1", sword);

      assertThat(avatar.getEquippedItems().getItems()).contains(sword);
      assertThat(avatar.getInventory().getItems()).doesNotContain(sword);
      verify(avatarRepository).save(avatar);
    }

    @Test
    @DisplayName("unequipItem moves item back to inventory and saves")
    void unequipItem() throws AvatarNotFoundExpection {
      Avatar avatar = stubAvatar();
      avatar.addItemToInventory(sword);
      avatar.equipItem(sword);
      when(avatarRepository.findById("avatar-1")).thenReturn(Optional.of(avatar));

      service.unequipItem("avatar-1", sword);

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
    void getName() throws AvatarNotFoundExpection {
      Avatar avatar = stubAvatar();
      when(avatarRepository.findById("avatar-1")).thenReturn(Optional.of(avatar));
      assertThat(service.getName("avatar-1")).isEqualTo("Hero");
    }

    @Test
    @DisplayName("getMoney delegates to avatar.getMoney()")
    void getMoney() throws AvatarNotFoundExpection {
      Avatar avatar = stubAvatar();
      when(avatarRepository.findById("avatar-1")).thenReturn(Optional.of(avatar));
      assertThat(service.getMoney("avatar-1").amount()).isZero();
    }

    @Test
    @DisplayName("getLevel delegates to avatar.getLevel()")
    void getLevel() throws AvatarNotFoundExpection {
      Avatar avatar = stubAvatar();
      when(avatarRepository.findById("avatar-1")).thenReturn(Optional.of(avatar));
      assertThat(service.getLevel("avatar-1").levelNumber()).isEqualTo(1);
    }

    @Test
    @DisplayName("getHealth delegates to avatar.getHealth()")
    void getHealth() throws AvatarNotFoundExpection {
      Avatar avatar = stubAvatar();
      when(avatarRepository.findById("avatar-1")).thenReturn(Optional.of(avatar));
      assertThat(service.getHealth("avatar-1").current().value()).isEqualTo(100);
    }

    @Test
    @DisplayName("getMana delegates to avatar.getMana()")
    void getMana() throws AvatarNotFoundExpection {
      Avatar avatar = stubAvatar();
      when(avatarRepository.findById("avatar-1")).thenReturn(Optional.of(avatar));
      assertThat(service.getMana("avatar-1").amount().value()).isEqualTo(50);
    }

    @Test
    @DisplayName("getExperience returns the current XP from the level")
    void getExperience() throws AvatarNotFoundExpection {
      Avatar avatar = stubAvatar();
      avatar.gainExperience(30);
      when(avatarRepository.findById("avatar-1")).thenReturn(Optional.of(avatar));
      assertThat(service.getExperience("avatar-1").amount()).isEqualTo(30);
    }

    @Test
    @DisplayName("getAvatarStats delegates to avatar.getAvatarStats()")
    void getAvatarStats() throws AvatarNotFoundExpection {
      Avatar avatar = stubAvatar();
      when(avatarRepository.findById("avatar-1")).thenReturn(Optional.of(avatar));
      assertThat(service.getAvatarStats("avatar-1")).isSameAs(avatar.getAvatarStats());
    }
  }
}
