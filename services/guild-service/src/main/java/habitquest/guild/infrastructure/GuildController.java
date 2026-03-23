package habitquest.guild.infrastructure;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

import common.ddd.Id;
import common.hexagonal.Adapter;
import habitquest.guild.application.GuildLogger;
import habitquest.guild.application.GuildNotFoundException;
import habitquest.guild.application.GuildService;
import habitquest.guild.domain.guild.*;
import habitquest.guild.infrastructure.dto.GuildMemberResponse;
import habitquest.guild.infrastructure.dto.GuildResponse;
import java.net.URI;
import java.util.List;
import java.util.Locale;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Adapter
@RestController
@RequestMapping("/api/v1/guilds")
public class GuildController {

  private final GuildService guildService;
  private final GuildLogger log;

  public GuildController(GuildService guildService, GuildLogger log) {
    this.guildService = guildService;
    this.log = log;
  }

  private Id<Guild> idOfGuild(String id) {
    return new Id<>(id);
  }

  private Id<GuildMember> idOfGuildMember(String id) {
    return new Id<>(id);
  }

  private Id<Invite> idOfInvite(String id) {
    return new Id<>(id);
  }

  @PostMapping
  public ResponseEntity<EntityModel<GuildCreatedResponse>> createGuild(
      @RequestBody CreateGuildRequest request) {
    String id =
        guildService
            .createGuild(
                request.name(),
                idOfGuildMember(request.creatorAvatarId()),
                request.creatorNickname())
            .value();
    GuildCreatedResponse body = new GuildCreatedResponse(id);
    log.info(body, "Guild created");
    EntityModel<GuildCreatedResponse> model =
        EntityModel.of(
            body,
            selfLink(id),
            linkTo(methodOn(GuildController.class).getGuild(id)).withRel("guild"),
            linkTo(methodOn(GuildController.class).getMembers(id)).withRel("members"),
            linkTo(methodOn(GuildController.class).getGlobalRank(id)).withRel("rank"));
    return ResponseEntity.created(URI.create("/api/v1/guilds/" + id)).body(model);
  }

