package habitquest.guild.infrastructure.inbound;

import common.ddd.Id;
import common.hexagonal.Adapter;
import habitquest.guild.application.exceptions.BattleNotFoundException;
import habitquest.guild.application.exceptions.GuildNotFoundException;
import habitquest.guild.application.port.in.BattleService;
import habitquest.guild.application.port.in.GuildService;
import habitquest.guild.application.port.out.GuildLogger;
import habitquest.guild.domain.battle.Battle;
import habitquest.guild.domain.battle.BattleOutcome;
import habitquest.guild.domain.battle.boss.BossType;
import habitquest.guild.domain.guild.Guild;
import habitquest.guild.domain.guild.GuildMember;
import habitquest.guild.domain.guild.UnauthorizedGuildOperationException;
import habitquest.guild.infrastructure.dto.BattleRequestsDto.*;
import habitquest.guild.infrastructure.dto.BattleResponseAssembler;
import habitquest.guild.infrastructure.dto.BattleResponsesDto.*;
import java.net.URI;
import java.util.List;
import java.util.Locale;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
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
  private final BattleResponseAssembler assembler;
  private final GuildLogger log;

  public BattleController(
      BattleService battleService,
      GuildService guildService,
      BattleResponseAssembler assembler,
      GuildLogger log) {
    this.battleService = battleService;
    this.guildService = guildService;
    this.assembler = assembler;
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
    return ResponseEntity.created(URI.create("/api/v1/battles/" + id))
        .body(assembler.toCreatedModel(body));
  }

  @GetMapping("/{id}")
  public ResponseEntity<EntityModel<BattleResponse>> getBattle(@PathVariable String id)
      throws BattleNotFoundException {
    BattleResponse body = BattleResponse.from(battleService.getBattleById(idOfBattle(id)));
    log.info(body, "Battle retrieved");
    return ResponseEntity.ok(assembler.toModel(body));
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
    BattleResponse body = BattleResponse.from(battle);
    log.info(body, "Battle retrieved for guild: " + guildId);
    return ResponseEntity.ok(assembler.toModelForGuild(body, guildId));
  }

  @GetMapping("/guild/{guildId}/in-progress")
  public ResponseEntity<EntityModel<InProgressResponse>> hasBattleInProgress(
      @PathVariable String guildId) throws BattleNotFoundException {
    boolean inProgress = battleService.hasBattleInProgress(idOfGuild(guildId));
    InProgressResponse body = new InProgressResponse(inProgress);
    log.info(body, "Battle in-progress check for guild: " + guildId);
    return ResponseEntity.ok(assembler.toInProgressModel(body, guildId));
  }

  @GetMapping("/boss")
  public ResponseEntity<CollectionModel<BossResponse>> getAllBosses() {
    List<BossResponse> bosses =
        battleService.getAllBossTypes().stream().map(BossResponse::from).toList();
    log.info(bosses.size(), "All boss types retrieved");
    return ResponseEntity.ok(assembler.toAllBossesModel(bosses));
  }

  @GetMapping("/{id}/boss")
  public ResponseEntity<EntityModel<BossResponse>> getBoss(@PathVariable String id)
      throws BattleNotFoundException {
    BossResponse body = BossResponse.from(battleService.getBoss(idOfBattle(id)));
    log.info(body, "Boss retrieved for battle: " + id);
    return ResponseEntity.ok(assembler.toBossModel(body, id));
  }

  @GetMapping("/{id}/boss/health")
  public ResponseEntity<EntityModel<BossHealthResponse>> getBossHealth(@PathVariable String id)
      throws BattleNotFoundException {
    int remaining = battleService.getBossRemainingHealth(idOfBattle(id)).remainingHealth().value();
    BossHealthResponse body = new BossHealthResponse(remaining);
    log.info(body, "Boss health retrieved for battle: " + id);
    return ResponseEntity.ok(assembler.toBossHealthModel(body, id));
  }

  @GetMapping("/{id}/turns/current")
  public ResponseEntity<EntityModel<TurnResponse>> getCurrentTurn(@PathVariable String id)
      throws BattleNotFoundException {
    TurnResponse body = new TurnResponse(battleService.getCurrentTurn(idOfBattle(id)));
    log.info(body, "Current turn retrieved for battle: " + id);
    return ResponseEntity.ok(assembler.toCurrentTurnModel(body, id));
  }

  @GetMapping("/{id}/turns/total")
  public ResponseEntity<EntityModel<TurnResponse>> getNumOfTurns(@PathVariable String id)
      throws BattleNotFoundException {
    TurnResponse body = new TurnResponse(battleService.getNumOfTurns(idOfBattle(id)));
    log.info(body, "Total turns retrieved for battle: " + id);
    return ResponseEntity.ok(assembler.toTotalTurnsModel(body, id));
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
        battleService.processDamage(
            idOfBattle(id), idOfGuildMember(request.attackerAvatarId()), request.damage());

    switch (outcome) {
      case BattleOutcome.Won(int exp, int money) ->
          log.info(new BattleOutcomeLog(id, "WON", exp, money), "Battle won, distributing rewards");
      case BattleOutcome.Lost(int penalty) ->
          log.info(new BattleOutcomeLog(id, "LOST", penalty, 0), "Battle lost, applying penalties");
      case BattleOutcome.Ongoing ignored ->
          log.info(new BattleCreatedResponse(id), "Battle ongoing, advancing turn");
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
    return ResponseEntity.ok(assembler.toStatusModel(body, id));
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
}
