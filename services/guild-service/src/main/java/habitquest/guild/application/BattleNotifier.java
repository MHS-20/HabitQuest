package habitquest.guild.application;

import habitquest.guild.domain.events.battleEvents.BattleLost;
import habitquest.guild.domain.events.battleEvents.BattleStarted;
import habitquest.guild.domain.events.battleEvents.BattleWon;

public interface BattleNotifier {

  void notifyBattleStarted(BattleStarted e);

  void notifyBattleWon(BattleWon e);

  void notifyBattleLost(BattleLost e);
}
