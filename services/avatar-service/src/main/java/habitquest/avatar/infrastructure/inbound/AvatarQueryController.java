package habitquest.avatar.infrastructure.inbound;

import common.ddd.Id;
import habitquest.avatar.application.exceptions.AvatarNotFoundException;
import habitquest.avatar.application.port.in.AvatarQueryService;
import habitquest.avatar.application.port.out.AvatarLogger;
import habitquest.avatar.application.service.AvatarSearchQuery;
import habitquest.avatar.domain.avatar.*;
import habitquest.avatar.domain.items.*;
import habitquest.avatar.domain.stats.AvatarStats;
import habitquest.avatar.infrastructure.dto.AvatarQueries.*;
import habitquest.avatar.infrastructure.dto.AvatarResponseAssembler;
import java.util.List;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/avatars")
public class AvatarQueryController {

  private final AvatarQueryService queryService;
  private final AvatarResponseAssembler assembler;
  private final AvatarLogger log;

  public AvatarQueryController(
      AvatarQueryService queryService, AvatarResponseAssembler assembler, AvatarLogger log) {
    this.queryService = queryService;
    this.assembler = assembler;
    this.log = log;
  }

  private Id<Avatar> idOf(String id) {
    return new Id<>(id);
  }

  @GetMapping("/{id}")
  public ResponseEntity<EntityModel<AvatarResponse>> getAvatar(@PathVariable String id)
      throws AvatarNotFoundException {
    Avatar avatar = queryService.getAvatarById(idOf(id));
    log.info(avatar, "Avatar retrieved");
    return ResponseEntity.ok(assembler.toModel(avatar));
  }

  @PostMapping("/search")
  public ResponseEntity<CollectionModel<EntityModel<AvatarResponse>>> searchAvatars(
      @RequestBody AvatarSearchQuery query) {
    List<Avatar> avatars = queryService.searchAvatars(query);
    log.info(query, "Avatar search executed, results: " + avatars.size());
    return ResponseEntity.ok(assembler.toCollectionModel(avatars, query));
  }

  @GetMapping("/{id}/invites")
  public ResponseEntity<CollectionModel<EntityModel<InviteResponse>>> getPendingInvites(
      @PathVariable String id) throws AvatarNotFoundException {
    Avatar avatar = queryService.getAvatarById(idOf(id));
    List<Invite> invites = avatar.getPendingInvites();
    log.info(invites, "Pending guild invites retrieved for avatar id: " + id);
    return ResponseEntity.ok(assembler.toInvitesModel(invites));
  }

  @GetMapping("/{id}/name")
  public ResponseEntity<EntityModel<NameResponse>> getName(@PathVariable String id)
      throws AvatarNotFoundException {
    String name = queryService.getName(idOf(id));
    log.info(new NameResponse(name), "Avatar name retrieved for id: " + id);
    return ResponseEntity.ok(assembler.toNameModel(name, id));
  }

  @GetMapping("/{id}/money")
  public ResponseEntity<EntityModel<MoneyResponse>> getMoney(@PathVariable String id)
      throws AvatarNotFoundException {
    Money money = queryService.getMoney(idOf(id));
    EntityModel<MoneyResponse> model = assembler.toMoneyModel(money, id);
    log.info(model.getContent(), "Avatar money retrieved for id: " + id);
    return ResponseEntity.ok(model);
  }

  @GetMapping("/{id}/inventory")
  public ResponseEntity<EntityModel<InventoryResponse>> getInventory(@PathVariable String id)
      throws AvatarNotFoundException {
    List<Item> inventory = queryService.getInventory(idOf(id));
    EntityModel<InventoryResponse> model = assembler.toInventoryModel(inventory, id);
    log.info(model.getContent(), "Avatar inventory retrieved for id: " + id);
    return ResponseEntity.ok(model);
  }

  @GetMapping("/{id}/equipped-items")
  public ResponseEntity<EntityModel<EquippedItemsResponse>> getEquippedItems(
      @PathVariable String id) throws AvatarNotFoundException {
    List<Equipment> equippedItems = queryService.getEquippedItems(idOf(id));
    EntityModel<EquippedItemsResponse> model = assembler.toEquippedItemsModel(equippedItems, id);
    log.info(model.getContent(), "Avatar equipped items retrieved for id: " + id);
    return ResponseEntity.ok(model);
  }

  @GetMapping("/{id}/experience")
  public ResponseEntity<EntityModel<ExperienceResponse>> getExperience(@PathVariable String id)
      throws AvatarNotFoundException {
    Experience experience = queryService.getExperience(idOf(id));
    EntityModel<ExperienceResponse> model = assembler.toExperienceModel(experience, id);
    log.info(model.getContent(), "Avatar experience retrieved for id: " + id);
    return ResponseEntity.ok(model);
  }

  @GetMapping("/{id}/level")
  public ResponseEntity<EntityModel<LevelResponse>> getLevel(@PathVariable String id)
      throws AvatarNotFoundException {
    Level level = queryService.getLevel(idOf(id));
    EntityModel<LevelResponse> model = assembler.toLevelModel(level, id);
    log.info(model.getContent(), "Avatar level retrieved for id: " + id);
    return ResponseEntity.ok(model);
  }

  @GetMapping("/{id}/health")
  public ResponseEntity<EntityModel<HealthResponse>> getHealth(@PathVariable String id)
      throws AvatarNotFoundException {
    AvatarHealth health = queryService.getHealth(idOf(id));
    EntityModel<HealthResponse> model = assembler.toHealthModel(health, id);
    log.info(model.getContent(), "Avatar health retrieved for id: " + id);
    return ResponseEntity.ok(model);
  }

  @GetMapping("/{id}/mana")
  public ResponseEntity<EntityModel<ManaResponse>> getMana(@PathVariable String id)
      throws AvatarNotFoundException {
    AvatarMana mana = queryService.getMana(idOf(id));
    EntityModel<ManaResponse> model = assembler.toManaModel(mana, id);
    log.info(model.getContent(), "Avatar mana retrieved for id: " + id);
    return ResponseEntity.ok(model);
  }

  @GetMapping("/{id}/stats")
  public ResponseEntity<EntityModel<StatsResponse>> getStats(@PathVariable String id)
      throws AvatarNotFoundException {
    AvatarStats stats = queryService.getAvatarStats(idOf(id));
    EntityModel<StatsResponse> model = assembler.toStatsModel(stats, id);
    log.info(model.getContent(), "Avatar stats retrieved for id: " + id);
    return ResponseEntity.ok(model);
  }
}