  @GetMapping("/{id}")
  public ResponseEntity<EntityModel<GuildResponse>> getGuild(@PathVariable String id)
      throws GuildNotFoundException {
    GuildResponse body = GuildResponse.from(guildService.getGuild(idOfGuild(id)));
    log.info(body, "Guild retrieved");
    EntityModel<GuildResponse> model =
        EntityModel.of(
            body,
            selfLink(id),
            linkTo(methodOn(GuildController.class).getMembers(id)).withRel("members"),
            linkTo(methodOn(GuildController.class).getGlobalRank(id)).withRel("rank"),
            linkTo(methodOn(GuildController.class).getLeaderboard()).withRel("leaderboard"),
            linkTo(methodOn(GuildController.class).deleteGuild(id)).withRel("delete"));
    return ResponseEntity.ok(model);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteGuild(@PathVariable String id) throws GuildNotFoundException {
    guildService.deleteGuild(idOfGuild(id));
    log.info(new GuildCreatedResponse(id), "Guild deleted");
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/{id}/members")
  public ResponseEntity<CollectionModel<GuildMemberResponse>> getMembers(@PathVariable String id)
      throws GuildNotFoundException {
    List<GuildMemberResponse> members =
        guildService.getMembers(idOfGuild(id)).stream().map(GuildMemberResponse::from).toList();
    log.info(new MembersCountResponse(id, members.size()), "Guild members retrieved");
    CollectionModel<GuildMemberResponse> model =
        CollectionModel.of(
            members,
            linkTo(methodOn(GuildController.class).getMembers(id)).withSelfRel(),
            selfLink(id));
    return ResponseEntity.ok(model);
  }

  @PostMapping("/{id}/invites")
  public ResponseEntity<Void> sendInvite(
      @PathVariable String id, @RequestBody SendInviteRequest request)
      throws GuildNotFoundException {
    try {
      guildService.sendInvite(
          idOfGuild(id),
          idOfGuildMember(request.requestorId()),
          idOfGuildMember(request.targetAvatarId()));
      log.info(request, "Invite sent");
      return ResponseEntity.noContent().build();
    } catch (UnauthorizedGuildOperationException e) {
      log.warn(request, "Unauthorized invite attempt");
      return ResponseEntity.status(403).build();
    }
  }

  @PostMapping("/{id}/invites/{inviteId}/accept")
  public ResponseEntity<Void> acceptInvite(
      @PathVariable String id,
      @PathVariable String inviteId,
      @RequestBody AcceptInviteRequest request)
      throws GuildNotFoundException {
    guildService.acceptInvite(
        idOfGuild(id),
        idOfInvite(inviteId),
        idOfGuildMember(request.avatarId()),
        request.nickname());
    log.info(request, "Invite accepted for guild: " + id);
    return ResponseEntity.noContent().build();
  }

  @DeleteMapping("/{id}/members/{memberId}")
  public ResponseEntity<Void> removeMember(
      @PathVariable String id,
      @PathVariable String memberId,
      @RequestBody RemoveMemberRequest request)
      throws GuildNotFoundException {
    try {
      guildService.removeMember(
          idOfGuild(id), idOfGuildMember(request.requestorId()), idOfGuildMember(memberId));
      log.info(request, "Member removed from guild: " + id);
      return ResponseEntity.noContent().build();
    } catch (UnauthorizedGuildOperationException e) {
      log.warn(request, "Unauthorized remove member attempt");
      return ResponseEntity.status(403).build();
    }
  }

  @PostMapping("/{id}/members/{memberId}/leave")
  public ResponseEntity<Void> leaveGuild(@PathVariable String id, @PathVariable String memberId)
      throws GuildNotFoundException {
    guildService.leaveGuild(idOfGuild(id), idOfGuildMember(memberId));
    log.info(new GuildCreatedResponse(id), "Member left guild, memberId: " + memberId);
    return ResponseEntity.noContent().build();
  }

  @PatchMapping("/{id}/members/{memberId}/role")
  public ResponseEntity<Void> promoteMember(
      @PathVariable String id,
      @PathVariable String memberId,
      @RequestBody PromoteMemberRequest request)
      throws GuildNotFoundException {
    GuildRole newRole;
    try {
      newRole = GuildRole.valueOf(request.roleName().toUpperCase(Locale.getDefault()));
    } catch (IllegalArgumentException e) {
      log.warn(request, "Invalid role name: " + request.roleName());
      return ResponseEntity.badRequest().build();
    }
    try {
      guildService.promoteMember(
          idOfGuild(id),
          idOfGuildMember(request.requestorId()),
          idOfGuildMember(memberId),
          newRole);
      log.info(request, "Member promoted in guild: " + id);
      return ResponseEntity.noContent().build();
    } catch (UnauthorizedGuildOperationException e) {
      log.warn(request, "Unauthorized promote member attempt");
      return ResponseEntity.status(403).build();
    }
  }

  @GetMapping("/{id}/rank")
  public ResponseEntity<EntityModel<RankResponse>> getGlobalRank(@PathVariable String id)
      throws GuildNotFoundException {
    Integer rank = guildService.getGlobalRank(idOfGuild(id));
    RankResponse body = new RankResponse(rank);
    log.info(body, "Global rank retrieved for guild: " + id);
    EntityModel<RankResponse> model =
        EntityModel.of(
            body,
            linkTo(methodOn(GuildController.class).getGlobalRank(id)).withSelfRel(),
            selfLink(id),
            linkTo(methodOn(GuildController.class).getLeaderboard()).withRel("leaderboard"));
    return ResponseEntity.ok(model);
  }

  @GetMapping("/leaderboard")
  public ResponseEntity<CollectionModel<GuildResponse>> getLeaderboard() {
    List<GuildResponse> leaderboard =
        guildService.getGuildLeaderboard().stream().map(GuildResponse::from).toList();
    log.info(new LeaderboardCountResponse(leaderboard.size()), "Leaderboard retrieved");
    CollectionModel<GuildResponse> model =
        CollectionModel.of(
            leaderboard, linkTo(methodOn(GuildController.class).getLeaderboard()).withSelfRel());
    return ResponseEntity.ok(model);
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
  public ResponseEntity<Void> handleUnauthorized(UnauthorizedGuildOperationException ex) {
    log.warn(ex, "Unauthorized guild operation");
    return ResponseEntity.status(403).build();
  }

  private Link selfLink(String id) {
    return Link.of("/api/v1/guilds/" + id).withSelfRel();
  }

  public record CreateGuildRequest(String name, String creatorAvatarId, String creatorNickname) {}

  public record RemoveMemberRequest(String requestorId) {}

  public record SendInviteRequest(String requestorId, String targetAvatarId) {}

  public record AcceptInviteRequest(String avatarId, String nickname) {}

  public record AddMemberRequest(String avatarId, String nickname, String roleName) {}

  public record PromoteMemberRequest(String roleName, String requestorId) {}

  public record GuildCreatedResponse(String id) {}

  public record RankResponse(Integer globalRank) {}

  public record ErrorResponse(String message) {}

  private record MembersCountResponse(String guildId, int count) {}

  private record LeaderboardCountResponse(int count) {}
}
