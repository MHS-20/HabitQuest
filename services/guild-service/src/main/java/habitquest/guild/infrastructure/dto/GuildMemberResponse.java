package habitquest.guild.infrastructure.dto;

import habitquest.guild.domain.guild.GuildMember;

public record GuildMemberResponse(String avatarId, String nickname, String role) {

  public static GuildMemberResponse from(GuildMember member) {
    return new GuildMemberResponse(member.getId(), member.getNickname(), member.getRole().name());
  }
}
