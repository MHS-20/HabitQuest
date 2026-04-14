package habitquest.quest.infrastructure.dto;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import habitquest.quest.application.QuestNotFoundException;
import habitquest.quest.application.QuestProgressView;
import habitquest.quest.domain.Habit;
import habitquest.quest.domain.Quest;
import habitquest.quest.domain.Reward;
import habitquest.quest.infrastructure.QuestController;
import habitquest.quest.infrastructure.dto.QuestRequestsDto.*;
import habitquest.quest.infrastructure.dto.QuestResponsesDto.*;
import java.time.Duration;
import java.util.List;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.stereotype.Component;

@Component
public class QuestResponseAssembler {

  public EntityModel<QuestCreatedResponse> toCreatedModel(Quest quest) {
    String id = quest.getId().value();
    return EntityModel.of(
        new QuestCreatedResponse(id),
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

  public EntityModel<NameResponse> toNameModel(String id, String name) {
    return EntityModel.of(new NameResponse(name), selfLink(id), questLink(id));
  }

  public EntityModel<DurationResponse> toDurationModel(String id, Duration duration) {
    return EntityModel.of(new DurationResponse(duration.toDays()), selfLink(id), questLink(id));
  }

  public EntityModel<Reward> toRewardModel(String id, Reward reward) {
    return EntityModel.of(reward, selfLink(id), questLink(id));
  }

  public EntityModel<HabitsResponse> toHabitsModel(String id, List<Habit> habits) {
    List<HabitResponse> habitResponses = habits.stream().map(HabitMapper::toResponse).toList();
    return EntityModel.of(new HabitsResponse(habitResponses), selfLink(id), questLink(id));
  }

  public EntityModel<AvatarQuestProgressResponse> toProgressModel(
      String avatarId, List<QuestProgressView> progress) {
    List<QuestProgressResponse> items =
        progress.stream()
            .map(
                p ->
                    new QuestProgressResponse(
                        p.questId(),
                        p.questName(),
                        p.status(),
                        p.completionPercentage(),
                        p.habits().stream()
                            .map(
                                h ->
                                    new HabitProgressResponse(
                                        h.habitId(),
                                        h.title(),
                                        h.requiredOccurrences(),
                                        h.attendedOccurrences(),
                                        h.remainingOccurrences()))
                            .toList()))
            .toList();

    return EntityModel.of(
        new AvatarQuestProgressResponse(avatarId, items),
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
