package habitquest.quest.infrastructure.dto;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import habitquest.quest.application.QuestNotFoundException;
import habitquest.quest.application.QuestProgressView;
import habitquest.quest.domain.Habit;
import habitquest.quest.domain.Quest;
import habitquest.quest.domain.Reward;
import habitquest.quest.infrastructure.QuestController;
import java.time.Duration;
import java.util.List;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.stereotype.Component;

@Component
public class QuestResponseAssembler {

  public EntityModel<QuestController.QuestCreatedResponse> toCreatedModel(Quest quest) {
    String id = quest.getId().value();
    return EntityModel.of(
        new QuestController.QuestCreatedResponse(id),
        selfLink(id),
        linkTo(methodOn(QuestController.class).getQuest(id)).withRel("quest"),
        linkTo(methodOn(QuestController.class).getDuration(id)).withRel("duration"),
        linkTo(methodOn(QuestController.class).getReward(id)).withRel("reward"),
        linkTo(methodOn(QuestController.class).getHabits(id)).withRel("habits"));
  }

  public CollectionModel<EntityModel<QuestResponse>> toCollectionModel(List<Quest> quests) {
    List<EntityModel<QuestResponse>> questModels =
        quests.stream().map(this::toSummaryModel).toList();
    return CollectionModel.of(
        questModels, linkTo(methodOn(QuestController.class).getAllQuests()).withSelfRel());
  }

  public EntityModel<QuestResponse> toModel(Quest quest) {
    String id = quest.getId().value();
    return EntityModel.of(
        QuestMapper.toResponse(quest),
        selfLink(id),
        linkTo(methodOn(QuestController.class).getName(id)).withRel("name"),
        linkTo(methodOn(QuestController.class).getDuration(id)).withRel("duration"),
        linkTo(methodOn(QuestController.class).getReward(id)).withRel("reward"),
        linkTo(methodOn(QuestController.class).getHabits(id)).withRel("habits"),
        linkTo(methodOn(QuestController.class).deleteQuest(id)).withRel("delete"));
  }

  public EntityModel<QuestController.NameResponse> toNameModel(String id, String name) {
    return EntityModel.of(new QuestController.NameResponse(name), selfLink(id), questLink(id));
  }

  public EntityModel<QuestController.DurationResponse> toDurationModel(
      String id, Duration duration) {
    return EntityModel.of(
        new QuestController.DurationResponse(duration.toDays()), selfLink(id), questLink(id));
  }

  public EntityModel<Reward> toRewardModel(String id, Reward reward) {
    return EntityModel.of(reward, selfLink(id), questLink(id));
  }

  public EntityModel<QuestController.HabitsResponse> toHabitsModel(String id, List<Habit> habits) {
    List<HabitResponse> habitResponses = habits.stream().map(HabitMapper::toResponse).toList();
    return EntityModel.of(
        new QuestController.HabitsResponse(habitResponses), selfLink(id), questLink(id));
  }

  public EntityModel<QuestController.AvatarQuestProgressResponse> toProgressModel(
      String avatarId, List<QuestProgressView> progress) {
    List<QuestController.QuestProgressResponse> items =
        progress.stream()
            .map(
                p ->
                    new QuestController.QuestProgressResponse(
                        p.questId(),
                        p.questName(),
                        p.status(),
                        p.completionPercentage(),
                        p.habits().stream()
                            .map(
                                h ->
                                    new QuestController.HabitProgressResponse(
                                        h.habitId(),
                                        h.title(),
                                        h.requiredOccurrences(),
                                        h.attendedOccurrences(),
                                        h.remainingOccurrences()))
                            .toList()))
            .toList();

    return EntityModel.of(
        new QuestController.AvatarQuestProgressResponse(avatarId, items),
        linkTo(methodOn(QuestController.class).getActiveQuestProgress(avatarId)).withSelfRel());
  }

  // ─── private helpers ────────────────────────────────────────────────────────

  private EntityModel<QuestResponse> toSummaryModel(Quest quest) {
    String id = quest.getId().value();
    return EntityModel.of(
        QuestMapper.toResponse(quest),
        selfLink(id),
        linkTo(methodOn(QuestController.class).getName(id)).withRel("name"),
        linkTo(methodOn(QuestController.class).getDuration(id)).withRel("duration"),
        linkTo(methodOn(QuestController.class).getReward(id)).withRel("reward"),
        linkTo(methodOn(QuestController.class).getHabits(id)).withRel("habits"));
  }

  private Link selfLink(String id) {
    try {
      return linkTo(methodOn(QuestController.class).getQuest(id)).withSelfRel();
    } catch (QuestNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  private Link questLink(String id) {
    try {
      return linkTo(methodOn(QuestController.class).getQuest(id)).withRel("quest");
    } catch (QuestNotFoundException e) {
      throw new RuntimeException(e);
    }
  }
}
