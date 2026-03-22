package habitquest.guild.infrastructure;

import habitquest.guild.domain.factory.BattleFactory;
import habitquest.guild.domain.factory.GuildFactory;
import habitquest.guild.domain.factory.InviteFactory;
import habitquest.guild.domain.factory.UUIDGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GuildConfig {

  @Bean
  public GuildFactory guildFactory() {
    return new GuildFactory(new UUIDGenerator());
  }

  @Bean
  public BattleFactory battleFactory() {
    return new BattleFactory(new UUIDGenerator());
  }

  @Bean
  public InviteFactory inviteFactory() {
    return new InviteFactory(new UUIDGenerator());
  }
}
