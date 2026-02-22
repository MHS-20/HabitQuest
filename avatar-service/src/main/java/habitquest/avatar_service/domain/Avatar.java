package habitquest.avatar_service.domain;

import common.ddd.Entity;

public class Avatar implements Entity<String> {
    private Money money;
    private Inventory inventory;
    private EquippedItems equippedItems;
    private Experience experience;
    private Level level;
    private Health health;
    private Mana mana;

    @Override
    public String getId() {
        return "";
    }
}
