package habitquest.guild.domain.battle.boss;

import habitquest.guild.domain.battle.stats.Stats;

public record Minotaur(String name, Stats stats) implements BossEnemy {}
