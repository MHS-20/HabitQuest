package habitquest.avatar.infrastructure.inbound;

import common.ddd.Id;
import habitquest.avatar.application.exceptions.AvatarNotFoundException;
import habitquest.avatar.application.exceptions.MarketplaceCommunicationException;
import habitquest.avatar.application.port.in.AvatarService;
import habitquest.avatar.application.port.out.AvatarLogger;
import habitquest.avatar.application.service.AvatarSearchRequest;
import habitquest.avatar.domain.avatar.*;
import habitquest.avatar.domain.items.*;
import habitquest.avatar.domain.stats.AvatarStats;
import habitquest.avatar.infrastructure.dto.*;
import habitquest.avatar.infrastructure.dto.AvatarRequestsDto.*;
import habitquest.avatar.infrastructure.dto.AvatarResponsesDto.*;
import java.net.URI;
import java.time.Instant;
import java.util.List;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/avatars")
public class AvatarController {

  private final AvatarService avatarService;
  private final AvatarResponseAssembler assembler;
  private final AvatarLogger log;

  public AvatarController(
      AvatarService avatarService, AvatarResponseAssembler assembler, AvatarLogger log) {
    this.avatarService = avatarService;
    this.assembler = assembler;
    this.log = log;
  }

  private Id<Avatar> idOf(String id) {
    return new Id<>(id);
  }

  private Id<Guild> guildIdOf(String id) {
    return new Id<>(id);
  }

  private Id<Invite> inviteIdOf(String id) {
    return new Id<>(id);
  }

  // ─── Avatar CRUD ────────────────────────────────────────────────────────────

  @PostMapping
  public ResponseEntity<EntityModel<AvatarCreatedResponse>> createAvatar(
      @RequestBody CreateAvatarRequest request) {
    Id<Avatar> id = avatarService.createAvatar(new Id<>(request.id()), request.name());
    EntityModel<AvatarCreatedResponse> model = assembler.toCreatedModel(id.value());
    log.info(model.getContent(), "Avatar created");
    return ResponseEntity.created(URI.create("/api/v1/avatars/" + id.value())).body(model);
  }

  @GetMapping("/{id}")
  public ResponseEntity<EntityModel<AvatarResponse>> getAvatar(@PathVariable String id)
      throws AvatarNotFoundException {
    Avatar avatar = avatarService.getAvatarById(idOf(id));
    log.info(avatar, "Avatar retrieved");
    return ResponseEntity.ok(assembler.toModel(avatar));
  }

