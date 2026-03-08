package habitquest.avatar.infrastructure;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

import habitquest.avatar.application.AvatarNotFoundExpection;
import habitquest.avatar.application.AvatarService;
import habitquest.avatar.domain.avatar.*;
import habitquest.avatar.domain.items.*;
import habitquest.avatar.domain.stats.AvatarStats;
import java.net.URI;
import java.util.Locale;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/avatars")
public class AvatarController {

  private final AvatarService avatarService;

  public AvatarController(AvatarService avatarService) {
    this.avatarService = avatarService;
  }

  // ─── Avatar CRUD ────────────────────────────────────────────────────────────

  @PostMapping
  public ResponseEntity<EntityModel<AvatarCreatedResponse>> createAvatar(
      @RequestBody CreateAvatarRequest request) {

    String id = avatarService.createAvatar(request.name());
    AvatarCreatedResponse body = new AvatarCreatedResponse(id);

    EntityModel<AvatarCreatedResponse> model =
        EntityModel.of(
            body,
            selfLink(id),
            linkTo(methodOn(AvatarController.class).getAvatar(id)).withRel("avatar"),
            linkTo(methodOn(AvatarController.class).getLevel(id)).withRel("level"),
            linkTo(methodOn(AvatarController.class).getHealth(id)).withRel("health"));

    return ResponseEntity.created(URI.create("/api/v1/avatars/" + id)).body(model);
  }

  @GetMapping("/{id}")
  public ResponseEntity<EntityModel<Avatar>> getAvatar(@PathVariable String id)
      throws AvatarNotFoundExpection {

    Avatar avatar = avatarService.getAvatarById(id);

    EntityModel<Avatar> model =
        EntityModel.of(
            avatar,
            selfLink(id),
            linkTo(methodOn(AvatarController.class).getInventory(id)).withRel("inventory"),
            linkTo(methodOn(AvatarController.class).getEquippedItems(id)).withRel("equippedItems"),
            linkTo(methodOn(AvatarController.class).getStats(id)).withRel("stats"),
            linkTo(methodOn(AvatarController.class).getLevel(id)).withRel("level"),
            linkTo(methodOn(AvatarController.class).getHealth(id)).withRel("health"),
            linkTo(methodOn(AvatarController.class).getMana(id)).withRel("mana"),
            linkTo(methodOn(AvatarController.class).getMoney(id)).withRel("money"),
            linkTo(methodOn(AvatarController.class).deleteAvatar(id)).withRel("delete"));

    return ResponseEntity.ok(model);
  }

