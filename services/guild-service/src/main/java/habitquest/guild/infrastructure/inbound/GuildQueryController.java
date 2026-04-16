package habitquest.guild.infrastructure.inbound;

import common.ddd.Id;
import common.hexagonal.Adapter;
import habitquest.guild.application.exceptions.GuildNotFoundException;
import habitquest.guild.application.port.in.GuildQueryService;
import habitquest.guild.application.port.out.GuildLogger;
import habitquest.guild.infrastructure.dto.GuildQueries.*;
import habitquest.guild.infrastructure.dto.GuildResponseAssembler;
import java.util.List;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Adapter
@RestController
@RequestMapping("/api/v1/guilds")
public class GuildQueryController {

  private final GuildQueryService guildQueryService;
  private final GuildResponseAssembler assembler;
  private final GuildLogger log;

  public GuildQueryController(
      GuildQueryService guildQueryService, GuildResponseAssembler assembler, GuildLogger log) {
    this.guildQueryService = guildQueryService;
    this.assembler = assembler;
    this.log = log;
  }

  private Id<habitquest.guild.domain.guild.Guild> idOfGuild(String id) {
    return new Id<>(id);
  }

  @GetMapping("/{id}")
  public ResponseEntity<EntityModel<GuildResponse>> getGuild(@PathVariable String id)
      throws GuildNotFoundException {
    GuildResponse body = GuildResponse.from(guildQueryService.getGuild(idOfGuild(id)));
    log.info(body, "Guild retrieved");
    return ResponseEntity.ok(assembler.toModel(body));
  }

  @GetMapping("/{id}/members")
  public ResponseEntity<CollectionModel<GuildMemberResponse>> getMembers(@PathVariable String id)
      throws GuildNotFoundException {
    List<GuildMemberResponse> members =
        guildQueryService.getMembers(idOfGuild(id)).stream()
            .map(GuildMemberResponse::from)
            .toList();
    log.info(new MembersCountResponse(id, members.size()), "Guild members retrieved");
    return ResponseEntity.ok(assembler.toMembersModel(members, id));
  }

  @GetMapping("/{id}/rank")
  public ResponseEntity<EntityModel<RankResponse>> getGlobalRank(@PathVariable String id)
      throws GuildNotFoundException {
    RankResponse body = new RankResponse(guildQueryService.getGlobalRank(idOfGuild(id)));
    log.info(body, "Global rank retrieved for guild: " + id);
    return ResponseEntity.ok(assembler.toRankModel(body, id));
  }

  @GetMapping("/leaderboard")
  public ResponseEntity<CollectionModel<GuildResponse>> getLeaderboard() {
    List<GuildResponse> leaderboard =
        guildQueryService.getGuildLeaderboard().stream().map(GuildResponse::from).toList();
    log.info(new LeaderboardCountResponse(leaderboard.size()), "Leaderboard retrieved");
    return ResponseEntity.ok(assembler.toLeaderboardModel(leaderboard));
  }
}
