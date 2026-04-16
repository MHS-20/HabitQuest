package habitquest.quest.infrastructure.inbound;

import common.ddd.Id;
import habitquest.quest.application.exceptions.QuestNotFoundException;
import habitquest.quest.application.port.in.QuestCommandService;
import habitquest.quest.application.port.out.QuestLogger;
import habitquest.quest.domain.*;
import habitquest.quest.infrastructure.dto.HabitMapper;
import habitquest.quest.infrastructure.dto.QuestCommands.*;
import habitquest.quest.infrastructure.dto.QuestQueries.*;
import habitquest.quest.infrastructure.dto.QuestResponseAssembler;
import java.net.URI;
import java.time.Duration;
import java.time.LocalDate;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/quests")
public class QuestCommandController {

  private final QuestCommandService questCommandService;
  private final QuestResponseAssembler questResponseAssembler;
  private final QuestLogger log;

  public QuestCommandController(
      QuestCommandService questCommandService,
      QuestResponseAssembler questResponseAssembler,
      QuestLogger log) {
    this.questCommandService = questCommandService;
    this.questResponseAssembler = questResponseAssembler;
    this.log = log;
  }

  @PostMapping
  public ResponseEntity<EntityModel<QuestCreatedResponse>> createQuest(
      @RequestBody CreateQuestCommand request) {
    log.info(request, "Creating quest");
    Quest created =
        questCommandService.createQuest(request.name(), parseDurationDays(request.durationDays()));
    log.info(created, "Quest created");
    return ResponseEntity.created(URI.create("/api/v1/quests/" + created.getId().value()))
        .body(questResponseAssembler.toCreatedModel(created));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteQuest(@PathVariable String id) throws QuestNotFoundException {
    log.info(idOfQuest(id), "Deleting quest");
    questCommandService.deleteQuest(idOfQuest(id));
    return ResponseEntity.noContent().build();
  }

  @PatchMapping("/{id}/name")
  public ResponseEntity<Void> updateName(
      @PathVariable String id, @RequestBody UpdateNameCommand request)
      throws QuestNotFoundException {
    log.info(request, "Updating quest name for quest " + id);
    questCommandService.updateName(idOfQuest(id), request.name());
    return ResponseEntity.noContent().build();
  }

  @PatchMapping("/{id}/duration")
  public ResponseEntity<Void> updateDuration(
      @PathVariable String id, @RequestBody UpdateDurationCommand request)
      throws QuestNotFoundException {
    log.info(request, "Updating quest duration for quest " + id);
    questCommandService.updateDuration(idOfQuest(id), parseDurationDays(request.durationDays()));
    return ResponseEntity.noContent().build();
  }

  @PatchMapping("/{id}/reward")
  public ResponseEntity<Void> updateReward(
      @PathVariable String id, @RequestBody UpdateRewardCommand request)
      throws QuestNotFoundException {
    log.info(request, "Updating quest reward for quest " + id);
    questCommandService.updateReward(idOfQuest(id), new MoneyReward(request.money()));
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{id}/habits")
  public ResponseEntity<Void> addHabit(
      @PathVariable String id, @RequestBody AddHabitCommand request) throws QuestNotFoundException {
    log.info(request, "Adding habit to quest " + id);
    questCommandService.addHabit(idOfQuest(id), HabitMapper.toDomain(request.habitId(), request));
    return ResponseEntity.noContent().build();
  }

  @DeleteMapping("/{id}/habits")
  public ResponseEntity<Void> removeHabit(
      @PathVariable String id, @RequestBody RemoveHabitCommand request)
      throws QuestNotFoundException {
    log.info(request, "Removing habit from quest " + id);
    questCommandService.removeHabit(idOfQuest(id), idOfHabit(request.habitId()));
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{id}/attendance")
  public ResponseEntity<Void> recordAttendance(
      @PathVariable String id, @RequestBody RecordAttendanceCommand request)
      throws QuestNotFoundException {
    log.info(request, "Recording habit attendance for quest " + id);
    questCommandService.recordHabitAttendance(
        idOfQuest(id),
        idOfAvatar(request.avatarId()),
        idOfHabit(request.habitId()),
        LocalDate.parse(request.attendedOn()));
    log.info(request, "Habit attendance recorded for quest " + id);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{id}/join")
  public ResponseEntity<Void> joinQuest(
      @PathVariable String id, @RequestBody JoinQuestCommand request)
      throws QuestNotFoundException {
    LocalDate joinedOn =
        request.joinedOn() == null || request.joinedOn().isBlank()
            ? LocalDate.now()
            : LocalDate.parse(request.joinedOn());
    questCommandService.joinQuest(idOfQuest(id), idOfAvatar(request.avatarId()), joinedOn);
    return ResponseEntity.noContent().build();
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
