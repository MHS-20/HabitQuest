package habitquest.guild.infrastructure.inbound;

import common.ddd.Id;
import common.hexagonal.Adapter;
import habitquest.guild.application.exceptions.BattleNotFoundException;
import habitquest.guild.application.exceptions.GuildNotFoundException;
import habitquest.guild.application.port.in.BattleCommandService;
import habitquest.guild.application.port.in.BattleQueryService;
import habitquest.guild.application.port.in.GuildQueryService;
import habitquest.guild.application.port.out.GuildLogger;
import habitquest.guild.domain.battle.BattleOutcome;
import habitquest.guild.domain.battle.boss.BossType;
import habitquest.guild.domain.guild.GuildMember;
import habitquest.guild.infrastructure.dto.BattleCommands.*;
import habitquest.guild.infrastructure.dto.BattleResponseAssembler;
import java.util.Locale;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Adapter
@RestController
@RequestMapping("/api/v1/battles")
public class BattleCommandController {

  private final BattleCommandService battleCommandService;
  private final BattleQueryService battleQueryService;
  private final GuildQueryService guildQueryService;
  private final BattleResponseAssembler assembler;
  private final GuildLogger log;

  public BattleCommandController(
      BattleCommandService battleCommandService,
      BattleQueryService battleQueryService,
      GuildQueryService guildQueryService,
      BattleResponseAssembler assembler,
      GuildLogger log) {
    this.battleCommandService = battleCommandService;
    this.battleQueryService = battleQueryService;
    this.guildQueryService = guildQueryService;
    this.assembler = assembler;
    this.log = log;
  }

  private Id<habitquest.guild.domain.battle.Battle> idOfBattle(String id) {
    return new Id<>(id);
  }

  private Id<habitquest.guild.domain.guild.Guild> idOfGuild(String id) {
    return new Id<>(id);
  }

  private Id<GuildMember> idOfGuildMember(String id) {
    return new Id<>(id);
  }

  @PostMapping
  public ResponseEntity<EntityModel<BattleCreatedResponse>> createBattle(
      @RequestBody CreateBattleCommand request) {
    BossType bossType;
    try {
      bossType = BossType.valueOf(request.bossType().toUpperCase(Locale.getDefault()));
    } catch (IllegalArgumentException e) {
      log.warn(request, "Invalid boss type: " + request.bossType());
      return ResponseEntity.badRequest().build();
    }
    if (!guildQueryService.isLeader(
        idOfGuild(request.guildId()), idOfGuildMember(request.requesterId()))) {
      log.warn(request, "Unauthorized battle creation attempt");
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
    var guildMembers = guildQueryService.getMembers(idOfGuild(request.guildId()));
    String id =
        battleCommandService
            .createBattle(
                idOfGuild(request.guildId()),
                bossType,
                guildMembers.size(),
                guildMembers.stream().map(GuildMember::getId).toList())
            .value();
    var body = new BattleCreatedResponse(id);
    log.info(body, "Battle created");
    return ResponseEntity.created(java.net.URI.create("/api/v1/battles/" + id))
        .body(assembler.toCreatedModel(body));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteBattle(
      @PathVariable String id, @RequestBody DeleteBattleCommand request) {
    if (!guildQueryService.isLeader(
        idOfGuild(request.guildId()), idOfGuildMember(request.requesterId()))) {
      log.warn(request, "Unauthorized battle deletion attempt");
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
    battleCommandService.deleteBattle(idOfBattle(id));
    log.info(new BattleCreatedResponse(id), "Battle deleted");
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{id}/damage")
  public ResponseEntity<Void> dealDamage(
      @PathVariable String id, @RequestBody TakeDamageCommand request)
      throws BattleNotFoundException, GuildNotFoundException {
    if (request.attackerAvatarId() == null) {
      log.warn(request, "Damage request missing attackerAvatarId");
      return ResponseEntity.badRequest().build();
    }
    if (!battleQueryService.isAttackerTurn(
        idOfBattle(id), idOfGuildMember(request.attackerAvatarId()))) {
      log.warn(request, "Not attacker's turn in battle: " + id);
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
    log.info(request, "Dealing damage in battle: " + id);
    BattleOutcome outcome =
        battleCommandService.processDamage(
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
}
