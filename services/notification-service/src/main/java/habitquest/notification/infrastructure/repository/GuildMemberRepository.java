package habitquest.notification.infrastructure.repository;

import java.util.Set;

public interface GuildMemberRepository {
  void addMember(String guildId, String avatarId);

  void removeMember(String guildId, String avatarId);

  void removeGuild(String guildId);

  Set<String> findMembersByGuildId(String guildId);
}
