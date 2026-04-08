package habitquest.avatar.domain.items;

public sealed interface Potion extends Item permits HealthPotion, ManaPotion {}
