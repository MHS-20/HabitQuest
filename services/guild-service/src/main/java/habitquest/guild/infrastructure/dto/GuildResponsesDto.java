package habitquest.guild.infrastructure.dto;

import habitquest.guild.domain.guild.Guild;
import habitquest.guild.domain.guild.GuildMember;
import java.util.List;

public class GuildResponsesDto {
  public record GuildCreatedResponse(String id) {}

  public record RankResponse(Integer globalRank) {}

  public record ErrorResponse(String message) {}

  public record MembersCountResponse(String guildId, int count) {}

  public record LeaderboardCountResponse(int count) {}

  public record GuildMemberResponse(String avatarId, String nickname, String role) {
    public static GuildMemberResponse from(GuildMember member) {
      return new GuildMemberResponse(
          member.getId().value(), member.getNickname(), member.getRole().name());
    }
  }

  public record GuildResponse(
      String id, String name, Integer globalRank, List<GuildMemberResponse> members) {

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