  @PutMapping("/{id}")
  public ResponseEntity<Void> updateAvatar(
      @PathVariable String id, @RequestBody Avatar updatedAvatar) throws AvatarNotFoundExpection {

    avatarService.updateAvatar(id, updatedAvatar);
    return ResponseEntity.noContent().build();
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteAvatar(@PathVariable String id) throws AvatarNotFoundExpection {

    avatarService.deleteAvatar(id);
    return ResponseEntity.noContent().build();
  }

  @PatchMapping("/{id}/name")
  public ResponseEntity<Void> updateName(
      @PathVariable String id, @RequestBody UpdateNameRequest request)
      throws AvatarNotFoundExpection {

    avatarService.updateName(id, request.name());
    return ResponseEntity.noContent().build();
  }

  // ─── Queries ────────────────────────────────────────────────────────────────

  @GetMapping("/{id}/name")
  public ResponseEntity<EntityModel<NameResponse>> getName(@PathVariable String id)
      throws AvatarNotFoundExpection {

    String name = avatarService.getName(id);
    EntityModel<NameResponse> model =
        EntityModel.of(new NameResponse(name), selfLink(id), avatarLink(id));

    return ResponseEntity.ok(model);
  }

  @GetMapping("/{id}/money")
  public ResponseEntity<EntityModel<Money>> getMoney(@PathVariable String id)
      throws AvatarNotFoundExpection {

    Money money = avatarService.getMoney(id);
    EntityModel<Money> model =
        EntityModel.of(
            money,
            selfLink(id),
            avatarLink(id),
            linkTo(methodOn(AvatarController.class).earnMoney(id, null)).withRel("earn"),
            linkTo(methodOn(AvatarController.class).spendMoney(id, null)).withRel("spend"));

    return ResponseEntity.ok(model);
  }

  @GetMapping("/{id}/inventory")
  public ResponseEntity<EntityModel<Inventory>> getInventory(@PathVariable String id)
      throws AvatarNotFoundExpection {

    Inventory inventory = avatarService.getInventory(id);
    EntityModel<Inventory> model =
        EntityModel.of(
            inventory,
            selfLink(id),
            avatarLink(id),
            linkTo(methodOn(AvatarController.class).addItem(id, null)).withRel("addItem"),
            linkTo(methodOn(AvatarController.class).removeItem(id, null)).withRel("removeItem"),
            linkTo(methodOn(AvatarController.class).getEquippedItems(id)).withRel("equippedItems"));

    return ResponseEntity.ok(model);
  }

  @GetMapping("/{id}/equipped-items")
  public ResponseEntity<EntityModel<EquippedItems>> getEquippedItems(@PathVariable String id)
      throws AvatarNotFoundExpection {

    EquippedItems equippedItems = avatarService.getEquippedItems(id);
    EntityModel<EquippedItems> model =
        EntityModel.of(
            equippedItems,
            selfLink(id),
            avatarLink(id),
            linkTo(methodOn(AvatarController.class).getInventory(id)).withRel("inventory"));

    return ResponseEntity.ok(model);
  }

  @GetMapping("/{id}/experience")
  public ResponseEntity<EntityModel<Experience>> getExperience(@PathVariable String id)
      throws AvatarNotFoundExpection {

    Experience experience = avatarService.getExperience(id);
    EntityModel<Experience> model =
        EntityModel.of(
            experience,
            selfLink(id),
            avatarLink(id),
            linkTo(methodOn(AvatarController.class).getLevel(id)).withRel("level"),
            linkTo(methodOn(AvatarController.class).grantExperience(id, null)).withRel("grant"));

    return ResponseEntity.ok(model);
  }

  @GetMapping("/{id}/level")
  public ResponseEntity<EntityModel<Level>> getLevel(@PathVariable String id)
      throws AvatarNotFoundExpection {

    Level level = avatarService.getLevel(id);
    EntityModel<Level> model = EntityModel.of(level, selfLink(id), avatarLink(id));

    return ResponseEntity.ok(model);
  }

  @GetMapping("/{id}/health")
  public ResponseEntity<EntityModel<AvatarHealth>> getHealth(@PathVariable String id)
      throws AvatarNotFoundExpection {

    AvatarHealth health = avatarService.getHealth(id);
    EntityModel<AvatarHealth> model =
        EntityModel.of(
            health,
            selfLink(id),
            avatarLink(id),
            linkTo(methodOn(AvatarController.class).healAvatar(id, null)).withRel("heal"),
            linkTo(methodOn(AvatarController.class).applyDamage(id, null)).withRel("damage"));

    return ResponseEntity.ok(model);
  }

  @GetMapping("/{id}/mana")
  public ResponseEntity<EntityModel<AvatarMana>> getMana(@PathVariable String id)
      throws AvatarNotFoundExpection {

    AvatarMana mana = avatarService.getMana(id);
    EntityModel<AvatarMana> model =
        EntityModel.of(
            mana,
            selfLink(id),
            avatarLink(id),
            linkTo(methodOn(AvatarController.class).restoreMana(id, null)).withRel("restore"),
            linkTo(methodOn(AvatarController.class).spendMana(id, null)).withRel("spend"));

    return ResponseEntity.ok(model);
  }

  @GetMapping("/{id}/stats")
  public ResponseEntity<EntityModel<AvatarStats>> getStats(@PathVariable String id)
      throws AvatarNotFoundExpection {

    AvatarStats stats = avatarService.getAvatarStats(id);
    EntityModel<AvatarStats> model =
        EntityModel.of(
            stats,
            selfLink(id),
            avatarLink(id),
            linkTo(methodOn(AvatarController.class).increaseStrength(id))
                .withRel("increaseStrength"),
            linkTo(methodOn(AvatarController.class).increaseDefense(id)).withRel("increaseDefense"),
            linkTo(methodOn(AvatarController.class).increaseIntelligence(id))
                .withRel("increaseIntelligence"));

    return ResponseEntity.ok(model);
  }

  // ─── Money ──────────────────────────────────────────────────────────────────

  @PostMapping("/{id}/money/earn")
  public ResponseEntity<Void> earnMoney(@PathVariable String id, @RequestBody AmountRequest request)
      throws AvatarNotFoundExpection {

    avatarService.earnMoney(id, request.amount());
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{id}/money/spend")
  public ResponseEntity<Void> spendMoney(
      @PathVariable String id, @RequestBody AmountRequest request) throws AvatarNotFoundExpection {

    avatarService.spendMoney(id, request.amount());
    return ResponseEntity.noContent().build();
  }

  // ─── Inventory ──────────────────────────────────────────────────────────────

  @PostMapping("/{id}/inventory/items")
  public ResponseEntity<Void> addItem(@PathVariable String id, @RequestBody ItemRequest request)
      throws AvatarNotFoundExpection {

    avatarService.addToInventory(id, request.toItem());
    return ResponseEntity.noContent().build();
  }

  @DeleteMapping("/{id}/inventory/items")
  public ResponseEntity<Void> removeItem(@PathVariable String id, @RequestBody ItemRequest request)
      throws AvatarNotFoundExpection {

    avatarService.removeItem(id, request.toItem());
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{id}/inventory/items/equip")
  public ResponseEntity<Void> equipItem(@PathVariable String id, @RequestBody ItemRequest request)
      throws AvatarNotFoundExpection {

    avatarService.equipItem(id, request.toItem());
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{id}/inventory/items/unequip")
  public ResponseEntity<Void> unequipItem(@PathVariable String id, @RequestBody ItemRequest request)
      throws AvatarNotFoundExpection {

    avatarService.unequipItem(id, request.toItem());
    return ResponseEntity.noContent().build();
  }

  // ─── Combat ─────────────────────────────────────────────────────────────────

  @PostMapping("/{id}/health/damage")
  public ResponseEntity<Void> applyDamage(
      @PathVariable String id, @RequestBody AmountRequest request) throws AvatarNotFoundExpection {

    avatarService.applyDamage(id, request.amount());
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{id}/health/heal")
  public ResponseEntity<Void> healAvatar(
      @PathVariable String id, @RequestBody AmountRequest request) throws AvatarNotFoundExpection {

    avatarService.healAvatar(id, request.amount());
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{id}/mana/spend")
  public ResponseEntity<Void> spendMana(@PathVariable String id, @RequestBody AmountRequest request)
      throws AvatarNotFoundExpection {

    avatarService.spendMana(id, request.amount());
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{id}/mana/restore")
  public ResponseEntity<Void> restoreMana(
      @PathVariable String id, @RequestBody AmountRequest request) throws AvatarNotFoundExpection {

    avatarService.restoreMana(id, request.amount());
    return ResponseEntity.noContent().build();
  }

  // ─── Progression ────────────────────────────────────────────────────────────

  @PostMapping("/{id}/experience/grant")
  public ResponseEntity<Void> grantExperience(
      @PathVariable String id, @RequestBody AmountRequest request) throws AvatarNotFoundExpection {

    avatarService.grantExperience(id, request.amount());
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{id}/stats/strength")
  public ResponseEntity<Void> increaseStrength(@PathVariable String id)
      throws AvatarNotFoundExpection {

    avatarService.increaseStrength(id);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{id}/stats/defense")
  public ResponseEntity<Void> increaseDefense(@PathVariable String id)
      throws AvatarNotFoundExpection {

    avatarService.increaseDefense(id);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{id}/stats/intelligence")
  public ResponseEntity<Void> increaseIntelligence(@PathVariable String id)
      throws AvatarNotFoundExpection {

    avatarService.increaseIntelligence(id);
    return ResponseEntity.noContent().build();
  }

  // ─── Exception handling ──────────────────────────────────────────────────────

  @ExceptionHandler(AvatarNotFoundExpection.class)
  public ResponseEntity<ErrorResponse> handleAvatarNotFound(AvatarNotFoundExpection ex) {
    return ResponseEntity.notFound().build();
  }

  @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
  public ResponseEntity<ErrorResponse> handleDomainError(RuntimeException ex) {
    return ResponseEntity.badRequest().body(new ErrorResponse(ex.getMessage()));
  }

  // ─── HATEOAS helpers ────────────────────────────────────────────────────────

  private Link selfLink(String id) {
    try {
      return linkTo(methodOn(AvatarController.class).getAvatar(id)).withSelfRel();
    } catch (AvatarNotFoundExpection e) {
      throw new RuntimeException(e);
    }
  }

  private Link avatarLink(String id) {
    try {
      return linkTo(methodOn(AvatarController.class).getAvatar(id)).withRel("avatar");
    } catch (AvatarNotFoundExpection e) {
      throw new RuntimeException(e);
    }
  }

  // ─── Request / Response records ─────────────────────────────────────────────

  public record CreateAvatarRequest(String name) {}

  public record UpdateNameRequest(String name) {}

  public record AmountRequest(Integer amount) {}

  public record AvatarCreatedResponse(String id) {}

  public record NameResponse(String name) {}

  public record ErrorResponse(String message) {}

  public record ItemRequest(String type, String name, String description, Integer power) {
    public Item toItem() {
      int p = power != null ? power : 0;
      return switch (type.toUpperCase(Locale.getDefault())) {
        case "WEAPON" -> new Weapon(name, description, p);
        case "ARMOR" -> new Armor(name, description, p);
        case "HEALTH_POTION" -> new HealthPotion(name, description, p);
        case "MANA_POTION" -> new ManaPotion(name, description, p);
        default -> throw new IllegalArgumentException("Unknown item type: " + type);
      };
    }
  }
}
