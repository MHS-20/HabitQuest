package habitquest.guild.infrastructure;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

import common.hexagonal.Adapter;
import habitquest.guild.application.GuildNotFoundException;
import habitquest.guild.application.GuildService;
import habitquest.guild.domain.guild.GuildMember;
import habitquest.guild.domain.guild.GuildRole;
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

  public GuildController(GuildService guildService) {
    this.guildService = guildService;
  }

  // Guild CRUD
  @PostMapping
  public ResponseEntity<EntityModel<GuildCreatedResponse>> createGuild(
      @RequestBody CreateGuildRequest request) {

    String id =
        guildService.createGuild(
            request.name(), request.creatorAvatarId(), request.creatorNickname());
    GuildCreatedResponse body = new GuildCreatedResponse(id);

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

    GuildResponse body = GuildResponse.from(guildService.getGuild(id));

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

    guildService.deleteGuild(id);
    return ResponseEntity.noContent().build();
  }

  // Members

  @GetMapping("/{id}/members")
  public ResponseEntity<CollectionModel<GuildMemberResponse>> getMembers(@PathVariable String id)
      throws GuildNotFoundException {

    List<GuildMemberResponse> members =
        guildService.getMembers(id).stream().map(GuildMemberResponse::from).toList();

    CollectionModel<GuildMemberResponse> model =
        CollectionModel.of(
            members,
            linkTo(methodOn(GuildController.class).getMembers(id)).withSelfRel(),
            selfLink(id),
            linkTo(methodOn(GuildController.class).addMember(id, null)).withRel("addMember"));

    return ResponseEntity.ok(model);
  }

  @PostMapping("/{id}/members")
  public ResponseEntity<Void> addMember(
      @PathVariable String id, @RequestBody AddMemberRequest request)
      throws GuildNotFoundException {

    GuildRole role;
    try {
      role = GuildRole.valueOf(request.roleName().toUpperCase(Locale.getDefault()));
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().body(null);
    }

    GuildMember member = new GuildMember(request.avatarId(), request.nickname(), role);
    guildService.addMember(id, member);
    return ResponseEntity.noContent().build();
  }

  @DeleteMapping("/{id}/members/{memberId}")
  public ResponseEntity<Void> removeMember(
      @PathVariable String id,
      @PathVariable String memberId,
      @RequestBody RemoveMemberRequest request)
      throws GuildNotFoundException {
    if (!guildService.isLeader(id, request.requestorId())) {
      return ResponseEntity.status(403).build();
    }
    guildService.removeMember(id, memberId);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{id}/members/{memberId}/leave")
  public ResponseEntity<Void> leaveGuild(@PathVariable String id, @PathVariable String memberId)
      throws GuildNotFoundException {

    guildService.leaveGuild(id, memberId);
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
      return ResponseEntity.badRequest().build();
    }

    if (!guildService.isLeader(id, request.requestorId())) {
      return ResponseEntity.status(403).build();
    }

    guildService.promoteMember(id, memberId, newRole);
    return ResponseEntity.noContent().build();
  }

  // Ranking & Leaderboard
  @GetMapping("/{id}/rank")
  public ResponseEntity<EntityModel<RankResponse>> getGlobalRank(@PathVariable String id)
      throws GuildNotFoundException {

    Integer rank = guildService.getGlobalRank(id);
    RankResponse body = new RankResponse(rank);

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
    CollectionModel<GuildResponse> model =
        CollectionModel.of(
            leaderboard, linkTo(methodOn(GuildController.class).getLeaderboard()).withSelfRel());
    return ResponseEntity.ok(model);
  }

  // Exception handling
  @ExceptionHandler(GuildNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleGuildNotFound(GuildNotFoundException ex) {
    return ResponseEntity.notFound().build();
  }

  @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
  public ResponseEntity<ErrorResponse> handleDomainError(RuntimeException ex) {
    return ResponseEntity.badRequest().body(new ErrorResponse(ex.getMessage()));
  }

  // HATEOAS helpers

  private Link selfLink(String id) {
    return Link.of("/api/v1/guilds/" + id).withSelfRel();
  }

  // Request / Response records
  public record CreateGuildRequest(String name, String creatorAvatarId, String creatorNickname) {}

  public record RemoveMemberRequest(String requestorId) {}

  public record AddMemberRequest(String avatarId, String nickname, String roleName) {}

  public record PromoteMemberRequest(String roleName, String requestorId) {}

  public record GuildCreatedResponse(String id) {}

  public record RankResponse(Integer globalRank) {}

  public record ErrorResponse(String message) {}
}
