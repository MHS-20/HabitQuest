package habitquest.guild.infrastructure;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

import common.ddd.Id;
import common.hexagonal.Adapter;
import habitquest.guild.application.BattleNotFoundException;
import habitquest.guild.application.BattleService;
import habitquest.guild.application.GuildLogger;
import habitquest.guild.application.GuildNotFoundException;
import habitquest.guild.application.GuildService;
import habitquest.guild.domain.battle.Battle;
import habitquest.guild.domain.battle.BattleOutcome;
import habitquest.guild.domain.battle.boss.BossType;
import habitquest.guild.domain.guild.Guild;
import habitquest.guild.domain.guild.GuildMember;
import habitquest.guild.domain.guild.UnauthorizedGuildOperationException;
import habitquest.guild.infrastructure.dto.BattleResponse;
import habitquest.guild.infrastructure.dto.BossResponse;
import java.net.URI;
import java.util.List;
import java.util.Locale;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Adapter
@RestController
@RequestMapping("/api/v1/battles")
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public class BattleController {

  private final BattleService battleService;
  private final GuildService guildService;
  private final AvatarClient avatarClient;
  private final GuildLogger log;

  public BattleController(
      BattleService battleService,
      GuildService guildService,
      AvatarClient avatarClient,
      GuildLogger log) {
    this.battleService = battleService;
    this.guildService = guildService;
    this.avatarClient = avatarClient;
    this.log = log;
  }

  private Id<Battle> idOfBattle(String id) {
    return new Id<>(id);
  }

  private Id<Guild> idOfGuild(String id) {
    return new Id<>(id);
  }

  private Id<GuildMember> idOfGuildMember(String id) {
    return new Id<>(id);
  }

  @PostMapping
  public ResponseEntity<EntityModel<BattleCreatedResponse>> createBattle(
      @RequestBody CreateBattleRequest request) {
    BossType bossType;
    try {
      bossType = BossType.valueOf(request.bossType().toUpperCase(Locale.getDefault()));
    } catch (IllegalArgumentException e) {
      log.warn(request, "Invalid boss type: " + request.bossType());
      return ResponseEntity.badRequest().build();
    }
    if (!guildService.isLeader(
        idOfGuild(request.guildId()), idOfGuildMember(request.requesterId()))) {
      log.warn(request, "Unauthorized battle creation attempt");
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
    var guildMembers = guildService.getMembers(idOfGuild(request.guildId()));
    String id =
        battleService
            .createBattle(
                idOfGuild(request.guildId()),
                bossType,
                guildMembers.size(),
                guildMembers.stream().map(GuildMember::getId).toList())
            .value();
    BattleCreatedResponse body = new BattleCreatedResponse(id);
    log.info(body, "Battle created");
    EntityModel<BattleCreatedResponse> model =
        EntityModel.of(
            body,
            selfLink(id),
            linkTo(methodOn(BattleController.class).getBattle(id)).withRel("battle"),
            linkTo(methodOn(BattleController.class).getBoss(id)).withRel("boss"),
            linkTo(methodOn(BattleController.class).getBattleStatus(id)).withRel("status"),
            linkTo(methodOn(BattleController.class).getCurrentTurn(id)).withRel("currentTurn"));
    return ResponseEntity.created(URI.create("/api/v1/battles/" + id)).body(model);
  }

  @GetMapping("/{id}")
  public ResponseEntity<EntityModel<BattleResponse>> getBattle(@PathVariable String id)
      throws BattleNotFoundException {
    BattleResponse body = BattleResponse.from(battleService.getBattleById(idOfBattle(id)));
    log.info(body, "Battle retrieved");
    EntityModel<BattleResponse> model =
        EntityModel.of(
            body,
            selfLink(id),
            linkTo(methodOn(BattleController.class).getBoss(id)).withRel("boss"),
            linkTo(methodOn(BattleController.class).getBossHealth(id)).withRel("bossHealth"),
            linkTo(methodOn(BattleController.class).getBattleStatus(id)).withRel("status"),
            linkTo(methodOn(BattleController.class).getCurrentTurn(id)).withRel("currentTurn"),
            linkTo(methodOn(BattleController.class).getNumOfTurns(id)).withRel("numOfTurns"),
            linkTo(methodOn(BattleController.class).dealDamage(id, null)).withRel("dealDamage"));
    return ResponseEntity.ok(model);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteBattle(
      @PathVariable String id, @RequestBody DeleteBattleRequest request) {
    if (!guildService.isLeader(
        idOfGuild(request.guildId()), idOfGuildMember(request.requesterId()))) {
      log.warn(request, "Unauthorized battle deletion attempt");
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
    battleService.deleteBattle(idOfBattle(id));
    log.info(new BattleCreatedResponse(id), "Battle deleted");
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/guild/{guildId}")
  public ResponseEntity<EntityModel<BattleResponse>> getBattleByGuild(@PathVariable String guildId)
      throws BattleNotFoundException {
    var battle =
        battleService
            .getBattleByGuild(idOfGuild(guildId))
            .orElseThrow(
                () -> new BattleNotFoundException("No battle found for guild: " + guildId));
    String id = battle.getId().value();
    BattleResponse body = BattleResponse.from(battle);
    log.info(body, "Battle retrieved for guild: " + guildId);
    EntityModel<BattleResponse> model =
        EntityModel.of(
            body,
            linkTo(methodOn(BattleController.class).getBattleByGuild(guildId)).withSelfRel(),
            selfLink(id),
            linkTo(methodOn(BattleController.class).getBattleStatus(id)).withRel("status"),
            linkTo(methodOn(BattleController.class).dealDamage(id, null)).withRel("dealDamage"));
    return ResponseEntity.ok(model);
  }

  @GetMapping("/guild/{guildId}/in-progress")
  public ResponseEntity<EntityModel<InProgressResponse>> hasBattleInProgress(
      @PathVariable String guildId) throws BattleNotFoundException {
    boolean inProgress = battleService.hasBattleInProgress(idOfGuild(guildId));
    InProgressResponse body = new InProgressResponse(inProgress);
    log.info(body, "Battle in-progress check for guild: " + guildId);
    EntityModel<InProgressResponse> model =
        EntityModel.of(
            body,
            linkTo(methodOn(BattleController.class).hasBattleInProgress(guildId)).withSelfRel(),
            linkTo(methodOn(BattleController.class).getBattleByGuild(guildId)).withRel("battle"));
    return ResponseEntity.ok(model);
  }

  @GetMapping("/boss")
  public ResponseEntity<CollectionModel<BossResponse>> getAllBosses() {
    List<BossResponse> bosses =
        battleService.getAllBossTypes().stream().map(BossResponse::from).toList();
    log.info(bosses.size(), "All boss types retrieved");
    CollectionModel<BossResponse> model =
        CollectionModel.of(
            bosses, linkTo(methodOn(BattleController.class).getAllBosses()).withSelfRel());
    return ResponseEntity.ok(model);
  }

  @GetMapping("/{id}/boss")
  public ResponseEntity<EntityModel<BossResponse>> getBoss(@PathVariable String id)
      throws BattleNotFoundException {
    BossResponse body = BossResponse.from(battleService.getBoss(idOfBattle(id)));
    log.info(body, "Boss retrieved for battle: " + id);
    EntityModel<BossResponse> model =
        EntityModel.of(
            body,
            linkTo(methodOn(BattleController.class).getBoss(id)).withSelfRel(),
            selfLink(id),
            linkTo(methodOn(BattleController.class).getBossHealth(id)).withRel("bossHealth"));
    return ResponseEntity.ok(model);
  }

  @GetMapping("/{id}/boss/health")
  public ResponseEntity<EntityModel<BossHealthResponse>> getBossHealth(@PathVariable String id)
      throws BattleNotFoundException {
    int remaining = battleService.getBossRemainingHealth(idOfBattle(id)).remainingHealth().value();
    BossHealthResponse body = new BossHealthResponse(remaining);
    log.info(body, "Boss health retrieved for battle: " + id);
    EntityModel<BossHealthResponse> model =
        EntityModel.of(
            body,
            linkTo(methodOn(BattleController.class).getBossHealth(id)).withSelfRel(),
            selfLink(id),
            linkTo(methodOn(BattleController.class).dealDamage(id, null)).withRel("dealDamage"));
    return ResponseEntity.ok(model);
  }

  @GetMapping("/{id}/turns/current")
  public ResponseEntity<EntityModel<TurnResponse>> getCurrentTurn(@PathVariable String id)
      throws BattleNotFoundException {
    Integer currentTurn = battleService.getCurrentTurn(idOfBattle(id));
    TurnResponse body = new TurnResponse(currentTurn);
    log.info(body, "Current turn retrieved for battle: " + id);
    EntityModel<TurnResponse> model =
        EntityModel.of(
            body,
            linkTo(methodOn(BattleController.class).getCurrentTurn(id)).withSelfRel(),
            selfLink(id),
            linkTo(methodOn(BattleController.class).getNumOfTurns(id)).withRel("numOfTurns"));
    return ResponseEntity.ok(model);
  }

  @GetMapping("/{id}/turns/total")
  public ResponseEntity<EntityModel<TurnResponse>> getNumOfTurns(@PathVariable String id)
      throws BattleNotFoundException {
    Integer numOfTurns = battleService.getNumOfTurns(idOfBattle(id));
    TurnResponse body = new TurnResponse(numOfTurns);
    log.info(body, "Total turns retrieved for battle: " + id);
    EntityModel<TurnResponse> model =
        EntityModel.of(
            body,
            linkTo(methodOn(BattleController.class).getNumOfTurns(id)).withSelfRel(),
            selfLink(id),
            linkTo(methodOn(BattleController.class).getCurrentTurn(id)).withRel("currentTurn"));
    return ResponseEntity.ok(model);
  }

  @PostMapping("/{id}/damage")
  public ResponseEntity<Void> dealDamage(
      @PathVariable String id, @RequestBody DamageRequest request)
      throws BattleNotFoundException, GuildNotFoundException {
    if (request.attackerAvatarId() == null) {
      log.warn(request, "Damage request missing attackerAvatarId");
      return ResponseEntity.badRequest().build();
    }
    if (!battleService.isAttackerTurn(
        idOfBattle(id), idOfGuildMember(request.attackerAvatarId()))) {
      log.warn(request, "Not attacker's turn in battle: " + id);
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
    log.info(request, "Dealing damage in battle: " + id);
    BattleOutcome outcome =
        battleService.dealDamageOnBoss(
            idOfBattle(id), idOfGuildMember(request.attackerAvatarId()), request.damage());

    if (outcome instanceof BattleOutcome.Ongoing) {
      boolean attackerDied =
          avatarClient.applyDamage(request.attackerAvatarId(), request.damage()).died();
      if (attackerDied) {
        outcome =
            battleService.applyCounterattack(
                idOfBattle(id), idOfGuildMember(request.attackerAvatarId()));
      }
    }

    switch (outcome) {
      case BattleOutcome.Won(int exp, int money) -> {
        log.info(new BattleOutcomeLog(id, "WON", exp, money), "Battle won, distributing rewards");
        battleService
            .getBattleById(idOfBattle(id))
            .getMembers()
            .forEach(
                m -> {
                  avatarClient.grantExperience(m.value(), exp);
                  avatarClient.earnMoney(m.value(), money);
                });
      }
      case BattleOutcome.Lost(int penalty) -> {
        log.info(new BattleOutcomeLog(id, "LOST", penalty, 0), "Battle lost, applying penalties");
        battleService
            .getBattleById(idOfBattle(id))
            .getMembers()
            .forEach(m -> avatarClient.applyPenalty(m.value(), penalty));
      }
      case BattleOutcome.Ongoing ignored -> {
        log.info(new BattleCreatedResponse(id), "Battle ongoing, advancing turn");
        battleService.nextTurn(idOfBattle(id));
      }
    }

    return ResponseEntity.noContent().build();
  }

  @GetMapping("/{id}/status")
  public ResponseEntity<EntityModel<BattleStatusResponse>> getBattleStatus(@PathVariable String id)
      throws BattleNotFoundException {
    BattleOutcome status = battleService.getBattleStatus(idOfBattle(id));
    boolean isOver = battleService.isBattleOver(idOfBattle(id));
    boolean isWon = battleService.isBattleWon(idOfBattle(id));
    BattleStatusResponse body = new BattleStatusResponse(status, isOver, isWon);
    log.info(body, "Battle status retrieved for battle: " + id);
    EntityModel<BattleStatusResponse> model =
        EntityModel.of(
            body,
            linkTo(methodOn(BattleController.class).getBattleStatus(id)).withSelfRel(),
            selfLink(id),
            linkTo(methodOn(BattleController.class).getBossHealth(id)).withRel("bossHealth"),
            linkTo(methodOn(BattleController.class).getCurrentTurn(id)).withRel("currentTurn"));
    return ResponseEntity.ok(model);
  }

  @ExceptionHandler(BattleNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleBattleNotFound(BattleNotFoundException ex) {
    log.warn(ex, "Battle not found");
    return ResponseEntity.notFound().build();
  }

  @ExceptionHandler(GuildNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleGuildNotFound(GuildNotFoundException ex) {
    log.warn(ex, "Guild not found");
    return ResponseEntity.notFound().build();
  }

  @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
  public ResponseEntity<ErrorResponse> handleDomainError(RuntimeException ex) {
    ErrorResponse error = new ErrorResponse(ex.getMessage());
    log.warn(error, "Domain error");
    return ResponseEntity.badRequest().body(error);
  }

  @ExceptionHandler(UnauthorizedGuildOperationException.class)
  public ResponseEntity<ErrorResponse> handleUnauthorized(UnauthorizedGuildOperationException ex) {
    ErrorResponse error = new ErrorResponse(ex.getMessage());
    log.warn(error, "Unauthorized guild operation");
    return ResponseEntity.status(403).body(error);
  }

  private Link selfLink(String id) {
    return Link.of("/api/v1/battles/" + id).withSelfRel();
  }

  public record CreateBattleRequest(String guildId, String bossType, String requesterId) {}

  public record DeleteBattleRequest(String guildId, String requesterId) {}

  public record DamageRequest(Integer damage, String attackerAvatarId) {}

  public record BattleCreatedResponse(String id) {}

  public record BossHealthResponse(int remainingHealth) {}

  public record TurnResponse(Integer turn) {}

  public record InProgressResponse(boolean inProgress) {}

  public record BattleStatusResponse(BattleOutcome status, boolean isOver, boolean isWon) {}

  public record ErrorResponse(String message) {}

  private record BattleOutcomeLog(String battleId, String outcome, int primary, int secondary) {}
}
