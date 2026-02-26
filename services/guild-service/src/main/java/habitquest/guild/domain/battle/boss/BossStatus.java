package habitquest.guild.domain.battle.boss;

import common.ddd.ValueObject;
import habitquest.guild.domain.battle.Health;

public record BossStatus(Health remainingHealth) implements ValueObject {}
