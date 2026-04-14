package habitquest.avatar.domain.items;

public sealed interface Equipment extends Item permits Weapon, Armor {}
