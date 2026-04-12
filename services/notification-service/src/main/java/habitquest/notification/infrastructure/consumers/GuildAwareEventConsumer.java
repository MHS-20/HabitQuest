package habitquest.notification.infrastructure.consumers;

import habitquest.notification.infrastructure.notification.NotificationService;
import habitquest.notification.infrastructure.repository.GuildMemberRepository;
import habitquest.notification.infrastructure.repository.UserEmailRepository;
import java.util.Set;

public class GuildAwareEventConsumer extends AvatarAwareEventConsumer {

  private final GuildMemberRepository guildMemberRepository;

  protected GuildAwareEventConsumer(
      UserEmailRepository userEmailRepository,
      GuildMemberRepository guildMemberRepository,
      NotificationService notificationService) {
    super(userEmailRepository, notificationService);
    this.guildMemberRepository = guildMemberRepository;
  }

  protected void sendToGuild(String guildId, String subject, String body) {
    Set<String> members = guildMemberRepository.findMembersByGuildId(guildId);
    if (members.isEmpty()) {
      logger().warn("Nessun membro trovato per guildId={}, notifica non inviata", guildId);
      return;
    }
    members.forEach(avatarId -> sendToAvatar(avatarId, subject, body));
  }

  protected void addMember(String guildId, String memberId) {
    guildMemberRepository.addMember(guildId, memberId);
  }

  protected void removeMember(String guildId, String memberId) {
    guildMemberRepository.removeMember(guildId, memberId);
  }

  protected void removeGuild(String guildId) {
    guildMemberRepository.removeGuild(guildId);
  }
}
