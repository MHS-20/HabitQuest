package habitquest.guild.domain.battle.boss;

import habitquest.guild.domain.battle.Experience;
import habitquest.guild.domain.battle.Money;
import habitquest.guild.domain.battle.Penalty;
import habitquest.guild.domain.battle.stats.Stats;

public record Minotaur(
    String name, Stats stats, Money moneyReward, Penalty penalty, Experience experienceReward)
    implements BossEnemy {}
