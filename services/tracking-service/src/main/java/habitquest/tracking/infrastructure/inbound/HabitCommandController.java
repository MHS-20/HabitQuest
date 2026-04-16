package habitquest.tracking.infrastructure.inbound;

import common.ddd.Id;
import habitquest.tracking.application.exceptions.HabitNotFoundException;
import habitquest.tracking.application.port.in.HabitCommandService;
import habitquest.tracking.application.port.out.HabitLogger;
import habitquest.tracking.domain.Avatar;
import habitquest.tracking.domain.Habit;
import habitquest.tracking.domain.Tag;
import habitquest.tracking.domain.reminder.Recurrence;
import habitquest.tracking.infrastructure.dto.HabitCommands.*;
import habitquest.tracking.infrastructure.dto.HabitMapper;
import habitquest.tracking.infrastructure.dto.HabitQueries.*;
import habitquest.tracking.infrastructure.dto.HabitResponseAssembler;
import java.net.URI;
import java.util.List;
import java.util.Objects;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/habits")
public class HabitCommandController {

  private final HabitCommandService habitCommandService;
  private final HabitResponseAssembler habitResponseAssembler;
  private final HabitLogger log;

  public HabitCommandController(
      HabitCommandService habitCommandService,
      HabitResponseAssembler habitResponseAssembler,
      HabitLogger log) {
    this.habitCommandService = habitCommandService;
    this.habitResponseAssembler = habitResponseAssembler;
    this.log = log;
  }

  @PostMapping
  public ResponseEntity<EntityModel<HabitCreatedResponse>> createHabit(
      @RequestBody CreateHabitCommand request) {
    log.info(request, "Creating habit");

    Id<Avatar> avatarId = idOfAvatar(request.avatarId());
    Recurrence recurrence =
        HabitMapper.toRecurrence(
            request.recurrenceType(), request.dayOfWeek(), request.dayOfMonth());

    Habit created =
        habitCommandService.createHabit(
            avatarId,
            request.title(),
            request.description(),
            recurrence,
            request.associatedQuestId(),
            request.sourceHabitId());

    List<Tag> tags =
        request.tags() == null
            ? List.of()
            : request.tags().stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(tag -> !tag.isBlank())
                .map(Tag::new)
                .toList();
    if (!tags.isEmpty()) {
      try {
        created = habitCommandService.updateTags(created.getId(), tags);
      } catch (HabitNotFoundException ex) {
        throw new IllegalStateException("Created habit not found while applying tags", ex);
      }
    }

    log.info(created, "Habit created");
    return ResponseEntity.created(URI.create("/api/v1/habits/" + created.getId().value()))
        .body(habitResponseAssembler.toCreatedModel(created));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteHabit(@PathVariable String id) throws HabitNotFoundException {
    log.info(idOfHabit(id), "Deleting habit");
    habitCommandService.deleteHabitById(idOfHabit(id));
    return ResponseEntity.noContent().build();
  }

  @PatchMapping("/{id}/title")
  public ResponseEntity<Void> updateTitle(
      @PathVariable String id, @RequestBody UpdateTitleCommand request)
      throws HabitNotFoundException {
    log.info(request, "Updating title for habit " + id);
    habitCommandService.updateTitle(idOfHabit(id), request.title());
    return ResponseEntity.noContent().build();
  }

  @PatchMapping("/{id}/description")
  public ResponseEntity<Void> updateDescription(
      @PathVariable String id, @RequestBody UpdateDescriptionCommand request)
      throws HabitNotFoundException {
    log.info(request, "Updating description for habit " + id);
    habitCommandService.updateDescription(idOfHabit(id), request.description());
    return ResponseEntity.noContent().build();
  }

  @PatchMapping("/{id}/tags")
  public ResponseEntity<Void> updateTags(
      @PathVariable String id, @RequestBody UpdateTagsCommand request)
      throws HabitNotFoundException {
    log.info(request, "Updating tags for habit " + id);
    List<Tag> tags = request.tags().stream().map(Tag::new).toList();
    habitCommandService.updateTags(idOfHabit(id), tags);
    return ResponseEntity.noContent().build();
  }

  @PatchMapping("/{id}/recurrence")
  public ResponseEntity<Void> updateRecurrence(
      @PathVariable String id, @RequestBody UpdateRecurrenceCommand request)
      throws HabitNotFoundException {
    log.info(request, "Updating recurrence for habit " + id);
    habitCommandService.updateRecurrence(
        idOfHabit(id),
        HabitMapper.toRecurrence(request.type(), request.dayOfWeek(), request.dayOfMonth()));
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{id}/attend")
  public ResponseEntity<Void> attendHabit(
      @PathVariable String id, @RequestBody AttendCommand request) throws HabitNotFoundException {
    log.info(request, "Attending habit " + id);
    habitCommandService.attendHabit(idOfHabit(id), request.date());
    return ResponseEntity.noContent().build();
  }

  // ─── private helpers ─────────────────────────────────────────────────────────

  private Id<Habit> idOfHabit(String id) {
    return new Id<>(id);
  }

  private Id<Avatar> idOfAvatar(String id) {
    return new Id<>(id);
  }
}
