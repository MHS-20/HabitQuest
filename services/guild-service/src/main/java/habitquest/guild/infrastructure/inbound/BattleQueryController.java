package habitquest.guild.infrastructure.inbound;

import common.ddd.Id;
import common.hexagonal.Adapter;
import habitquest.guild.application.exceptions.BattleNotFoundException;
import habitquest.guild.application.port.in.BattleQueryService;
import habitquest.guild.application.port.in.GuildQueryService;
import habitquest.guild.application.port.out.GuildLogger;
import habitquest.guild.domain.battle.Battle;
import habitquest.guild.domain.battle.BattleOutcome;
import habitquest.guild.infrastructure.dto.*;
import habitquest.guild.infrastructure.dto.BattleQueries.*;
import java.util.List;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Adapter
@RestController
@RequestMapping("/api/v1/battles")
public class BattleQueryController {

  private final BattleQueryService battleQueryService;
  private final GuildQueryService guildQueryService;
  private final BattleResponseAssembler assembler;
  private final GuildLogger log;

  public BattleQueryController(
      BattleQueryService battleQueryService,
      GuildQueryService guildQueryService,
      BattleResponseAssembler assembler,
      GuildLogger log) {
    this.battleQueryService = battleQueryService;
    this.guildQueryService = guildQueryService;
    this.assembler = assembler;
    this.log = log;
  }

  private Id<Battle> idOfBattle(String id) {
    return new Id<>(id);
  }

  private Id<habitquest.guild.domain.guild.Guild> idOfGuild(String id) {
    return new Id<>(id);
  }

  @GetMapping("/{id}")
  public ResponseEntity<EntityModel<BattleQueries.BattleResponse>> getBattle(
      @PathVariable String id) throws BattleNotFoundException {
    var body = BattleQueries.BattleResponse.from(battleQueryService.getBattleById(idOfBattle(id)));
    log.info(body, "Battle retrieved");
    return ResponseEntity.ok(assembler.toModel(body));
  }

  @GetMapping("/guild/{guildId}")
  public ResponseEntity<EntityModel<BattleQueries.BattleResponse>> getBattleByGuild(
      @PathVariable String guildId) throws BattleNotFoundException {
    var battle =
        battleQueryService
            .getBattleByGuild(idOfGuild(guildId))
            .orElseThrow(
                () -> new BattleNotFoundException("No battle found for guild: " + guildId));
    var body = BattleQueries.BattleResponse.from(battle);
    log.info(body, "Battle retrieved for guild: " + guildId);
    return ResponseEntity.ok(assembler.toModelForGuild(body, guildId));
  }

  @GetMapping("/guild/{guildId}/in-progress")
  public ResponseEntity<EntityModel<InProgressResponse>> hasBattleInProgress(
      @PathVariable String guildId) throws BattleNotFoundException {
    boolean inProgress = battleQueryService.hasBattleInProgress(idOfGuild(guildId));
    InProgressResponse body = new InProgressResponse(inProgress);
    log.info(body, "Battle in-progress check for guild: " + guildId);
    return ResponseEntity.ok(assembler.toInProgressModel(body, guildId));
  }

  @GetMapping("/boss")
  public ResponseEntity<CollectionModel<BossResponse>> getAllBosses() {
    List<BossResponse> bosses =
        battleQueryService.getAllBossTypes().stream().map(BossResponse::from).toList();
    log.info(bosses.size(), "All boss types retrieved");
    return ResponseEntity.ok(assembler.toAllBossesModel(bosses));
  }

  @GetMapping("/{id}/boss")
  public ResponseEntity<EntityModel<BattleQueries.BossResponse>> getBoss(@PathVariable String id)
      throws BattleNotFoundException {
    var body = BattleQueries.BossResponse.from(battleQueryService.getBoss(idOfBattle(id)));
    log.info(body, "Boss retrieved for battle: " + id);
    return ResponseEntity.ok(assembler.toBossModel(body, id));
  }

  @GetMapping("/{id}/boss/health")
  public ResponseEntity<EntityModel<BossHealthResponse>> getBossHealth(@PathVariable String id)
      throws BattleNotFoundException {
    int remaining =
        battleQueryService.getBossRemainingHealth(idOfBattle(id)).remainingHealth().value();
    BossHealthResponse body = new BossHealthResponse(remaining);
    log.info(body, "Boss health retrieved for battle: " + id);
    return ResponseEntity.ok(assembler.toBossHealthModel(body, id));
  }

  @GetMapping("/{id}/turns/current")
  public ResponseEntity<EntityModel<TurnResponse>> getCurrentTurn(@PathVariable String id)
      throws BattleNotFoundException {
    TurnResponse body = new TurnResponse(battleQueryService.getCurrentTurn(idOfBattle(id)));
    log.info(body, "Current turn retrieved for battle: " + id);
    return ResponseEntity.ok(assembler.toCurrentTurnModel(body, id));
  }

  @GetMapping("/{id}/turns/total")
  public ResponseEntity<EntityModel<TurnResponse>> getNumOfTurns(@PathVariable String id)
      throws BattleNotFoundException {
    TurnResponse body = new TurnResponse(battleQueryService.getNumOfTurns(idOfBattle(id)));
    log.info(body, "Total turns retrieved for battle: " + id);
    return ResponseEntity.ok(assembler.toTotalTurnsModel(body, id));
  }

  @GetMapping("/{id}/status")
  public ResponseEntity<EntityModel<BattleStatusResponse>> getBattleStatus(@PathVariable String id)
      throws BattleNotFoundException {
    BattleOutcome status = battleQueryService.getBattleStatus(idOfBattle(id));
    boolean isOver = battleQueryService.isBattleOver(idOfBattle(id));
    boolean isWon = battleQueryService.isBattleWon(idOfBattle(id));
    BattleStatusResponse body = new BattleStatusResponse(status, isOver, isWon);
    log.info(body, "Battle status retrieved for battle: " + id);
    return ResponseEntity.ok(assembler.toStatusModel(body, id));
  }
}
