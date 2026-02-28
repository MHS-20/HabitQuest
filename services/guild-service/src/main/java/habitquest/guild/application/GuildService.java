package habitquest.guild.application;

import common.hexagonal.InBoundPort;
import habitquest.guild.domain.guild.Guild;
import habitquest.guild.domain.guild.GuildMember;
import habitquest.guild.domain.guild.GuildRole;

import java.util.List;

@InBoundPort
public interface GuildService {
        String createGuild(String name);
        Guild getGuild(String guildId) throws GuildNotFoundException;
        void updateGuild(String guildId, Guild request) throws GuildNotFoundException;
        void deleteGuild(String guildId) throws GuildNotFoundException;

        List<GuildMember> getMembers(String guildId);
        Integer getGlobalRank(String guildId);
        void addMembers(String guildId, List<GuildMember> members);
        void removeMembers(String guildId, List<String> memberIds);
        void promoteMember(String guildId, String memberId, GuildRole newRole);
}
