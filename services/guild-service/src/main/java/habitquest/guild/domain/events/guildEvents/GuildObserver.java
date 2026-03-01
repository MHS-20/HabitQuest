package habitquest.guild.domain.events.guildEvents;

public interface GuildObserver {
  void notifyGuildEvent(GuildEvent event);
}
