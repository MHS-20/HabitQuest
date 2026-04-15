package habitquest.avatar.infrastructure.inbound;

import common.ddd.Id;
import habitquest.avatar.application.exceptions.AvatarNotFoundException;
import habitquest.avatar.application.port.in.AvatarCommandService;
import habitquest.avatar.application.port.out.AvatarLogger;
import habitquest.avatar.domain.avatar.Avatar;
import habitquest.avatar.domain.avatar.Guild;
import habitquest.avatar.domain.avatar.Invite;
import habitquest.avatar.infrastructure.dto.AvatarCommands.*;
import habitquest.avatar.infrastructure.dto.AvatarResponseAssembler;
import habitquest.avatar.infrastructure.dto.ItemMapper;
import java.net.URI;
import java.time.Instant;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/avatars")
public class AvatarCommandController {

  private final AvatarCommandService commandService;
  private final AvatarResponseAssembler assembler;
  private final AvatarLogger log;

  public AvatarCommandController(
      AvatarCommandService commandService, AvatarResponseAssembler assembler, AvatarLogger log) {
    this.commandService = commandService;
    this.assembler = assembler;
    this.log = log;
  }

  @PostMapping
  public ResponseEntity<EntityModel<AvatarCreatedResponse>> createAvatar(
      @RequestBody CreateAvatarCommand request) {
    Id<Avatar> id = commandService.createAvatar(new Id<>(request.id()), request.name());
    EntityModel<AvatarCreatedResponse> model = assembler.toCreatedModel(id.value());
    log.info(model.getContent(), "Avatar created");
    return ResponseEntity.created(URI.create("/api/v1/avatars/" + id.value())).body(model);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteAvatar(@PathVariable String id) throws AvatarNotFoundException {
    commandService.deleteAvatar(idOf(id));
    log.info(new AvatarCreatedResponse(id), "Avatar deleted");
    return ResponseEntity.noContent().build();
  }

  @PatchMapping("/{id}/name")
  public ResponseEntity<Void> updateName(
      @PathVariable String id, @RequestBody UpdateNameCommand request)
      throws AvatarNotFoundException {
    commandService.updateName(idOf(id), request.name());
    log.info(request, "Avatar name updated for id: " + id);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{id}/invites")
  public ResponseEntity<Void> receiveGuildInvite(
      @PathVariable String id, @RequestBody GuildInviteCommand request)
      throws AvatarNotFoundException {

    Instant expiresAt =
        request.expiresAt() == null || request.expiresAt().isBlank()
            ? Instant.now().plusSeconds(86400)
            : Instant.parse(request.expiresAt());

    commandService.addPendingInvite(
        idOf(id),
        new Invite(
            inviteIdOf(request.inviteId()),
            guildIdOf(request.guildId()),
            request.guildName(),
            expiresAt));

    log.info(request, "Guild invite received for avatar id: " + id);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{id}/invites/{inviteId}/accept")
  public ResponseEntity<Void> acceptGuildInvite(
      @PathVariable String id, @PathVariable String inviteId) throws AvatarNotFoundException {
    commandService.acceptInvite(idOf(id), inviteIdOf(inviteId));
    log.info(id, "Guild invite accepted for avatar id: " + id);
    return ResponseEntity.noContent().build();
  }

  // Money
  @PostMapping("/{id}/money/earn")
  public ResponseEntity<Void> earnMoney(@PathVariable String id, @RequestBody MoneyCommand request)
      throws AvatarNotFoundException {
    commandService.earnMoney(idOf(id), ItemMapper.toMoney(request));
    log.info(request, "Avatar earned money for id: " + id);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{id}/money/spend")
  public ResponseEntity<Void> spendMoney(@PathVariable String id, @RequestBody MoneyCommand request)
      throws AvatarNotFoundException {
    commandService.spendMoney(idOf(id), ItemMapper.toMoney(request));
    log.info(request, "Avatar spent money for id: " + id);
    return ResponseEntity.noContent().build();
  }

  // Inventory mutations
  @PostMapping("/{id}/inventory/items")
  public ResponseEntity<Void> addItem(@PathVariable String id, @RequestBody ItemCommand request)
      throws AvatarNotFoundException {
    commandService.addToInventory(idOf(id), ItemMapper.toDomain(request));
    log.info(request, "Item added to inventory for avatar id: " + id);
    return ResponseEntity.noContent().build();
  }

  @DeleteMapping("/{id}/inventory/items")
  public ResponseEntity<Void> removeItem(@PathVariable String id, @RequestBody ItemCommand request)
      throws AvatarNotFoundException {
    commandService.removeItem(idOf(id), ItemMapper.toDomain(request));
    log.info(request, "Item removed from inventory for avatar id: " + id);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{id}/inventory/items/equip")
  public ResponseEntity<Void> equipItem(@PathVariable String id, @RequestBody ItemCommand request)
      throws AvatarNotFoundException {
    commandService.equipItem(idOf(id), ItemMapper.toEquipment(request));
    log.info(request, "Item equipped for avatar id: " + id);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{id}/inventory/items/unequip")
  public ResponseEntity<Void> unequipItem(@PathVariable String id, @RequestBody ItemCommand request)
      throws AvatarNotFoundException {
    commandService.unequipItem(idOf(id), ItemMapper.toEquipment(request));
    log.info(request, "Item unequipped for avatar id: " + id);
    return ResponseEntity.noContent().build();
  }

  // Combat & potions
  @PostMapping("/{id}/health/damage")
  public ResponseEntity<DamageResponse> applyDamage(
      @PathVariable String id, @RequestBody ApplyDamageCommand request)
      throws AvatarNotFoundException {
    boolean died = commandService.applyDamage(idOf(id), request.amount());
    DamageResponse response = new DamageResponse(died);
    log.info(response, "Damage applied to avatar id: " + id);
    return ResponseEntity.ok(response);
  }

  @PostMapping("/{id}/health/potion")
  public ResponseEntity<Void> useHealthPotion(
      @PathVariable String id, @RequestBody UsePotionCommand request)
      throws AvatarNotFoundException {
    commandService.useHealthPotion(idOf(id), request.potionName());
    log.info(request, "Health potion used for avatar id: " + id);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{id}/mana/potion")
  public ResponseEntity<Void> useManaPotion(
      @PathVariable String id, @RequestBody UsePotionCommand request)
      throws AvatarNotFoundException {
    commandService.useManaPotion(idOf(id), request.potionName());
    log.info(request, "Mana potion used for avatar id: " + id);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{id}/mana/spend")
  public ResponseEntity<Void> spendMana(
      @PathVariable String id, @RequestBody SpendManaCommand request)
      throws AvatarNotFoundException {
    commandService.spendMana(idOf(id), request.amount());
    log.info(request, "Avatar spent mana for id: " + id);
    return ResponseEntity.noContent().build();
  }

  // Progression
  @PostMapping("/{id}/experience/grant")
  public ResponseEntity<Void> grantExperience(
      @PathVariable String id, @RequestBody GrantExperienceCommand request)
      throws AvatarNotFoundException {
    commandService.grantExperience(idOf(id), request.amount());
    log.info(request, "Experience granted to avatar id: " + id);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{id}/stats/strength")
  public ResponseEntity<Void> increaseStrength(@PathVariable String id)
      throws AvatarNotFoundException {
    commandService.increaseStrength(idOf(id));
    log.info(new AvatarCreatedResponse(id), "Avatar strength increased for id: " + id);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{id}/stats/defense")
  public ResponseEntity<Void> increaseDefense(@PathVariable String id)
      throws AvatarNotFoundException {
    commandService.increaseDefense(idOf(id));
    log.info(new AvatarCreatedResponse(id), "Avatar defense increased for id: " + id);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{id}/stats/intelligence")
  public ResponseEntity<Void> increaseIntelligence(@PathVariable String id)
      throws AvatarNotFoundException {
    commandService.increaseIntelligence(idOf(id));
    log.info(new AvatarCreatedResponse(id), "Avatar intelligence increased for id: " + id);
    return ResponseEntity.noContent().build();
  }

  // private helpers
  private Id<Avatar> idOf(String id) {
    return new Id<>(id);
  }

  private Id<Guild> guildIdOf(String id) {
    return new Id<>(id);
  }

  private Id<Invite> inviteIdOf(String id) {
    return new Id<>(id);
  }
}
