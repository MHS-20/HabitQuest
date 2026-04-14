package habitquest.quest.infrastructure;

import common.ddd.Id;
import habitquest.quest.application.QuestLogger;
import habitquest.quest.application.QuestNotFoundException;
import habitquest.quest.application.QuestProgressView;
import habitquest.quest.application.QuestService;
import habitquest.quest.domain.*;
import habitquest.quest.infrastructure.dto.HabitMapper;
import habitquest.quest.infrastructure.dto.QuestRequestsDto.*;
import habitquest.quest.infrastructure.dto.QuestResponseAssembler;
import habitquest.quest.infrastructure.dto.QuestResponsesDto.*;
import java.net.URI;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
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
  private final QuestResponseAssembler questResponseAssembler;
  private final QuestLogger log;

  public QuestController(
      QuestService questService, QuestResponseAssembler questResponseAssembler, QuestLogger log) {
    this.questService = questService;
    this.questResponseAssembler = questResponseAssembler;
    this.log = log;
  }

  @PostMapping
  public ResponseEntity<EntityModel<QuestCreatedResponse>> createQuest(
      @RequestBody CreateQuestRequest request) {
    log.info(request, "Creating quest");
    Quest created =
        questService.createQuest(request.name(), parseDurationDays(request.durationDays()));
    log.info(created, "Quest created");

    return ResponseEntity.created(URI.create("/api/v1/quests/" + created.getId().value()))
        .body(questResponseAssembler.toCreatedModel(created));
  }

  @GetMapping
  public ResponseEntity<CollectionModel<EntityModel<QuestResponse>>> getAllQuests() {
    return ResponseEntity.ok(questResponseAssembler.toCollectionModel(questService.getAllQuests()));
  }

  @GetMapping("/{id}")
  public ResponseEntity<EntityModel<QuestResponse>> getQuest(@PathVariable String id)
      throws QuestNotFoundException {
    Quest quest = questService.getQuest(idOfQuest(id));
    log.info(quest, "Fetched quest");
    return ResponseEntity.ok(questResponseAssembler.toModel(quest));
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
    return ResponseEntity.ok(questResponseAssembler.toNameModel(id, name));
  }

  @GetMapping("/{id}/duration")
  public ResponseEntity<EntityModel<DurationResponse>> getDuration(@PathVariable String id)
      throws QuestNotFoundException {
    Duration duration = questService.getDuration(idOfQuest(id));
    log.info(idOfQuest(id), "Fetched quest duration");
    return ResponseEntity.ok(questResponseAssembler.toDurationModel(id, duration));
  }

  @GetMapping("/{id}/reward")
  public ResponseEntity<EntityModel<Reward>> getReward(@PathVariable String id)
      throws QuestNotFoundException {
    Reward reward = questService.getReward(idOfQuest(id));
    log.info(reward, "Fetched quest reward");
    return ResponseEntity.ok(questResponseAssembler.toRewardModel(id, reward));
  }

  @GetMapping("/{id}/habits")
  public ResponseEntity<EntityModel<HabitsResponse>> getHabits(@PathVariable String id)
      throws QuestNotFoundException {
    List<Habit> habits = questService.getHabits(idOfQuest(id));
    log.info(habits, "Fetched quest habits");
    return ResponseEntity.ok(questResponseAssembler.toHabitsModel(id, habits));
  }

  @GetMapping("/progress/{avatarId}")
  public ResponseEntity<EntityModel<AvatarQuestProgressResponse>> getActiveQuestProgress(
      @PathVariable String avatarId) {
    List<QuestProgressView> progress =
        questService.getActiveQuestProgressByAvatar(idOfAvatar(avatarId));
    return ResponseEntity.ok(questResponseAssembler.toProgressModel(avatarId, progress));
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

  // ─── private helpers ────────────────────────────────────────────────────────

  private static Id<Quest> idOfQuest(String id) {
    return new Id<>(id);
  }

  private static Id<Habit> idOfHabit(String id) {
    return new Id<>(id);
  }

  private static Id<Avatar> idOfAvatar(String id) {
    return new Id<>(id);
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
}
