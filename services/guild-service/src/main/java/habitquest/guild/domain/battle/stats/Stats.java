package habitquest.guild.domain.battle.stats;

import common.ddd.ValueObject;

public record Stats(String id, Health health, Strength strength, Defense defense)
    implements ValueObject {}
