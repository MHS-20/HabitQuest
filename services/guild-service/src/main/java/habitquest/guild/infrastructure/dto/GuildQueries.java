package habitquest.guild.infrastructure.dto;

import common.cqrs.QueryResponse;
import habitquest.guild.domain.guild.Guild;
import habitquest.guild.domain.guild.GuildMember;
import java.util.List;

public class GuildQueries {
  // Query responses
  public record RankResponse(Integer globalRank) implements QueryResponse {}

  public record MembersCountResponse(String guildId, int count) implements QueryResponse {}

  public record LeaderboardCountResponse(int count) implements QueryResponse {}

  public record GuildMemberResponse(String avatarId, String nickname, String role)
      implements QueryResponse {
    public static GuildMemberResponse from(GuildMember member) {
      return new GuildMemberResponse(
          member.getId().value(), member.getNickname(), member.getRole().name());
    }
  }

  public record GuildResponse(
      String id, String name, Integer globalRank, List<GuildMemberResponse> members)
      implements QueryResponse {
    public GuildResponse {
      members = List.copyOf(members);
    }

    public static GuildResponse from(Guild guild) {
      return new GuildResponse(
          guild.getId().value(),
          guild.getName(),
          guild.getGlobalRank(),
          guild.getMembers().stream().map(GuildMemberResponse::from).toList());
    }
  }
}
