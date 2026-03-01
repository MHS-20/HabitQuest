package habitquest.guild.application;

import habitquest.guild.domain.events.BattleEvent;
import habitquest.guild.domain.events.BattleObserver;
import habitquest.guild.domain.events.GuildEvent;
import habitquest.guild.domain.events.GuildObserver;

public class GuildObserverImpl implements GuildObserver, BattleObserver {
  @Override
  public void notifyGuildEvent(GuildEvent event) {}

  @Override
  public void notifyBattleEvent(BattleEvent event) {
    // TODO Auto-generated method stub
  }
}
