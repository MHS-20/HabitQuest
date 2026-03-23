package habitquest.guild.application;

import habitquest.guild.domain.events.battleEvents.*;
import org.springframework.stereotype.Component;

@Component
public class BattleObserverImpl implements BattleObserver {

  private final BattleNotifier battleNotifier;
  private final GuildLogger log;

  public BattleObserverImpl(BattleNotifier battleNotifier, GuildLogger log) {
    this.battleNotifier = battleNotifier;
    this.log = log;
  }

  @Override
  public void notifyBattleEvent(BattleEvent event) {
    switch (event) {
      case BattleStarted e -> handleBattleStarted(e);
      case BattleWon e -> handleBattleWon(e);
      case BattleLost e -> handleBattleLost(e);
      default ->
          throw new IllegalArgumentException("Unknown event type: " + event.getClass().getName());
    }
  }

  public void handleBattleStarted(BattleStarted e) {
    log.info(e, "Handling BattleStarted event");
    battleNotifier.notifyBattleStarted(e);
  }

  public void handleBattleWon(BattleWon e) {
    log.info(e, "Handling BattleWon event");
    battleNotifier.notifyBattleWon(e);
  }

  public void handleBattleLost(BattleLost e) {
    log.info(e, "Handling BattleLost event");
    battleNotifier.notifyBattleLost(e);
  }
}
