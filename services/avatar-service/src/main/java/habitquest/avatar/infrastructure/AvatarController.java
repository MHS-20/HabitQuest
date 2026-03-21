package habitquest.avatar.infrastructure;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

import common.ddd.Id;
import habitquest.avatar.application.AvatarNotFoundException;
import habitquest.avatar.application.AvatarSearchRequest;
import habitquest.avatar.application.AvatarService;
import habitquest.avatar.domain.avatar.*;
import habitquest.avatar.domain.items.*;
import habitquest.avatar.domain.stats.AvatarStats;
import habitquest.avatar.infrastructure.dto.*;
import java.net.URI;
import java.util.List;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/avatars")
public class AvatarController {

  private final AvatarService avatarService;
  private final MarketplaceClient marketplaceClient;

  public AvatarController(AvatarService avatarService, MarketplaceClient marketplaceClient) {
    this.avatarService = avatarService;
    this.marketplaceClient = marketplaceClient;
  }

  private Id<Avatar> idOf(String id) {
    return new Id<>(id);
  }

  // ─── Avatar CRUD ────────────────────────────────────────────────────────────
  @PostMapping
  public ResponseEntity<EntityModel<AvatarCreatedResponse>> createAvatar(
      @RequestBody CreateAvatarRequest request) {

    Id<Avatar> id = avatarService.createAvatar(new Id<>(request.id()), request.name());
    marketplaceClient.createMarketplace(request.id());
    AvatarCreatedResponse body = new AvatarCreatedResponse(id.value());

    EntityModel<AvatarCreatedResponse> model =
        EntityModel.of(
            body,
            selfLink(id.value()),
            linkTo(methodOn(AvatarController.class).getAvatar(id.value())).withRel("avatar"),
            linkTo(methodOn(AvatarController.class).getLevel(id.value())).withRel("level"),
            linkTo(methodOn(AvatarController.class).getHealth(id.value())).withRel("health"));

    return ResponseEntity.created(URI.create("/api/v1/avatars/" + id.value())).body(model);
  }

