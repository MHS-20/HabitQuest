package habitquest.guild.infrastructure.dto;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

import common.hexagonal.Adapter;
import habitquest.guild.infrastructure.dto.GuildCommands.*;
import habitquest.guild.infrastructure.dto.GuildQueries.*;
import habitquest.guild.infrastructure.inbound.GuildCommandController;
import habitquest.guild.infrastructure.inbound.GuildQueryController;
import java.util.List;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.stereotype.Component;

@Adapter
@Component
public class GuildResponseAssembler {

  // ── GuildResponse ─────────────────────────────────────────────────────────────
  public EntityModel<GuildResponse> toModel(GuildResponse body) {
    String id = body.id();
    return EntityModel.of(
        body,
        selfLink(id),
        linkTo(methodOn(GuildQueryController.class).getMembers(id)).withRel("members"),
        linkTo(methodOn(GuildQueryController.class).getGlobalRank(id)).withRel("rank"),
        linkTo(methodOn(GuildQueryController.class).getLeaderboard()).withRel("leaderboard"),
        linkTo(methodOn(GuildCommandController.class).deleteGuild(id)).withRel("delete"));
  }

  public CollectionModel<GuildResponse> toLeaderboardModel(List<GuildResponse> guilds) {
    return CollectionModel.of(
        guilds, linkTo(methodOn(GuildQueryController.class).getLeaderboard()).withSelfRel());
  }

  // ── GuildCreatedResponse ──────────────────────────────────────────────────────
  public EntityModel<GuildCreatedResponse> toCreatedModel(GuildCreatedResponse body) {
    String id = body.id();
    return EntityModel.of(
        body,
        selfLink(id),
        linkTo(methodOn(GuildQueryController.class).getGuild(id)).withRel("guild"),
        linkTo(methodOn(GuildQueryController.class).getMembers(id)).withRel("members"),
        linkTo(methodOn(GuildQueryController.class).getGlobalRank(id)).withRel("rank"));
  }

  // ── GuildMemberResponse ───────────────────────────────────────────────────────
  public CollectionModel<GuildMemberResponse> toMembersModel(
      List<GuildMemberResponse> members, String guildId) {
    return CollectionModel.of(
        members,
        linkTo(methodOn(GuildQueryController.class).getMembers(guildId)).withSelfRel(),
        selfLink(guildId));
  }

  // ── RankResponse ──────────────────────────────────────────────────────────────
  public EntityModel<RankResponse> toRankModel(RankResponse body, String guildId) {
    return EntityModel.of(
        body,
        linkTo(methodOn(GuildQueryController.class).getGlobalRank(guildId)).withSelfRel(),
        selfLink(guildId),
        linkTo(methodOn(GuildQueryController.class).getLeaderboard()).withRel("leaderboard"));
  }

  // ── helpers ───────────────────────────────────────────────────────────────────
  private Link selfLink(String id) {
    return Link.of("/api/v1/guilds/" + id).withSelfRel();
  }
}
