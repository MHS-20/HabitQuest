package habitquest.marketplace.domain.items;

public sealed interface Equipment extends Item permits Weapon, Armor {}
