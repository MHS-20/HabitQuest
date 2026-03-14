package habitquest.guild.infrastructure.dto;

import habitquest.guild.domain.guild.Guild;
import java.util.List;

public record GuildResponse(
    String id, String name, Integer globalRank, List<GuildMemberResponse> members) {

  public static GuildResponse from(Guild guild) {
    return new GuildResponse(
        guild.getId(),
        guild.getName(),
        guild.getGlobalRank(),
        guild.getMembers().stream().map(GuildMemberResponse::from).toList());
  }
}
