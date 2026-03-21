package habitquest.notification.infrastructure.repository;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryGuildMemberRepository implements GuildMemberRepository {
  private final Map<String, Set<String>> store = new ConcurrentHashMap<>();

  @Override
  public void addMember(String guildId, String avatarId) {
    store.computeIfAbsent(guildId, id -> ConcurrentHashMap.newKeySet()).add(avatarId);
  }

  @Override
  public void removeMember(String guildId, String avatarId) {
    store.computeIfPresent(
        guildId,
        (id, members) -> {
          members.remove(avatarId);
          return members;
        });
  }

  @Override
  public void removeGuild(String guildId) {
    store.remove(guildId);
  }

  @Override
  public Set<String> findMembersByGuildId(String guildId) {
    return Collections.unmodifiableSet(store.getOrDefault(guildId, Collections.emptySet()));
  }
}
