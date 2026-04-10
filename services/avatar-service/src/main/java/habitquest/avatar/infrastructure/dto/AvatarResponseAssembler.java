package habitquest.avatar.infrastructure.dto;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

import habitquest.avatar.application.AvatarNotFoundException;
import habitquest.avatar.application.AvatarSearchRequest;
import habitquest.avatar.domain.avatar.*;
import habitquest.avatar.domain.items.*;
import habitquest.avatar.domain.stats.AvatarStats;
import habitquest.avatar.infrastructure.AvatarController;
import java.util.List;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
public class AvatarResponseAssembler
    implements RepresentationModelAssembler<Avatar, EntityModel<AvatarResponse>> {

  @Override
  @NonNull
  public EntityModel<AvatarResponse> toModel(@NonNull Avatar avatar) {
    String id = avatar.getId().value();
    AvatarResponse dto = AvatarMapper.toResponse(avatar);

    return EntityModel.of(
        dto,
        selfLink(id),
        linkTo(methodOn(AvatarController.class).getInventory(id)).withRel("inventory"),
        linkTo(methodOn(AvatarController.class).getEquippedItems(id)).withRel("equippedItems"),
        linkTo(methodOn(AvatarController.class).getStats(id)).withRel("stats"),
        linkTo(methodOn(AvatarController.class).getLevel(id)).withRel("level"),
        linkTo(methodOn(AvatarController.class).getHealth(id)).withRel("health"),
        linkTo(methodOn(AvatarController.class).getMana(id)).withRel("mana"),
        linkTo(methodOn(AvatarController.class).getMoney(id)).withRel("money"),
        linkTo(methodOn(AvatarController.class).deleteAvatar(id)).withRel("delete"));
  }

  public EntityModel<AvatarController.AvatarCreatedResponse> toCreatedModel(String id) {
    AvatarController.AvatarCreatedResponse body = new AvatarController.AvatarCreatedResponse(id);

    return EntityModel.of(
        body,
        selfLink(id),
        linkTo(methodOn(AvatarController.class).getAvatar(id)).withRel("avatar"),
        linkTo(methodOn(AvatarController.class).getLevel(id)).withRel("level"),
        linkTo(methodOn(AvatarController.class).getHealth(id)).withRel("health"));
  }

  public CollectionModel<EntityModel<AvatarResponse>> toCollectionModel(
      List<Avatar> avatars, AvatarSearchRequest query) {

    List<EntityModel<AvatarResponse>> models =
        avatars.stream()
            .map(
                avatar -> {
                  String id = avatar.getId().value();
                  AvatarResponse dto = AvatarMapper.toResponse(avatar);
                  return EntityModel.of(
                      dto,
                      selfLink(id),
                      linkTo(methodOn(AvatarController.class).getAvatar(id)).withRel("avatar"));
                })
            .toList();

    return CollectionModel.of(
        models, linkTo(methodOn(AvatarController.class).searchAvatars(query)).withSelfRel());
  }

  // ─── Sotto-risorse ───────────────────────────────────────────────────────────

  public EntityModel<AvatarController.NameResponse> toNameModel(String name, String id) {
    return EntityModel.of(new AvatarController.NameResponse(name), selfLink(id), avatarLink(id));
  }

  public EntityModel<AvatarController.MoneyResponse> toMoneyModel(Money money, String id) {
    AvatarController.MoneyResponse dto = AvatarMapper.toResponse(money);

    return EntityModel.of(
        dto,
        selfLink(id),
        avatarLink(id),
        linkTo(methodOn(AvatarController.class).earnMoney(id, null)).withRel("earn"),
        linkTo(methodOn(AvatarController.class).spendMoney(id, null)).withRel("spend"));
  }

  public EntityModel<AvatarController.InventoryResponse> toInventoryModel(
      Inventory inventory, String id) {
    AvatarController.InventoryResponse dto = AvatarMapper.toResponse(inventory);

    return EntityModel.of(
        dto,
        selfLink(id),
        avatarLink(id),
        linkTo(methodOn(AvatarController.class).addItem(id, null)).withRel("addItem"),
        linkTo(methodOn(AvatarController.class).removeItem(id, null)).withRel("removeItem"),
        linkTo(methodOn(AvatarController.class).getEquippedItems(id)).withRel("equippedItems"));
  }

  public EntityModel<AvatarController.EquippedItemsResponse> toEquippedItemsModel(
      EquippedItems equippedItems, String id) {
    AvatarController.EquippedItemsResponse dto = AvatarMapper.toResponse(equippedItems);

    return EntityModel.of(
        dto,
        selfLink(id),
        avatarLink(id),
        linkTo(methodOn(AvatarController.class).getInventory(id)).withRel("inventory"));
  }

  public EntityModel<AvatarController.ExperienceResponse> toExperienceModel(
      Experience experience, String id) {
    AvatarController.ExperienceResponse dto = AvatarMapper.toResponse(experience);

    return EntityModel.of(
        dto,
        selfLink(id),
        avatarLink(id),
        linkTo(methodOn(AvatarController.class).getLevel(id)).withRel("level"),
        linkTo(methodOn(AvatarController.class).grantExperience(id, null)).withRel("grant"));
  }

  public EntityModel<AvatarController.LevelResponse> toLevelModel(Level level, String id) {
    AvatarController.LevelResponse dto = AvatarMapper.toResponse(level);
    return EntityModel.of(dto, selfLink(id), avatarLink(id));
  }

  public EntityModel<AvatarController.HealthResponse> toHealthModel(
      AvatarHealth health, String id) {
    AvatarController.HealthResponse dto = AvatarMapper.toResponse(health);

    return EntityModel.of(
        dto,
        selfLink(id),
        avatarLink(id),
        linkTo(methodOn(AvatarController.class).healAvatar(id, null)).withRel("heal"),
        linkTo(methodOn(AvatarController.class).applyDamage(id, null)).withRel("damage"));
  }

  public EntityModel<AvatarController.ManaResponse> toManaModel(AvatarMana mana, String id) {
    AvatarController.ManaResponse dto = AvatarMapper.toResponse(mana);

    return EntityModel.of(
        dto,
        selfLink(id),
        avatarLink(id),
        linkTo(methodOn(AvatarController.class).restoreMana(id, null)).withRel("restore"),
        linkTo(methodOn(AvatarController.class).spendMana(id, null)).withRel("spend"));
  }

  public EntityModel<AvatarController.StatsResponse> toStatsModel(AvatarStats stats, String id) {
    AvatarController.StatsResponse dto = AvatarMapper.toResponse(stats);

    return EntityModel.of(
        dto,
        selfLink(id),
        avatarLink(id),
        linkTo(methodOn(AvatarController.class).increaseStrength(id)).withRel("increaseStrength"),
        linkTo(methodOn(AvatarController.class).increaseDefense(id)).withRel("increaseDefense"),
        linkTo(methodOn(AvatarController.class).increaseIntelligence(id))
            .withRel("increaseIntelligence"));
  }

  public CollectionModel<EntityModel<AvatarController.InviteResponse>> toInvitesModel(
      List<Invite> invites) {
    List<EntityModel<AvatarController.InviteResponse>> models =
        invites.stream()
            .map(
                invite ->
                    EntityModel.of(
                        new AvatarController.InviteResponse(
                            invite.inviteId().value(),
                            invite.guildId().value(),
                            invite.guildName(),
                            invite.expiresAt())))
            .toList();

    return CollectionModel.of(models);
  }

  // ─── Helper privati ─────────────────────────────────────────────────────────

  private Link selfLink(String id) {
    try {
      return linkTo(methodOn(AvatarController.class).getAvatar(id)).withSelfRel();
    } catch (AvatarNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  private Link avatarLink(String id) {
    return linkTo(methodOn(AvatarController.class).getAvatar(id)).withRel("avatar");
  }
}
