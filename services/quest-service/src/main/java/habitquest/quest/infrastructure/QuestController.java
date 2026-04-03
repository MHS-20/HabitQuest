package habitquest.quest.infrastructure;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import common.ddd.Id;
import habitquest.quest.application.QuestLogger;
import habitquest.quest.application.QuestNotFoundException;
import habitquest.quest.application.QuestProgressView;
import habitquest.quest.application.QuestService;
import habitquest.quest.domain.*;
import habitquest.quest.infrastructure.dto.HabitMapper;
import habitquest.quest.infrastructure.dto.HabitResponse;
import habitquest.quest.infrastructure.dto.QuestMapper;
import habitquest.quest.infrastructure.dto.QuestResponse;
import java.net.URI;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/quests")
public class QuestController {

  private final QuestService questService;
  private final QuestLogger log;

  public QuestController(QuestService questService, QuestLogger log) {
    this.questService = questService;
    this.log = log;
  }

  @PostMapping
  public ResponseEntity<EntityModel<QuestCreatedResponse>> createQuest(
      @RequestBody CreateQuestRequest request) {
    log.info(request, "Creating quest");
    Quest created =
        questService.createQuest(request.name(), parseDurationDays(request.durationDays()));
    log.info(created, "Quest created");

    QuestCreatedResponse body = new QuestCreatedResponse(created.getId().value());
    EntityModel<QuestCreatedResponse> model =
        EntityModel.of(
            body,
            selfLink(created.getId().value()),
            linkTo(methodOn(QuestController.class).getQuest(created.getId().value()))
                .withRel("quest"),
            linkTo(methodOn(QuestController.class).getDuration(created.getId().value()))
                .withRel("duration"),
            linkTo(methodOn(QuestController.class).getReward(created.getId().value()))
                .withRel("reward"),
            linkTo(methodOn(QuestController.class).getHabits(created.getId().value()))
                .withRel("habits"));

    return ResponseEntity.created(URI.create("/api/v1/quests/" + created.getId().value()))
        .body(model);
  }

  @GetMapping
  public ResponseEntity<CollectionModel<EntityModel<QuestResponse>>> getAllQuests() {
    List<EntityModel<QuestResponse>> questModels =
        questService.getAllQuests().stream()
            .map(
                quest ->
                    EntityModel.of(
                        QuestMapper.toResponse(quest),
                        selfLink(quest.getId().value()),
                        linkTo(methodOn(QuestController.class).getName(quest.getId().value()))
                            .withRel("name"),
                        linkTo(methodOn(QuestController.class).getDuration(quest.getId().value()))
                            .withRel("duration"),
                        linkTo(methodOn(QuestController.class).getReward(quest.getId().value()))
                            .withRel("reward"),
                        linkTo(methodOn(QuestController.class).getHabits(quest.getId().value()))
                            .withRel("habits")))
            .toList();

    CollectionModel<EntityModel<QuestResponse>> model =
        CollectionModel.of(
            questModels, linkTo(methodOn(QuestController.class).getAllQuests()).withSelfRel());
    return ResponseEntity.ok(model);
  }

