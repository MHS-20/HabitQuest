package habitquest.quest.infrastructure;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import habitquest.quest.application.QuestNotFoundException;
import habitquest.quest.application.QuestService;
import habitquest.quest.domain.Habit;
import habitquest.quest.domain.MoneyReward;
import habitquest.quest.domain.Quest;
import habitquest.quest.domain.Reward;
import java.net.URI;
import java.time.Duration;
import java.util.List;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
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

  public QuestController(QuestService questService) {
    this.questService = questService;
  }

  @PostMapping
  public ResponseEntity<EntityModel<QuestCreatedResponse>> createQuest(
      @RequestBody CreateQuestRequest request) {
    Quest created = questService.createQuest(request.name());
    QuestCreatedResponse body = new QuestCreatedResponse(created.getId());

    EntityModel<QuestCreatedResponse> model =
        EntityModel.of(
            body,
            selfLink(created.getId()),
            linkTo(methodOn(QuestController.class).getQuest(created.getId())).withRel("quest"),
            linkTo(methodOn(QuestController.class).getDuration(created.getId()))
                .withRel("duration"),
            linkTo(methodOn(QuestController.class).getReward(created.getId())).withRel("reward"),
            linkTo(methodOn(QuestController.class).getHabits(created.getId())).withRel("habits"));

    return ResponseEntity.created(URI.create("/api/v1/quests/" + created.getId())).body(model);
  }

  @GetMapping("/{id}")
  public ResponseEntity<EntityModel<Quest>> getQuest(@PathVariable String id)
      throws QuestNotFoundException {
    Quest quest = questService.getQuest(id);

    EntityModel<Quest> model =
        EntityModel.of(
            quest,
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
    questService.deleteQuest(id);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/{id}/name")
  public ResponseEntity<EntityModel<NameResponse>> getName(@PathVariable String id)
      throws QuestNotFoundException {
    EntityModel<NameResponse> model =
        EntityModel.of(new NameResponse(questService.getName(id)), selfLink(id), questLink(id));
    return ResponseEntity.ok(model);
  }

  @GetMapping("/{id}/duration")
  public ResponseEntity<EntityModel<DurationResponse>> getDuration(@PathVariable String id)
      throws QuestNotFoundException {
    EntityModel<DurationResponse> model =
        EntityModel.of(
            new DurationResponse(questService.getDuration(id).toString()),
            selfLink(id),
            questLink(id));
    return ResponseEntity.ok(model);
  }

  @GetMapping("/{id}/reward")
  public ResponseEntity<EntityModel<Reward>> getReward(@PathVariable String id)
      throws QuestNotFoundException {
    EntityModel<Reward> model =
        EntityModel.of(questService.getReward(id), selfLink(id), questLink(id));
    return ResponseEntity.ok(model);
  }

  @GetMapping("/{id}/habits")
  public ResponseEntity<EntityModel<HabitsResponse>> getHabits(@PathVariable String id)
      throws QuestNotFoundException {
    EntityModel<HabitsResponse> model =
        EntityModel.of(new HabitsResponse(questService.getHabits(id)), selfLink(id), questLink(id));
    return ResponseEntity.ok(model);
  }

  @PatchMapping("/{id}/name")
  public ResponseEntity<Void> updateName(
      @PathVariable String id, @RequestBody UpdateNameRequest request)
      throws QuestNotFoundException {
    questService.updateName(id, request.name());
    return ResponseEntity.noContent().build();
  }

  @PatchMapping("/{id}/duration")
  public ResponseEntity<Void> updateDuration(
      @PathVariable String id, @RequestBody UpdateDurationRequest request)
      throws QuestNotFoundException {
    questService.updateDuration(id, Duration.parse(request.duration()));
    return ResponseEntity.noContent().build();
  }

  @PatchMapping("/{id}/reward")
  public ResponseEntity<Void> updateReward(
      @PathVariable String id, @RequestBody UpdateRewardRequest request)
      throws QuestNotFoundException {
    questService.updateReward(id, new MoneyReward());
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{id}/habits")
  public ResponseEntity<Void> addHabit(@PathVariable String id, @RequestBody HabitRequest request)
      throws QuestNotFoundException {
    questService.addHabit(id, new Habit(request.habitId()));
    return ResponseEntity.noContent().build();
  }

  @DeleteMapping("/{id}/habits")
  public ResponseEntity<Void> removeHabit(
      @PathVariable String id, @RequestBody HabitRequest request) throws QuestNotFoundException {
    questService.removeHabit(id, new Habit(request.habitId()));
    return ResponseEntity.noContent().build();
  }

  @ExceptionHandler(QuestNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleQuestNotFound(QuestNotFoundException ex) {
    return ResponseEntity.notFound().build();
  }

  @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
  public ResponseEntity<ErrorResponse> handleDomainError(RuntimeException ex) {
    return ResponseEntity.badRequest().body(new ErrorResponse(ex.getMessage()));
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

  public record CreateQuestRequest(String name) {}

  public record UpdateNameRequest(String name) {}

  public record UpdateDurationRequest(String duration) {}

  public record UpdateRewardRequest(Integer experience, Integer money) {}

  public record HabitRequest(String habitId, String title) {}

  public record QuestCreatedResponse(String id) {}

  public record NameResponse(String name) {}

  public record DurationResponse(String duration) {}

  public record HabitsResponse(List<Habit> habits) {}

  public record ErrorResponse(String message) {}
}
