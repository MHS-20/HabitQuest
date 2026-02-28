package habitquest.guild.application;

import common.ddd.Repository;
import common.hexagonal.OutBoundPort;
import habitquest.guild.domain.guild.Guild;

@OutBoundPort
public interface GuildRepository extends Repository {
    Guild save(Guild guild);
    Guild findById(String id) throws GuildNotFoundException;
    void deleteById(String id) throws GuildNotFoundException;
}