  @GetMapping("/{id}")
  public ResponseEntity<EntityModel<AvatarResponse>> getAvatar(@PathVariable String id)
      throws AvatarNotFoundException {

    Avatar avatar = avatarService.getAvatarById(idOf(id));

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

  // GET /api/v1/avatars/search
  @GetMapping("/search")
  public ResponseEntity<CollectionModel<EntityModel<AvatarResponse>>> searchAvatars(
      @RequestBody AvatarSearchRequest query) {
    List<Avatar> avatars = avatarService.searchAvatars(query);
    List<EntityModel<AvatarResponse>> avatarModels =
        avatars.stream()
            .map(
                avatar -> {
                  AvatarResponse dto = AvatarMapper.toResponse(avatar);
                  return EntityModel.of(
                      dto,
                      selfLink(avatar.getId().value()),
                      linkTo(methodOn(AvatarController.class).getAvatar(avatar.getId().value()))
                          .withRel("avatar"));
                })
            .toList();

    CollectionModel<EntityModel<AvatarResponse>> model =
        CollectionModel.of(
            avatarModels,
            linkTo(methodOn(AvatarController.class).searchAvatars(query)).withSelfRel());
    return ResponseEntity.ok(model);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteAvatar(@PathVariable String id) throws AvatarNotFoundException {

    avatarService.deleteAvatar(idOf(id));
    return ResponseEntity.noContent().build();
  }

  @PatchMapping("/{id}/name")
  public ResponseEntity<Void> updateName(
      @PathVariable String id, @RequestBody UpdateNameRequest request)
      throws AvatarNotFoundException {

    avatarService.updateName(idOf(id), request.name());
    return ResponseEntity.noContent().build();
  }

  // ─── Queries ────────────────────────────────────────────────────────────────

  @GetMapping("/{id}/name")
  public ResponseEntity<EntityModel<NameResponse>> getName(@PathVariable String id)
      throws AvatarNotFoundException {

    String name = avatarService.getName(idOf(id));
    EntityModel<NameResponse> model =
        EntityModel.of(new NameResponse(name), selfLink(id), avatarLink(id));

    return ResponseEntity.ok(model);
  }

  @GetMapping("/{id}/money")
  public ResponseEntity<EntityModel<MoneyResponse>> getMoney(@PathVariable String id)
      throws AvatarNotFoundException {

    Money money = avatarService.getMoney(idOf(id));
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

    Inventory inventory = avatarService.getInventory(idOf(id));
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

    EquippedItems equippedItems = avatarService.getEquippedItems(idOf(id));
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

    Experience experience = avatarService.getExperience(idOf(id));
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

    Level level = avatarService.getLevel(idOf(id));
    LevelResponse dto = AvatarMapper.toResponse(level);
    EntityModel<LevelResponse> model = EntityModel.of(dto, selfLink(id), avatarLink(id));

    return ResponseEntity.ok(model);
  }

  @GetMapping("/{id}/health")
  public ResponseEntity<EntityModel<HealthResponse>> getHealth(@PathVariable String id)
      throws AvatarNotFoundException {

    AvatarHealth health = avatarService.getHealth(idOf(id));
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

    AvatarMana mana = avatarService.getMana(idOf(id));
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

    AvatarStats stats = avatarService.getAvatarStats(idOf(id));
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

    avatarService.earnMoney(idOf(id), request.amount());
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{id}/money/spend")
  public ResponseEntity<Void> spendMoney(
      @PathVariable String id, @RequestBody AmountRequest request) throws AvatarNotFoundException {

    avatarService.spendMoney(idOf(id), request.amount());
    return ResponseEntity.noContent().build();
  }

  // ─── Inventory ──────────────────────────────────────────────────────────────

  @PostMapping("/{id}/inventory/items")
  public ResponseEntity<Void> addItem(@PathVariable String id, @RequestBody ItemRequest request)
      throws AvatarNotFoundException {

    avatarService.addToInventory(idOf(id), ItemMapper.toDomain(request));
    return ResponseEntity.noContent().build();
  }

  @DeleteMapping("/{id}/inventory/items")
  public ResponseEntity<Void> removeItem(@PathVariable String id, @RequestBody ItemRequest request)
      throws AvatarNotFoundException {

    avatarService.removeItem(idOf(id), ItemMapper.toDomain(request));
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{id}/inventory/items/equip")
  public ResponseEntity<Void> equipItem(@PathVariable String id, @RequestBody ItemRequest request)
      throws AvatarNotFoundException {

    avatarService.equipItem(idOf(id), ItemMapper.toDomain(request));
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{id}/inventory/items/unequip")
  public ResponseEntity<Void> unequipItem(@PathVariable String id, @RequestBody ItemRequest request)
      throws AvatarNotFoundException {

    avatarService.unequipItem(idOf(id), ItemMapper.toDomain(request));
    return ResponseEntity.noContent().build();
  }

  // ─── Combat ─────────────────────────────────────────────────────────────────

  @PostMapping("/{id}/health/damage")
  public ResponseEntity<DamageResponse> applyDamage(
      @PathVariable String id, @RequestBody AmountRequest request) throws AvatarNotFoundException {
    boolean died = avatarService.applyDamage(idOf(id), request.amount());
    return ResponseEntity.ok(new DamageResponse(died));
  }

  @PostMapping("/{id}/health/heal")
  public ResponseEntity<Void> healAvatar(
      @PathVariable String id, @RequestBody AmountRequest request) throws AvatarNotFoundException {

    avatarService.healAvatar(idOf(id), request.amount());
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{id}/mana/spend")
  public ResponseEntity<Void> spendMana(@PathVariable String id, @RequestBody AmountRequest request)
      throws AvatarNotFoundException {

    avatarService.spendMana(idOf(id), request.amount());
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{id}/mana/restore")
  public ResponseEntity<Void> restoreMana(
      @PathVariable String id, @RequestBody AmountRequest request) throws AvatarNotFoundException {

    avatarService.restoreMana(idOf(id), request.amount());
    return ResponseEntity.noContent().build();
  }

  // ─── Progression ────────────────────────────────────────────────────────────

  @PostMapping("/{id}/experience/grant")
  public ResponseEntity<Void> grantExperience(
      @PathVariable String id, @RequestBody AmountRequest request) throws AvatarNotFoundException {

    avatarService.grantExperience(idOf(id), request.amount());
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{id}/stats/strength")
  public ResponseEntity<Void> increaseStrength(@PathVariable String id)
      throws AvatarNotFoundException {

    avatarService.increaseStrength(idOf(id));
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{id}/stats/defense")
  public ResponseEntity<Void> increaseDefense(@PathVariable String id)
      throws AvatarNotFoundException {

    avatarService.increaseDefense(idOf(id));
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{id}/stats/intelligence")
  public ResponseEntity<Void> increaseIntelligence(@PathVariable String id)
      throws AvatarNotFoundException {

    avatarService.increaseIntelligence(idOf(id));
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

  @ExceptionHandler({MarketplaceCommunicationException.class})
  public ResponseEntity<ErrorResponse> handleMarketplaceError(RuntimeException ex) {
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

  public record CreateAvatarRequest(String id, String name) {}

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

  public record DamageResponse(boolean died) {}

  public record LevelResponse(
      Integer levelNumber, Integer currentExperience, Integer experienceRequired) {}

  public record ErrorResponse(String message) {}
}
