package habitquest.guild.infrastructure.dto;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

import common.hexagonal.Adapter;
import habitquest.guild.infrastructure.dto.BattleCommands.*;
import habitquest.guild.infrastructure.dto.BattleQueries.*;
import habitquest.guild.infrastructure.inbound.BattleCommandController;
import habitquest.guild.infrastructure.inbound.BattleQueryController;
import java.util.List;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.stereotype.Component;

@Adapter
@Component
public class BattleResponseAssembler {
  public static final String CURRENT_TURN = "currentTurn";

  // ── BattleResponse ────────────────────────────────────────────────────────────

  public EntityModel<BattleResponse> toModel(BattleResponse body) {
    String id = body.id();
    return EntityModel.of(
        body,
        selfLink(id),
        linkTo(methodOn(BattleQueryController.class).getBoss(id)).withRel("boss"),
        linkTo(methodOn(BattleQueryController.class).getBossHealth(id)).withRel("bossHealth"),
        linkTo(methodOn(BattleQueryController.class).getBattleStatus(id)).withRel("status"),
        linkTo(methodOn(BattleQueryController.class).getCurrentTurn(id)).withRel(CURRENT_TURN),
        linkTo(methodOn(BattleQueryController.class).getNumOfTurns(id)).withRel("numOfTurns"),
        linkTo(methodOn(BattleCommandController.class).dealDamage(id, null)).withRel("dealDamage"));
  }

  public EntityModel<BattleResponse> toModelForGuild(BattleResponse body, String guildId) {
    String id = body.id();
    return EntityModel.of(
        body,
        linkTo(methodOn(BattleQueryController.class).getBattleByGuild(guildId)).withSelfRel(),
        selfLink(id),
        linkTo(methodOn(BattleQueryController.class).getBattleStatus(id)).withRel("status"),
        linkTo(methodOn(BattleCommandController.class).dealDamage(id, null)).withRel("dealDamage"));
  }

  // ── BattleCreatedResponse ─────────────────────────────────────────────────────

  public EntityModel<BattleCreatedResponse> toCreatedModel(BattleCreatedResponse body) {
    String id = body.id();
    return EntityModel.of(
        body,
        selfLink(id),
        linkTo(methodOn(BattleQueryController.class).getBattle(id)).withRel("battle"),
        linkTo(methodOn(BattleQueryController.class).getBoss(id)).withRel("boss"),
        linkTo(methodOn(BattleQueryController.class).getBattleStatus(id)).withRel("status"),
        linkTo(methodOn(BattleQueryController.class).getCurrentTurn(id)).withRel(CURRENT_TURN));
  }

  // ── BossResponse ──────────────────────────────────────────────────────────────

  public EntityModel<BossResponse> toBossModel(BossResponse body, String battleId) {
    return EntityModel.of(
        body,
        linkTo(methodOn(BattleQueryController.class).getBoss(battleId)).withSelfRel(),
        selfLink(battleId),
        linkTo(methodOn(BattleQueryController.class).getBossHealth(battleId))
            .withRel("bossHealth"));
  }

  public CollectionModel<BossResponse> toAllBossesModel(List<BossResponse> bosses) {
    return CollectionModel.of(
        bosses, linkTo(methodOn(BattleQueryController.class).getAllBosses()).withSelfRel());
  }

  // ── BossHealthResponse ────────────────────────────────────────────────────────

  public EntityModel<BossHealthResponse> toBossHealthModel(
      BossHealthResponse body, String battleId) {
    return EntityModel.of(
        body,
        linkTo(methodOn(BattleQueryController.class).getBossHealth(battleId)).withSelfRel(),
        selfLink(battleId),
        linkTo(methodOn(BattleCommandController.class).dealDamage(battleId, null))
            .withRel("dealDamage"));
  }

  // ── TurnResponse ──────────────────────────────────────────────────────────────

  public EntityModel<TurnResponse> toCurrentTurnModel(TurnResponse body, String battleId) {
    return EntityModel.of(
        body,
        linkTo(methodOn(BattleQueryController.class).getCurrentTurn(battleId)).withSelfRel(),
        selfLink(battleId),
        linkTo(methodOn(BattleQueryController.class).getNumOfTurns(battleId))
            .withRel("numOfTurns"));
  }

  public EntityModel<TurnResponse> toTotalTurnsModel(TurnResponse body, String battleId) {
    return EntityModel.of(
        body,
        linkTo(methodOn(BattleQueryController.class).getNumOfTurns(battleId)).withSelfRel(),
        selfLink(battleId),
        linkTo(methodOn(BattleQueryController.class).getCurrentTurn(battleId))
            .withRel(CURRENT_TURN));
  }

  // ── InProgressResponse ────────────────────────────────────────────────────────

  public EntityModel<InProgressResponse> toInProgressModel(
      InProgressResponse body, String guildId) {
    return EntityModel.of(
        body,
        linkTo(methodOn(BattleQueryController.class).hasBattleInProgress(guildId)).withSelfRel(),
        linkTo(methodOn(BattleQueryController.class).getBattleByGuild(guildId)).withRel("battle"));
  }

  // ── BattleStatusResponse ──────────────────────────────────────────────────────

  public EntityModel<BattleStatusResponse> toStatusModel(
      BattleStatusResponse body, String battleId) {
    return EntityModel.of(
        body,
        linkTo(methodOn(BattleQueryController.class).getBattleStatus(battleId)).withSelfRel(),
        selfLink(battleId),
        linkTo(methodOn(BattleQueryController.class).getBossHealth(battleId)).withRel("bossHealth"),
        linkTo(methodOn(BattleQueryController.class).getCurrentTurn(battleId))
            .withRel(CURRENT_TURN));
  }

  // ── helpers ───────────────────────────────────────────────────────────────────

  private Link selfLink(String id) {
    return Link.of("/api/v1/battles/" + id).withSelfRel();
  }
}
