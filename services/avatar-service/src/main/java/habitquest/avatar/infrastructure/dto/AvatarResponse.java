package habitquest.avatar.infrastructure.dto;

import habitquest.avatar.infrastructure.AvatarController.*;
import java.util.List;

public record AvatarResponse(
    String id,
    String name,
    MoneyResponse money,
    LevelResponse level,
    HealthResponse health,
    ManaResponse mana,
    StatsResponse stats,
    InventoryResponse inventory,
    EquippedItemsResponse equippedItems,
    List<String> spells) {}
