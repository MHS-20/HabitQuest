package habitquest.guild.infrastructure;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

import common.hexagonal.Adapter;
import habitquest.guild.application.BattleNotFoundException;
import habitquest.guild.application.BattleService;
import habitquest.guild.application.GuildNotFoundException;
import habitquest.guild.application.GuildService;
import habitquest.guild.domain.battle.BattleOutcome;
import habitquest.guild.domain.battle.boss.BossType;
import habitquest.guild.domain.guild.UnauthorizedGuildOperationException;
import habitquest.guild.infrastructure.dto.BattleResponse;
import habitquest.guild.infrastructure.dto.BossResponse;
import java.net.URI;
import java.util.Locale;
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

  public BattleController(
      BattleService battleService, GuildService guildService, AvatarClient avatarClient) {
    this.battleService = battleService;
    this.guildService = guildService;
    this.avatarClient = avatarClient;
  }

  // ─── Battle lifecycle ────────────────────────────────────────────────────────
  @PostMapping
  public ResponseEntity<EntityModel<BattleCreatedResponse>> createBattle(
      @RequestBody CreateBattleRequest request) {

    BossType bossType;
    try {
      bossType = BossType.valueOf(request.bossType().toUpperCase(Locale.getDefault()));
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().build();
    }

    if (!guildService.isLeader(request.guildId(), request.requesterId())) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    String id =
        battleService.createBattle(
            request.guildId(), bossType, guildService.getMembers(request.guildId()).size());

    EntityModel<BattleCreatedResponse> model =
        EntityModel.of(
            new BattleCreatedResponse(id),
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

    EntityModel<BattleResponse> model =
        EntityModel.of(
            BattleResponse.from(battleService.getBattleById(id)),
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
    if (!guildService.isLeader(request.guildId(), request.requesterId)) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
    battleService.deleteBattle(id);
    return ResponseEntity.noContent().build();
  }

  // ─── Query ──────────────────────────────────────────────────────────────────
  @GetMapping("/guild/{guildId}")
  public ResponseEntity<EntityModel<BattleResponse>> getBattleByGuild(@PathVariable String guildId)
      throws BattleNotFoundException {

    var battle =
        battleService
            .getBattleByGuild(guildId)
            .orElseThrow(
                () -> new BattleNotFoundException("No battle found for guild: " + guildId));
    String id = battle.getId();

    EntityModel<BattleResponse> model =
        EntityModel.of(
            BattleResponse.from(battle),
            linkTo(methodOn(BattleController.class).getBattleByGuild(guildId)).withSelfRel(),
            selfLink(id),
            linkTo(methodOn(BattleController.class).getBattleStatus(id)).withRel("status"),
            linkTo(methodOn(BattleController.class).dealDamage(id, null)).withRel("dealDamage"));

    return ResponseEntity.ok(model);
  }

  @GetMapping("/guild/{guildId}/in-progress")
  public ResponseEntity<EntityModel<InProgressResponse>> hasBattleInProgress(
      @PathVariable String guildId) throws BattleNotFoundException {

    boolean inProgress = battleService.hasBattleInProgress(guildId);

    EntityModel<InProgressResponse> model =
        EntityModel.of(
            new InProgressResponse(inProgress),
            linkTo(methodOn(BattleController.class).hasBattleInProgress(guildId)).withSelfRel(),
            linkTo(methodOn(BattleController.class).getBattleByGuild(guildId)).withRel("battle"));

    return ResponseEntity.ok(model);
  }

  // ─── Boss info ───────────────────────────────────────────────────────────────

  @GetMapping("/{id}/boss")
  public ResponseEntity<EntityModel<BossResponse>> getBoss(@PathVariable String id)
      throws BattleNotFoundException {
    EntityModel<BossResponse> model =
        EntityModel.of(
            BossResponse.from(battleService.getBoss(id)),
            linkTo(methodOn(BattleController.class).getBoss(id)).withSelfRel(),
            selfLink(id),
            linkTo(methodOn(BattleController.class).getBossHealth(id)).withRel("bossHealth"));

    return ResponseEntity.ok(model);
  }

  @GetMapping("/{id}/boss/health")
  public ResponseEntity<EntityModel<BossHealthResponse>> getBossHealth(@PathVariable String id)
      throws BattleNotFoundException {
    int remaining = battleService.getBossRemainingHealth(id).remainingHealth().value();
    EntityModel<BossHealthResponse> model =
        EntityModel.of(
            new BossHealthResponse(remaining),
            linkTo(methodOn(BattleController.class).getBossHealth(id)).withSelfRel(),
            selfLink(id),
            linkTo(methodOn(BattleController.class).dealDamage(id, null)).withRel("dealDamage"));

    return ResponseEntity.ok(model);
  }

  // ─── Turn management ────────────────────────────────────────────────────────
  @GetMapping("/{id}/turns/current")
  public ResponseEntity<EntityModel<TurnResponse>> getCurrentTurn(@PathVariable String id)
      throws BattleNotFoundException {
    Integer currentTurn = battleService.getCurrentTurn(id);

    EntityModel<TurnResponse> model =
        EntityModel.of(
            new TurnResponse(currentTurn),
            linkTo(methodOn(BattleController.class).getCurrentTurn(id)).withSelfRel(),
            selfLink(id),
            linkTo(methodOn(BattleController.class).getNumOfTurns(id)).withRel("numOfTurns"));

    return ResponseEntity.ok(model);
  }

  @GetMapping("/{id}/turns/total")
  public ResponseEntity<EntityModel<TurnResponse>> getNumOfTurns(@PathVariable String id)
      throws BattleNotFoundException {
    Integer numOfTurns = battleService.getNumOfTurns(id);

    EntityModel<TurnResponse> model =
        EntityModel.of(
            new TurnResponse(numOfTurns),
            linkTo(methodOn(BattleController.class).getNumOfTurns(id)).withSelfRel(),
            selfLink(id),
            linkTo(methodOn(BattleController.class).getCurrentTurn(id)).withRel("currentTurn"));

    return ResponseEntity.ok(model);
  }

  // ─── Combat ─────────────────────────────────────────────────────────────────
  @PostMapping("/{id}/damage")
  public ResponseEntity<Void> dealDamage(
      @PathVariable String id, @RequestBody DamageRequest request)
      throws BattleNotFoundException, GuildNotFoundException {

    if (request.attackerAvatarId() == null) {
      return ResponseEntity.badRequest().build();
    }
    if (!battleService.isAttackerTurn(id, request.attackerAvatarId())) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    // Passo 1: l'attaccante colpisce il boss
    BattleOutcome outcome =
        battleService.dealDamageOnBoss(id, request.attackerAvatarId(), request.damage());

    // Passo 2: se il boss non è morto, il boss contrattacca l'attaccante
    if (outcome instanceof BattleOutcome.Ongoing) {
      boolean attackerDied =
          avatarClient.applyDamage(request.attackerAvatarId(), request.damage()).died();
      if (attackerDied) {
        outcome = battleService.applyCounterattack(id, request.attackerAvatarId());
      }
    }

    // Passo 3: controllo dello stato
    switch (outcome) {
      case BattleOutcome.Won(int exp, int money) ->
          battleService
              .getBattleById(id)
              .getMembers()
              .forEach(
                  m -> {
                    avatarClient.grantExperience(m, exp);
                    avatarClient.earnMoney(m, money);
                  });
      case BattleOutcome.Lost(int penalty) ->
          battleService
              .getBattleById(id)
              .getMembers()
              .forEach(m -> avatarClient.applyPenalty(m, penalty));
      case BattleOutcome.Ongoing ignored -> battleService.nextTurn(id);
    }

    return ResponseEntity.noContent().build();
  }

  @GetMapping("/{id}/status")
  public ResponseEntity<EntityModel<BattleStatusResponse>> getBattleStatus(@PathVariable String id)
      throws BattleNotFoundException {

    BattleOutcome status = battleService.getBattleStatus(id);
    boolean isOver = battleService.isBattleOver(id);
    boolean isWon = battleService.isBattleWon(id);

    EntityModel<BattleStatusResponse> model =
        EntityModel.of(
            new BattleStatusResponse(status, isOver, isWon),
            linkTo(methodOn(BattleController.class).getBattleStatus(id)).withSelfRel(),
            selfLink(id),
            linkTo(methodOn(BattleController.class).getBossHealth(id)).withRel("bossHealth"),
            linkTo(methodOn(BattleController.class).getCurrentTurn(id)).withRel("currentTurn"));

    return ResponseEntity.ok(model);
  }

  // ─── Exception handling ──────────────────────────────────────────────────────
  @ExceptionHandler(BattleNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleBattleNotFound(BattleNotFoundException ex) {
    return ResponseEntity.notFound().build();
  }

  @ExceptionHandler(GuildNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleGuildNotFound(GuildNotFoundException ex) {
    return ResponseEntity.notFound().build();
  }

  @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
  public ResponseEntity<ErrorResponse> handleDomainError(RuntimeException ex) {
    return ResponseEntity.badRequest().body(new ErrorResponse(ex.getMessage()));
  }

  @ExceptionHandler(UnauthorizedGuildOperationException.class)
  public ResponseEntity<ErrorResponse> handleUnauthorized(UnauthorizedGuildOperationException ex) {
    return ResponseEntity.status(403).body(new ErrorResponse(ex.getMessage()));
  }

  // ─── HATEOAS helpers ────────────────────────────────────────────────────────
  private Link selfLink(String id) {
    return Link.of("/api/v1/battles/" + id).withSelfRel();
  }

  // ─── Request / Response records ─────────────────────────────────────────────
  public record CreateBattleRequest(String guildId, String bossType, String requesterId) {}

  public record DeleteBattleRequest(String guildId, String requesterId) {}

  public record DamageRequest(Integer damage, String attackerAvatarId) {}

  public record BattleCreatedResponse(String id) {}

  public record BossHealthResponse(int remainingHealth) {}

  public record TurnResponse(Integer turn) {}

  public record InProgressResponse(boolean inProgress) {}

  public record BattleStatusResponse(BattleOutcome status, boolean isOver, boolean isWon) {}

  public record ErrorResponse(String message) {}
}
