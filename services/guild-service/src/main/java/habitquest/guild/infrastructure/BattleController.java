package habitquest.guild.infrastructure;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

import common.hexagonal.Adapter;
import habitquest.guild.application.BattleNotFoundException;
import habitquest.guild.application.BattleService;
import habitquest.guild.application.GuildNotFoundException;
import habitquest.guild.application.GuildService;
import habitquest.guild.domain.battle.Battle;
import habitquest.guild.domain.battle.BattleStatus;
import habitquest.guild.domain.battle.boss.BossEnemy;
import habitquest.guild.domain.battle.boss.BossStatus;
import habitquest.guild.domain.battle.boss.Minotaur;
import habitquest.guild.domain.guild.GuildMember;
import java.net.URI;
import java.util.List;
import java.util.Locale;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
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

    BossEnemy boss = resolveBoss(request.bossType());
    String id = battleService.createBattle(request.guildId(), boss, request.numOfTurns());
    BattleCreatedResponse body = new BattleCreatedResponse(id);

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
  public ResponseEntity<EntityModel<Battle>> getBattle(@PathVariable String id)
      throws BattleNotFoundException {

    Battle battle = battleService.getBattleById(id);

    EntityModel<Battle> model =
        EntityModel.of(
            battle,
            selfLink(id),
            linkTo(methodOn(BattleController.class).getBoss(id)).withRel("boss"),
            linkTo(methodOn(BattleController.class).getBossHealth(id)).withRel("bossHealth"),
            linkTo(methodOn(BattleController.class).getBattleStatus(id)).withRel("status"),
            linkTo(methodOn(BattleController.class).getCurrentTurn(id)).withRel("currentTurn"),
            linkTo(methodOn(BattleController.class).getNumOfTurns(id)).withRel("numOfTurns"),
            linkTo(methodOn(BattleController.class).dealDamage(id, null)).withRel("dealDamage"),
            linkTo(methodOn(BattleController.class).nextTurn(id)).withRel("nextTurn"),
            linkTo(methodOn(BattleController.class).deleteBattle(id)).withRel("delete"));

    return ResponseEntity.ok(model);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteBattle(@PathVariable String id) {

    battleService.deleteBattle(id);
    return ResponseEntity.noContent().build();
  }

  // ─── Query ──────────────────────────────────────────────────────────────────

  @GetMapping("/guild/{guildId}")
  public ResponseEntity<EntityModel<Battle>> getBattleByGuild(@PathVariable String guildId)
      throws BattleNotFoundException {

    Battle battle = battleService.getBattleByGuild(guildId);
    String id = battle.getId();

    EntityModel<Battle> model =
        EntityModel.of(
            battle,
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
  public ResponseEntity<EntityModel<BossEnemy>> getBoss(@PathVariable String id)
      throws BattleNotFoundException {

    BossEnemy boss = battleService.getBoss(id);

    EntityModel<BossEnemy> model =
        EntityModel.of(
            boss,
            linkTo(methodOn(BattleController.class).getBoss(id)).withSelfRel(),
            selfLink(id),
            linkTo(methodOn(BattleController.class).getBossHealth(id)).withRel("bossHealth"));

    return ResponseEntity.ok(model);
  }

  @GetMapping("/{id}/boss/health")
  public ResponseEntity<EntityModel<BossStatus>> getBossHealth(@PathVariable String id)
      throws BattleNotFoundException {

    BossStatus bossStatus = battleService.getBossRemainingHealth(id);

    EntityModel<BossStatus> model =
        EntityModel.of(
            bossStatus,
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
            linkTo(methodOn(BattleController.class).getNumOfTurns(id)).withRel("numOfTurns"),
            linkTo(methodOn(BattleController.class).nextTurn(id)).withRel("nextTurn"));

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
            linkTo(methodOn(BattleController.class).getCurrentTurn(id)).withRel("currentTurn"),
            linkTo(methodOn(BattleController.class).increaseNumOfTurns(id)).withRel("increase"));

    return ResponseEntity.ok(model);
  }

  @PostMapping("/{id}/turns/next")
  public ResponseEntity<Void> nextTurn(@PathVariable String id) throws BattleNotFoundException {

    battleService.nextTurn(id);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{id}/turns/increase")
  public ResponseEntity<Void> increaseNumOfTurns(@PathVariable String id)
      throws BattleNotFoundException {

    battleService.increaseNumOfTurn(id);
    return ResponseEntity.noContent().build();
  }

  // ─── Combat ─────────────────────────────────────────────────────────────────

  @PostMapping("/{id}/damage")
  public ResponseEntity<Void> dealDamage(
      @PathVariable String id, @RequestBody DamageRequest request)
      throws BattleNotFoundException, GuildNotFoundException {
    BossEnemy boss = battleService.getBoss(id);
    String guildId = battleService.getGuildId(id);
    battleService.dealDamage(id, request.damage());
    BattleStatus newStatus = battleService.getBattleStatus(id);
    List<GuildMember> members = guildService.getMembers(guildId);

    switch (newStatus) {
      case WON -> {
        int exp = boss.experienceReward().amount();
        int money = boss.moneyReward().amount();
        for (GuildMember member : members) {
          avatarClient.grantExperience(member.getId(), exp);
          avatarClient.earnMoney(member.getId(), money);
        }
      }

      case LOST -> {
        int penalty = boss.penalty().amount();
        for (GuildMember member : members) {
          avatarClient.applyPenalty(member.getId(), penalty);
        }
      }

      case ONGOING -> {
        if (request.attackerAvatarId() != null) {
          avatarClient.applyDamage(request.attackerAvatarId(), request.damage());
        }
      }

      default -> throw new IllegalStateException("Unexpected battle status: " + newStatus);
    }

    return ResponseEntity.noContent().build();
  }

  @GetMapping("/{id}/status")
  public ResponseEntity<EntityModel<BattleStatusResponse>> getBattleStatus(@PathVariable String id)
      throws BattleNotFoundException {

    BattleStatus status = battleService.getBattleStatus(id);
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

  // ─── HATEOAS helpers ────────────────────────────────────────────────────────

  private Link selfLink(String id) {
    return Link.of("/api/v1/battles/" + id).withSelfRel();
    //    try {
    //      return linkTo(methodOn(BattleController.class).getBattle(id)).withSelfRel();
    //    } catch (BattleNotFoundException e) {
    //      throw new RuntimeException(e);
    //    }
  }

  // ─── Boss factory helper ─────────────────────────────────────────────────────

  private BossEnemy resolveBoss(String bossType) {
    return switch (bossType.toUpperCase(Locale.getDefault())) {
      case "MINOTAUR" -> Minotaur.INSTANCE;
      default -> throw new IllegalArgumentException("Unknown boss type: " + bossType);
    };
  }

  // ─── Request / Response records ─────────────────────────────────────────────

  public record CreateBattleRequest(String guildId, String bossType, Integer numOfTurns) {}

  public record DamageRequest(Integer damage, String attackerAvatarId) {}

  public record BattleCreatedResponse(String id) {}

  public record TurnResponse(Integer turn) {}

  public record InProgressResponse(boolean inProgress) {}

  public record BattleStatusResponse(BattleStatus status, boolean isOver, boolean isWon) {}

  public record ErrorResponse(String message) {}
}