  @GetMapping("/{id}")
  public ResponseEntity<EntityModel<QuestResponse>> getQuest(@PathVariable String id)
      throws QuestNotFoundException {
    Quest quest = questService.getQuest(idOfQuest(id));
    log.info(quest, "Fetched quest");

    QuestResponse dto = QuestMapper.toResponse(quest);
    EntityModel<QuestResponse> model =
        EntityModel.of(
            dto,
            selfLink(id),
            linkTo(methodOn(QuestController.class).getName(id)).withRel("name"),
            linkTo(methodOn(QuestController.class).getDuration(id)).withRel("duration"),
            linkTo(methodOn(QuestController.class).getReward(id)).withRel("reward"),
            linkTo(methodOn(QuestController.class).getHabits(id)).withRel("habits"),
            linkTo(methodOn(QuestController.class).deleteQuest(id)).withRel("delete"));

    return ResponseEntity.ok(model);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteQuest(@PathVariable String id) throws QuestNotFoundException {
    log.info(idOfQuest(id), "Deleting quest");
    questService.deleteQuest(idOfQuest(id));
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/{id}/name")
  public ResponseEntity<EntityModel<NameResponse>> getName(@PathVariable String id)
      throws QuestNotFoundException {
    String name = questService.getName(idOfQuest(id));
    log.info(idOfQuest(id), "Fetched quest name");
    EntityModel<NameResponse> model =
        EntityModel.of(new NameResponse(name), selfLink(id), questLink(id));
    return ResponseEntity.ok(model);
  }

  @GetMapping("/{id}/duration")
  public ResponseEntity<EntityModel<DurationResponse>> getDuration(@PathVariable String id)
      throws QuestNotFoundException {
    Duration duration = questService.getDuration(idOfQuest(id));
    log.info(idOfQuest(id), "Fetched quest duration");
    EntityModel<DurationResponse> model =
        EntityModel.of(new DurationResponse(duration.toDays()), selfLink(id), questLink(id));
    return ResponseEntity.ok(model);
  }

  @GetMapping("/{id}/reward")
  public ResponseEntity<EntityModel<Reward>> getReward(@PathVariable String id)
      throws QuestNotFoundException {
    Reward reward = questService.getReward(idOfQuest(id));
    log.info(reward, "Fetched quest reward");
    EntityModel<Reward> model = EntityModel.of(reward, selfLink(id), questLink(id));
    return ResponseEntity.ok(model);
  }

  @GetMapping("/{id}/habits")
  public ResponseEntity<EntityModel<HabitsResponse>> getHabits(@PathVariable String id)
      throws QuestNotFoundException {
    List<Habit> habits = questService.getHabits(idOfQuest(id));
    log.info(habits, "Fetched quest habits");
    List<HabitResponse> habitResponses = habits.stream().map(HabitMapper::toResponse).toList();
    EntityModel<HabitsResponse> model =
        EntityModel.of(new HabitsResponse(habitResponses), selfLink(id), questLink(id));
    return ResponseEntity.ok(model);
  }

  @GetMapping("/progress/{avatarId}")
  public ResponseEntity<EntityModel<AvatarQuestProgressResponse>> getActiveQuestProgress(
      @PathVariable String avatarId) {
    List<QuestProgressView> progress =
        questService.getActiveQuestProgressByAvatar(idOfAvatar(avatarId));
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

    EntityModel<AvatarQuestProgressResponse> model =
        EntityModel.of(
            new AvatarQuestProgressResponse(avatarId, items),
            linkTo(methodOn(QuestController.class).getActiveQuestProgress(avatarId)).withSelfRel());
    return ResponseEntity.ok(model);
  }

  @PatchMapping("/{id}/name")
  public ResponseEntity<Void> updateName(
      @PathVariable String id, @RequestBody UpdateNameRequest request)
      throws QuestNotFoundException {
    log.info(request, "Updating quest name for quest " + id);
    questService.updateName(idOfQuest(id), request.name());
    return ResponseEntity.noContent().build();
  }

  @PatchMapping("/{id}/duration")
  public ResponseEntity<Void> updateDuration(
      @PathVariable String id, @RequestBody UpdateDurationRequest request)
      throws QuestNotFoundException {
    log.info(request, "Updating quest duration for quest " + id);
    questService.updateDuration(idOfQuest(id), parseDurationDays(request.durationDays()));
    return ResponseEntity.noContent().build();
  }

  @PatchMapping("/{id}/reward")
  public ResponseEntity<Void> updateReward(
      @PathVariable String id, @RequestBody UpdateRewardRequest request)
      throws QuestNotFoundException {
    log.info(request, "Updating quest reward for quest " + id);
    questService.updateReward(idOfQuest(id), new MoneyReward(request.money()));
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{id}/habits")
  public ResponseEntity<Void> addHabit(
      @PathVariable String id, @RequestBody AddHabitRequest request) throws QuestNotFoundException {
    log.info(request, "Adding habit to quest " + id);
    questService.addHabit(idOfQuest(id), HabitMapper.toDomain(request.habitId(), request));
    return ResponseEntity.noContent().build();
  }

  @DeleteMapping("/{id}/habits")
  public ResponseEntity<Void> removeHabit(
      @PathVariable String id, @RequestBody RemoveHabitRequest request)
      throws QuestNotFoundException {
    log.info(request, "Removing habit from quest " + id);
    questService.removeHabit(idOfQuest(id), idOfHabit(request.habitId()));
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{id}/attendance")
  public ResponseEntity<Void> recordAttendance(
      @PathVariable String id, @RequestBody RecordAttendanceRequest request)
      throws QuestNotFoundException {
    log.info(request, "Recording habit attendance for quest " + id);
    questService.recordHabitAttendance(
        idOfQuest(id),
        idOfAvatar(request.avatarId()),
        idOfHabit(request.habitId()),
        LocalDate.parse(request.attendedOn()));
    log.info(request, "Habit attendance recorded for quest " + id);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{id}/join")
  public ResponseEntity<Void> joinQuest(
      @PathVariable String id, @RequestBody JoinQuestRequest request)
      throws QuestNotFoundException {
    LocalDate joinedOn =
        request.joinedOn() == null || request.joinedOn().isBlank()
            ? LocalDate.now()
            : LocalDate.parse(request.joinedOn());
    questService.joinQuest(idOfQuest(id), idOfAvatar(request.avatarId()), joinedOn);
    return ResponseEntity.noContent().build();
  }

  @ExceptionHandler(QuestNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleQuestNotFound(QuestNotFoundException ex) {
    return ResponseEntity.notFound().build();
  }

  @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
  public ResponseEntity<ErrorResponse> handleDomainError(RuntimeException ex) {
    log.error(ex, "Domain error", ex);
    return ResponseEntity.badRequest().body(new ErrorResponse(ex.getMessage()));
  }

  @ExceptionHandler(TrackingHabitCommunicationException.class)
  public ResponseEntity<ErrorResponse> handleTrackingHabitError(
      TrackingHabitCommunicationException ex) {
    log.error(ex, "Tracking habit synchronization error", ex);
    return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(new ErrorResponse(ex.getMessage()));
  }

  private static Id<Quest> idOfQuest(String id) {
    return new Id<>(id);
  }

  private static Id<Habit> idOfHabit(String id) {
    return new Id<>(id);
  }

  private static Id<Avatar> idOfAvatar(String id) {
    return new Id<>(id);
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

  private static Duration parseDurationDays(Integer durationDays) {
    if (durationDays == null) {
      throw new IllegalArgumentException("Duration days cannot be null");
    }
    if (durationDays <= 0) {
      throw new IllegalArgumentException("Duration days must be greater than 0");
    }
    return Duration.ofDays(durationDays);
  }

  public record CreateQuestRequest(String name, Integer durationDays) {}

  public record UpdateNameRequest(String name) {}

  public record UpdateDurationRequest(Integer durationDays) {}

  public record UpdateRewardRequest(Integer experience, Integer money) {}

  public record RemoveHabitRequest(String habitId, String title) {}

  public record AddHabitRequest(
      String habitId,
      String title,
      String description,
      List<String> tags,
      RecurrenceRequest recurrence) {}

  public record RecurrenceRequest(String type, Integer dayOfMonth, String dayOfWeek) {}

  public record RecordAttendanceRequest(String avatarId, String habitId, String attendedOn) {}

  public record JoinQuestRequest(String avatarId, String joinedOn) {}

  public record QuestCreatedResponse(String id) {}

  public record NameResponse(String name) {}

  public record DurationResponse(Long durationDays) {}

  public record HabitsResponse(List<HabitResponse> habits) {}

  public record AvatarQuestProgressResponse(String avatarId, List<QuestProgressResponse> quests) {}

  public record QuestProgressResponse(
      String questId,
      String questName,
      String status,
      Integer completionPercentage,
      List<HabitProgressResponse> habits) {}

  public record HabitProgressResponse(
      String habitId,
      String title,
      Integer requiredOccurrences,
      Integer attendedOccurrences,
      Integer remainingOccurrences) {}

  public record ErrorResponse(String message) {}
}
