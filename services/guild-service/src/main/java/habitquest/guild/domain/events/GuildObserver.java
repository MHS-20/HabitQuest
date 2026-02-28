package habitquest.guild.domain.events;

public interface GuildObserver {
    void notifyGuildEvent(GuildEvent event);
}
