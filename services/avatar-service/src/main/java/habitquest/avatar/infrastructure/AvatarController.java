package habitquest.avatar.infrastructure;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

import habitquest.avatar.application.AvatarNotFoundException;
import habitquest.avatar.application.AvatarService;
import habitquest.avatar.domain.avatar.*;
import habitquest.avatar.domain.items.*;
import habitquest.avatar.domain.stats.AvatarStats;
import habitquest.avatar.infrastructure.dto.*;
import java.net.URI;
import java.util.List;
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
  public ResponseEntity<EntityModel<AvatarResponse>> getAvatar(@PathVariable String id)
      throws AvatarNotFoundException {

    Avatar avatar = avatarService.getAvatarById(id);

    AvatarResponse dto = AvatarMapper.toResponse(avatar);

    EntityModel<AvatarResponse> model =
        EntityModel.of(
            dto,
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

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteAvatar(@PathVariable String id) throws AvatarNotFoundException {

    avatarService.deleteAvatar(id);
    return ResponseEntity.noContent().build();
  }

  @PatchMapping("/{id}/name")
  public ResponseEntity<Void> updateName(
      @PathVariable String id, @RequestBody UpdateNameRequest request)
      throws AvatarNotFoundException {

    avatarService.updateName(id, request.name());
    return ResponseEntity.noContent().build();
  }

  // ─── Queries ────────────────────────────────────────────────────────────────

  @GetMapping("/{id}/name")
  public ResponseEntity<EntityModel<NameResponse>> getName(@PathVariable String id)
      throws AvatarNotFoundException {

    String name = avatarService.getName(id);
    EntityModel<NameResponse> model =
        EntityModel.of(new NameResponse(name), selfLink(id), avatarLink(id));

    return ResponseEntity.ok(model);
  }

  @GetMapping("/{id}/money")
  public ResponseEntity<EntityModel<MoneyResponse>> getMoney(@PathVariable String id)
      throws AvatarNotFoundException {

    Money money = avatarService.getMoney(id);
    MoneyResponse dto = AvatarMapper.toResponse(money);
    EntityModel<MoneyResponse> model =
        EntityModel.of(
            dto,
            selfLink(id),
            avatarLink(id),
            linkTo(methodOn(AvatarController.class).earnMoney(id, null)).withRel("earn"),
            linkTo(methodOn(AvatarController.class).spendMoney(id, null)).withRel("spend"));

    return ResponseEntity.ok(model);
  }

  @GetMapping("/{id}/inventory")
  public ResponseEntity<EntityModel<InventoryResponse>> getInventory(@PathVariable String id)
      throws AvatarNotFoundException {

    Inventory inventory = avatarService.getInventory(id);
    InventoryResponse dto = AvatarMapper.toResponse(inventory);
    EntityModel<InventoryResponse> model =
        EntityModel.of(
            dto,
            selfLink(id),
            avatarLink(id),
            linkTo(methodOn(AvatarController.class).addItem(id, null)).withRel("addItem"),
            linkTo(methodOn(AvatarController.class).removeItem(id, null)).withRel("removeItem"),
            linkTo(methodOn(AvatarController.class).getEquippedItems(id)).withRel("equippedItems"));

    return ResponseEntity.ok(model);
  }

  @GetMapping("/{id}/equipped-items")
  public ResponseEntity<EntityModel<EquippedItemsResponse>> getEquippedItems(
      @PathVariable String id) throws AvatarNotFoundException {

    EquippedItems equippedItems = avatarService.getEquippedItems(id);
    EquippedItemsResponse dto = AvatarMapper.toResponse(equippedItems);
    EntityModel<EquippedItemsResponse> model =
        EntityModel.of(
            dto,
            selfLink(id),
            avatarLink(id),
            linkTo(methodOn(AvatarController.class).getInventory(id)).withRel("inventory"));

    return ResponseEntity.ok(model);
  }

  @GetMapping("/{id}/experience")
  public ResponseEntity<EntityModel<ExperienceResponse>> getExperience(@PathVariable String id)
      throws AvatarNotFoundException {

    Experience experience = avatarService.getExperience(id);
    ExperienceResponse dto = AvatarMapper.toResponse(experience);
    EntityModel<ExperienceResponse> model =
        EntityModel.of(
            dto,
            selfLink(id),
            avatarLink(id),
            linkTo(methodOn(AvatarController.class).getLevel(id)).withRel("level"),
            linkTo(methodOn(AvatarController.class).grantExperience(id, null)).withRel("grant"));

    return ResponseEntity.ok(model);
  }

  @GetMapping("/{id}/level")
  public ResponseEntity<EntityModel<LevelResponse>> getLevel(@PathVariable String id)
      throws AvatarNotFoundException {

    Level level = avatarService.getLevel(id);
    LevelResponse dto = AvatarMapper.toResponse(level);
    EntityModel<LevelResponse> model = EntityModel.of(dto, selfLink(id), avatarLink(id));

    return ResponseEntity.ok(model);
  }

  @GetMapping("/{id}/health")
  public ResponseEntity<EntityModel<HealthResponse>> getHealth(@PathVariable String id)
      throws AvatarNotFoundException {

    AvatarHealth health = avatarService.getHealth(id);
    HealthResponse dto = AvatarMapper.toResponse(health);
    EntityModel<HealthResponse> model =
        EntityModel.of(
            dto,
            selfLink(id),
            avatarLink(id),
            linkTo(methodOn(AvatarController.class).healAvatar(id, null)).withRel("heal"),
            linkTo(methodOn(AvatarController.class).applyDamage(id, null)).withRel("damage"));

    return ResponseEntity.ok(model);
  }

  @GetMapping("/{id}/mana")
  public ResponseEntity<EntityModel<ManaResponse>> getMana(@PathVariable String id)
      throws AvatarNotFoundException {

    AvatarMana mana = avatarService.getMana(id);
    ManaResponse dto = AvatarMapper.toResponse(mana);
    EntityModel<ManaResponse> model =
        EntityModel.of(
            dto,
            selfLink(id),
            avatarLink(id),
            linkTo(methodOn(AvatarController.class).restoreMana(id, null)).withRel("restore"),
            linkTo(methodOn(AvatarController.class).spendMana(id, null)).withRel("spend"));

    return ResponseEntity.ok(model);
  }

  @GetMapping("/{id}/stats")
  public ResponseEntity<EntityModel<StatsResponse>> getStats(@PathVariable String id)
      throws AvatarNotFoundException {

    AvatarStats stats = avatarService.getAvatarStats(id);
    StatsResponse dto = AvatarMapper.toResponse(stats);
    EntityModel<StatsResponse> model =
        EntityModel.of(
            dto,
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
      throws AvatarNotFoundException {

    avatarService.earnMoney(id, request.amount());
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{id}/money/spend")
  public ResponseEntity<Void> spendMoney(
      @PathVariable String id, @RequestBody AmountRequest request) throws AvatarNotFoundException {

    avatarService.spendMoney(id, request.amount());
    return ResponseEntity.noContent().build();
  }

  // ─── Inventory ──────────────────────────────────────────────────────────────

  @PostMapping("/{id}/inventory/items")
  public ResponseEntity<Void> addItem(@PathVariable String id, @RequestBody ItemRequest request)
      throws AvatarNotFoundException {

    avatarService.addToInventory(id, ItemMapper.toDomain(request));
    return ResponseEntity.noContent().build();
  }

  @DeleteMapping("/{id}/inventory/items")
  public ResponseEntity<Void> removeItem(@PathVariable String id, @RequestBody ItemRequest request)
      throws AvatarNotFoundException {

    avatarService.removeItem(id, ItemMapper.toDomain(request));
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{id}/inventory/items/equip")
  public ResponseEntity<Void> equipItem(@PathVariable String id, @RequestBody ItemRequest request)
      throws AvatarNotFoundException {

    avatarService.equipItem(id, ItemMapper.toDomain(request));
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{id}/inventory/items/unequip")
  public ResponseEntity<Void> unequipItem(@PathVariable String id, @RequestBody ItemRequest request)
      throws AvatarNotFoundException {

    avatarService.unequipItem(id, ItemMapper.toDomain(request));
    return ResponseEntity.noContent().build();
  }

  // ─── Combat ─────────────────────────────────────────────────────────────────

  @PostMapping("/{id}/health/damage")
  public ResponseEntity<Void> applyDamage(
      @PathVariable String id, @RequestBody AmountRequest request) throws AvatarNotFoundException {

    avatarService.applyDamage(id, request.amount());
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{id}/health/heal")
  public ResponseEntity<Void> healAvatar(
      @PathVariable String id, @RequestBody AmountRequest request) throws AvatarNotFoundException {

    avatarService.healAvatar(id, request.amount());
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{id}/mana/spend")
  public ResponseEntity<Void> spendMana(@PathVariable String id, @RequestBody AmountRequest request)
      throws AvatarNotFoundException {

    avatarService.spendMana(id, request.amount());
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{id}/mana/restore")
  public ResponseEntity<Void> restoreMana(
      @PathVariable String id, @RequestBody AmountRequest request) throws AvatarNotFoundException {

    avatarService.restoreMana(id, request.amount());
    return ResponseEntity.noContent().build();
  }

  // ─── Progression ────────────────────────────────────────────────────────────

  @PostMapping("/{id}/experience/grant")
  public ResponseEntity<Void> grantExperience(
      @PathVariable String id, @RequestBody AmountRequest request) throws AvatarNotFoundException {

    avatarService.grantExperience(id, request.amount());
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{id}/stats/strength")
  public ResponseEntity<Void> increaseStrength(@PathVariable String id)
      throws AvatarNotFoundException {

    avatarService.increaseStrength(id);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{id}/stats/defense")
  public ResponseEntity<Void> increaseDefense(@PathVariable String id)
      throws AvatarNotFoundException {

    avatarService.increaseDefense(id);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{id}/stats/intelligence")
  public ResponseEntity<Void> increaseIntelligence(@PathVariable String id)
      throws AvatarNotFoundException {

    avatarService.increaseIntelligence(id);
    return ResponseEntity.noContent().build();
  }

  // ─── Exception handling ──────────────────────────────────────────────────────
  @ExceptionHandler(AvatarNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleAvatarNotFound(AvatarNotFoundException ex) {
    return ResponseEntity.notFound().build();
  }

  @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
  public ResponseEntity<ErrorResponse> handleDomainError(RuntimeException ex) {
    return ResponseEntity.badRequest().body(new ErrorResponse(ex.getMessage()));
  }

  // ─── HATEOAS helpers ───────────────────────────────────────────────────────
  private Link selfLink(String id) {
    try {
      return linkTo(methodOn(AvatarController.class).getAvatar(id)).withSelfRel();
    } catch (AvatarNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  private Link avatarLink(String id) {
    return linkTo(methodOn(AvatarController.class).getAvatar(id)).withRel("avatar");
  }

  public record CreateAvatarRequest(String name) {}

  public record UpdateNameRequest(String name) {}

  public record AmountRequest(Integer amount) {}

  public record AvatarCreatedResponse(String id) {}

  public record NameResponse(String name) {}

  public record ItemResponse(String type, String name, String description, Integer power) {}

  public record ItemRequest(String type, String name, String description, Integer power) {}

  public record ExperienceResponse(Integer amount) {}

  public record EquippedItemsResponse(String id, List<ItemResponse> items) {}

  public record InventoryResponse(String id, List<ItemResponse> items) {}

  public record StatsResponse(Integer strength, Integer defense, Integer intelligence) {}

  public record MoneyResponse(Integer amount) {}

  public record ManaResponse(Integer amount, Integer max) {}

  public record HealthResponse(Integer current, Integer max) {}

  public record LevelResponse(
      Integer levelNumber, Integer currentExperience, Integer experienceRequired) {}

  public record ErrorResponse(String message) {}
}