  @PostMapping("/search")
  public ResponseEntity<CollectionModel<EntityModel<AvatarResponse>>> searchAvatars(
      @RequestBody AvatarSearchRequest query) {
    List<Avatar> avatars = avatarService.searchAvatars(query);
    log.info(query, "Avatar search executed, results: " + avatars.size());
    return ResponseEntity.ok(assembler.toCollectionModel(avatars, query));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteAvatar(@PathVariable String id) throws AvatarNotFoundException {
    avatarService.deleteAvatar(idOf(id));
    log.info(new AvatarCreatedResponse(id), "Avatar deleted");
    return ResponseEntity.noContent().build();
  }

  @PatchMapping("/{id}/name")
  public ResponseEntity<Void> updateName(
      @PathVariable String id, @RequestBody UpdateNameRequest request)
      throws AvatarNotFoundException {
    avatarService.updateName(idOf(id), request.name());
    log.info(request, "Avatar name updated for id: " + id);
    return ResponseEntity.noContent().build();
  }

  // ─── Guild Invites ───────────────────────────────────────────────────────────
  @PostMapping("/{id}/invites")
  public ResponseEntity<Void> receiveGuildInvite(
      @PathVariable String id, @RequestBody GuildInviteRequest request)
      throws AvatarNotFoundException {

    Instant expiresAt =
        request.expiresAt() == null || request.expiresAt().isBlank()
            ? Instant.now().plusSeconds(86400)
            : Instant.parse(request.expiresAt());

    avatarService.addPendingInvite(
        idOf(id),
        new Invite(
            inviteIdOf(request.inviteId()),
            guildIdOf(request.guildId()),
            request.guildName(),
            expiresAt));

    log.info(request, "Guild invite received for avatar id: " + id);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/{id}/invites")
  public ResponseEntity<CollectionModel<EntityModel<InviteResponse>>> getPendingInvites(
      @PathVariable String id) throws AvatarNotFoundException {
    Avatar avatar = avatarService.getAvatarById(idOf(id));
    List<Invite> invites = avatar.getPendingInvites();
    log.info(invites, "Pending guild invites retrieved for avatar id: " + id);
    return ResponseEntity.ok(assembler.toInvitesModel(invites));
  }

  @PostMapping("/{id}/invites/{inviteId}/accept")
  public ResponseEntity<Void> acceptGuildInvite(
      @PathVariable String id, @PathVariable String inviteId) throws AvatarNotFoundException {
    avatarService.acceptInvite(idOf(id), inviteIdOf(inviteId));
    log.info(id, "Guild invite accepted for avatar id: " + id);
    return ResponseEntity.noContent().build();
  }

  // ─── Queries ────────────────────────────────────────────────────────────────

  @GetMapping("/{id}/name")
  public ResponseEntity<EntityModel<NameResponse>> getName(@PathVariable String id)
      throws AvatarNotFoundException {

    String name = avatarService.getName(idOf(id));
    log.info(new NameResponse(name), "Avatar name retrieved for id: " + id);

    return ResponseEntity.ok(assembler.toNameModel(name, id));
  }

  @GetMapping("/{id}/money")
  public ResponseEntity<EntityModel<MoneyResponse>> getMoney(@PathVariable String id)
      throws AvatarNotFoundException {

    Money money = avatarService.getMoney(idOf(id));
    EntityModel<MoneyResponse> model = assembler.toMoneyModel(money, id);
    log.info(model.getContent(), "Avatar money retrieved for id: " + id);

    return ResponseEntity.ok(model);
  }

  @GetMapping("/{id}/inventory")
  public ResponseEntity<EntityModel<InventoryResponse>> getInventory(@PathVariable String id)
      throws AvatarNotFoundException {

    List<Item> inventory = avatarService.getInventory(idOf(id));
    EntityModel<InventoryResponse> model = assembler.toInventoryModel(inventory, id);
    log.info(model.getContent(), "Avatar inventory retrieved for id: " + id);

    return ResponseEntity.ok(model);
  }

  @GetMapping("/{id}/equipped-items")
  public ResponseEntity<EntityModel<EquippedItemsResponse>> getEquippedItems(
      @PathVariable String id) throws AvatarNotFoundException {

    EquippedItems equippedItems = avatarService.getEquippedItems(idOf(id));
    EntityModel<EquippedItemsResponse> model = assembler.toEquippedItemsModel(equippedItems, id);
    log.info(model.getContent(), "Avatar equipped items retrieved for id: " + id);

    return ResponseEntity.ok(model);
  }

  @GetMapping("/{id}/experience")
  public ResponseEntity<EntityModel<ExperienceResponse>> getExperience(@PathVariable String id)
      throws AvatarNotFoundException {

    Experience experience = avatarService.getExperience(idOf(id));
    EntityModel<ExperienceResponse> model = assembler.toExperienceModel(experience, id);
    log.info(model.getContent(), "Avatar experience retrieved for id: " + id);

    return ResponseEntity.ok(model);
  }

  @GetMapping("/{id}/level")
  public ResponseEntity<EntityModel<LevelResponse>> getLevel(@PathVariable String id)
      throws AvatarNotFoundException {

    Level level = avatarService.getLevel(idOf(id));
    EntityModel<LevelResponse> model = assembler.toLevelModel(level, id);
    log.info(model.getContent(), "Avatar level retrieved for id: " + id);

    return ResponseEntity.ok(model);
  }

  @GetMapping("/{id}/health")
  public ResponseEntity<EntityModel<HealthResponse>> getHealth(@PathVariable String id)
      throws AvatarNotFoundException {

    AvatarHealth health = avatarService.getHealth(idOf(id));
    EntityModel<HealthResponse> model = assembler.toHealthModel(health, id);
    log.info(model.getContent(), "Avatar health retrieved for id: " + id);

    return ResponseEntity.ok(model);
  }

  @GetMapping("/{id}/mana")
  public ResponseEntity<EntityModel<ManaResponse>> getMana(@PathVariable String id)
      throws AvatarNotFoundException {

    AvatarMana mana = avatarService.getMana(idOf(id));
    EntityModel<ManaResponse> model = assembler.toManaModel(mana, id);
    log.info(model.getContent(), "Avatar mana retrieved for id: " + id);

    return ResponseEntity.ok(model);
  }

  @GetMapping("/{id}/stats")
  public ResponseEntity<EntityModel<StatsResponse>> getStats(@PathVariable String id)
      throws AvatarNotFoundException {

    AvatarStats stats = avatarService.getAvatarStats(idOf(id));
    EntityModel<StatsResponse> model = assembler.toStatsModel(stats, id);
    log.info(model.getContent(), "Avatar stats retrieved for id: " + id);

    return ResponseEntity.ok(model);
  }

  // ─── Money ──────────────────────────────────────────────────────────────────

  @PostMapping("/{id}/money/earn")
  public ResponseEntity<Void> earnMoney(@PathVariable String id, @RequestBody AmountRequest request)
      throws AvatarNotFoundException {

    avatarService.earnMoney(idOf(id), request.amount());
    log.info(request, "Avatar earned money for id: " + id);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{id}/money/spend")
  public ResponseEntity<Void> spendMoney(
      @PathVariable String id, @RequestBody AmountRequest request) throws AvatarNotFoundException {

    avatarService.spendMoney(idOf(id), request.amount());
    log.info(request, "Avatar spent money for id: " + id);
    return ResponseEntity.noContent().build();
  }

  // ─── Inventory ──────────────────────────────────────────────────────────────

  @PostMapping("/{id}/inventory/items")
  public ResponseEntity<Void> addItem(@PathVariable String id, @RequestBody ItemRequest request)
      throws AvatarNotFoundException {

    avatarService.addToInventory(idOf(id), ItemMapper.toDomain(request));
    log.info(request, "Item added to inventory for avatar id: " + id);
    return ResponseEntity.noContent().build();
  }

  @DeleteMapping("/{id}/inventory/items")
  public ResponseEntity<Void> removeItem(@PathVariable String id, @RequestBody ItemRequest request)
      throws AvatarNotFoundException {

    avatarService.removeItem(idOf(id), ItemMapper.toDomain(request));
    log.info(request, "Item removed from inventory for avatar id: " + id);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{id}/inventory/items/equip")
  public ResponseEntity<Void> equipItem(@PathVariable String id, @RequestBody ItemRequest request)
      throws AvatarNotFoundException {

    avatarService.equipItem(idOf(id), ItemMapper.toEquipment(request));
    log.info(request, "Item equipped for avatar id: " + id);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{id}/inventory/items/unequip")
  public ResponseEntity<Void> unequipItem(@PathVariable String id, @RequestBody ItemRequest request)
      throws AvatarNotFoundException {

    avatarService.unequipItem(idOf(id), ItemMapper.toEquipment(request));
    log.info(request, "Item unequipped for avatar id: " + id);
    return ResponseEntity.noContent().build();
  }

  // ─── Combat ─────────────────────────────────────────────────────────────────

  @PostMapping("/{id}/health/damage")
  public ResponseEntity<DamageResponse> applyDamage(
      @PathVariable String id, @RequestBody AmountRequest request) throws AvatarNotFoundException {

    boolean died = avatarService.applyDamage(idOf(id), request.amount());
    DamageResponse response = new DamageResponse(died);
    log.info(response, "Damage applied to avatar id: " + id);
    return ResponseEntity.ok(response);
  }

  @PostMapping("/{id}/health/potion")
  public ResponseEntity<Void> useHealthPotion(
      @PathVariable String id, @RequestBody PotionRequest request) throws AvatarNotFoundException {

    avatarService.useHealthPotion(idOf(id), request.potionName());
    log.info(request, "Health potion used for avatar id: " + id);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{id}/mana/potion")
  public ResponseEntity<Void> useManaPotion(
      @PathVariable String id, @RequestBody PotionRequest request) throws AvatarNotFoundException {

    avatarService.useManaPotion(idOf(id), request.potionName());
    log.info(request, "Mana potion used for avatar id: " + id);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{id}/health/heal")
  public ResponseEntity<Void> healAvatar(
      @PathVariable String id, @RequestBody AmountRequest request) throws AvatarNotFoundException {

    avatarService.healAvatar(idOf(id), request.amount());
    log.info(request, "Avatar healed for id: " + id);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{id}/mana/spend")
  public ResponseEntity<Void> spendMana(@PathVariable String id, @RequestBody AmountRequest request)
      throws AvatarNotFoundException {

    avatarService.spendMana(idOf(id), request.amount());
    log.info(request, "Avatar spent mana for id: " + id);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{id}/mana/restore")
  public ResponseEntity<Void> restoreMana(
      @PathVariable String id, @RequestBody AmountRequest request) throws AvatarNotFoundException {

    avatarService.restoreMana(idOf(id), request.amount());
    log.info(request, "Avatar mana restored for id: " + id);
    return ResponseEntity.noContent().build();
  }

  // ─── Progression ────────────────────────────────────────────────────────────

  @PostMapping("/{id}/experience/grant")
  public ResponseEntity<Void> grantExperience(
      @PathVariable String id, @RequestBody AmountRequest request) throws AvatarNotFoundException {

    avatarService.grantExperience(idOf(id), request.amount());
    log.info(request, "Experience granted to avatar id: " + id);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{id}/stats/strength")
  public ResponseEntity<Void> increaseStrength(@PathVariable String id)
      throws AvatarNotFoundException {

    avatarService.increaseStrength(idOf(id));
    log.info(new AvatarCreatedResponse(id), "Avatar strength increased for id: " + id);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{id}/stats/defense")
  public ResponseEntity<Void> increaseDefense(@PathVariable String id)
      throws AvatarNotFoundException {

    avatarService.increaseDefense(idOf(id));
    log.info(new AvatarCreatedResponse(id), "Avatar defense increased for id: " + id);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{id}/stats/intelligence")
  public ResponseEntity<Void> increaseIntelligence(@PathVariable String id)
      throws AvatarNotFoundException {

    avatarService.increaseIntelligence(idOf(id));
    log.info(new AvatarCreatedResponse(id), "Avatar intelligence increased for id: " + id);
    return ResponseEntity.noContent().build();
  }

  // ─── Exception handling ──────────────────────────────────────────────────────

  @ExceptionHandler(AvatarNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleAvatarNotFound(AvatarNotFoundException ex) {
    ErrorResponse error = new ErrorResponse(ex.getMessage());
    log.error(error, "Avatar not found", ex);
    return ResponseEntity.notFound().build();
  }

  @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
  public ResponseEntity<ErrorResponse> handleDomainError(RuntimeException ex) {
    ErrorResponse error = new ErrorResponse(ex.getMessage());
    log.error(error, "Domain error", ex);
    return ResponseEntity.badRequest().body(error);
  }

  @ExceptionHandler(MarketplaceCommunicationException.class)
  public ResponseEntity<ErrorResponse> handleMarketplaceError(RuntimeException ex) {
    ErrorResponse error = new ErrorResponse(ex.getMessage());
    log.error(error, "Marketplace communication error", ex);
    return ResponseEntity.badRequest().body(error);
  }
}
