package habitquest.guild.infrastructure.inbound;

import common.ddd.Id;
import common.hexagonal.Adapter;
import habitquest.guild.application.exceptions.GuildNotFoundException;
import habitquest.guild.application.port.in.GuildCommandService;
import habitquest.guild.application.port.out.GuildLogger;
import habitquest.guild.domain.guild.GuildMember;
import habitquest.guild.domain.guild.Invite;
import habitquest.guild.domain.guild.UnauthorizedGuildOperationException;
import habitquest.guild.infrastructure.dto.GuildCommands.*;
import habitquest.guild.infrastructure.dto.GuildResponseAssembler;
import java.net.URI;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Adapter
@RestController
@RequestMapping("/api/v1/guilds")
public class GuildCommandController {

  private final GuildCommandService guildCommandService;
  private final GuildResponseAssembler assembler;
  private final GuildLogger log;

  public GuildCommandController(
      GuildCommandService guildCommandService, GuildResponseAssembler assembler, GuildLogger log) {
    this.guildCommandService = guildCommandService;
    this.assembler = assembler;
    this.log = log;
  }

  private Id<habitquest.guild.domain.guild.Guild> idOfGuild(String id) {
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
      @RequestBody CreateGuildCommand request) {
    String id =
        guildCommandService
            .createGuild(
                request.name(),
                idOfGuildMember(request.creatorAvatarId()),
                request.creatorNickname())
            .value();
    GuildCreatedResponse body = new GuildCreatedResponse(id);
    log.info(body, "Guild created");
    return ResponseEntity.created(URI.create("/api/v1/guilds/" + id))
        .body(assembler.toCreatedModel(body));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteGuild(@PathVariable String id) throws GuildNotFoundException {
    guildCommandService.deleteGuild(idOfGuild(id));
    log.info(new GuildCreatedResponse(id), "Guild deleted");
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{id}/invites")
  public ResponseEntity<Void> sendInvite(
      @PathVariable String id, @RequestBody SendInviteCommand request)
      throws GuildNotFoundException {
    try {
      guildCommandService.sendInvite(
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
      @RequestBody AcceptInviteCommand request)
      throws GuildNotFoundException {
    guildCommandService.acceptInvite(
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
      @RequestBody RemoveMemberCommand request)
      throws GuildNotFoundException {
    try {
      guildCommandService.removeMember(
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
    guildCommandService.leaveGuild(idOfGuild(id), idOfGuildMember(memberId));
    log.info(new GuildCreatedResponse(id), "Member left guild, memberId: " + memberId);
    return ResponseEntity.noContent().build();
  }

  @PatchMapping("/{id}/members/{memberId}/role")
  public ResponseEntity<Void> promoteMember(
      @PathVariable String id,
      @PathVariable String memberId,
      @RequestBody PromoteMemberCommand request)
      throws GuildNotFoundException {
    habitquest.guild.domain.guild.GuildRole newRole;
    try {
      newRole =
          habitquest.guild.domain.guild.GuildRole.valueOf(
              request.roleName().toUpperCase(java.util.Locale.getDefault()));
    } catch (IllegalArgumentException e) {
      log.warn(request, "Invalid role name: " + request.roleName());
      return ResponseEntity.badRequest().build();
    }
    try {
      guildCommandService.promoteMember(
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
}
