package habitquest.quest.infrastructure.inbound;

import common.ddd.Id;
import habitquest.quest.application.exceptions.QuestNotFoundException;
import habitquest.quest.application.port.in.QuestQueryService;
import habitquest.quest.application.port.out.QuestLogger;
import habitquest.quest.application.service.QuestProgressView;
import habitquest.quest.domain.*;
import habitquest.quest.infrastructure.dto.QuestQueries.*;
import habitquest.quest.infrastructure.dto.QuestResponseAssembler;
import java.time.Duration;
import java.util.List;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/quests")
public class QuestQueryController {

  private final QuestQueryService questQueryService;
  private final QuestResponseAssembler questResponseAssembler;
  private final QuestLogger log;

  public QuestQueryController(
      QuestQueryService questQueryService,
      QuestResponseAssembler questResponseAssembler,
      QuestLogger log) {
    this.questQueryService = questQueryService;
    this.questResponseAssembler = questResponseAssembler;
    this.log = log;
  }

  @GetMapping
  public ResponseEntity<CollectionModel<EntityModel<QuestResponse>>> getAllQuests() {
    return ResponseEntity.ok(
        questResponseAssembler.toCollectionModel(questQueryService.getAllQuests()));
  }

  @GetMapping("/{id}")
  public ResponseEntity<EntityModel<QuestResponse>> getQuest(@PathVariable String id)
      throws QuestNotFoundException {
    Quest quest = questQueryService.getQuest(idOfQuest(id));
    log.info(quest, "Fetched quest");
    return ResponseEntity.ok(questResponseAssembler.toModel(quest));
  }

  @GetMapping("/{id}/name")
  public ResponseEntity<EntityModel<NameResponse>> getName(@PathVariable String id)
      throws QuestNotFoundException {
    String name = questQueryService.getName(idOfQuest(id));
    log.info(idOfQuest(id), "Fetched quest name");
    return ResponseEntity.ok(questResponseAssembler.toNameModel(id, name));
  }

  @GetMapping("/{id}/duration")
  public ResponseEntity<EntityModel<DurationResponse>> getDuration(@PathVariable String id)
      throws QuestNotFoundException {
    Duration duration = questQueryService.getDuration(idOfQuest(id));
    log.info(idOfQuest(id), "Fetched quest duration");
    return ResponseEntity.ok(questResponseAssembler.toDurationModel(id, duration));
  }

  @GetMapping("/{id}/reward")
  public ResponseEntity<EntityModel<Reward>> getReward(@PathVariable String id)
      throws QuestNotFoundException {
    Reward reward = questQueryService.getReward(idOfQuest(id));
    log.info(reward, "Fetched quest reward");
    return ResponseEntity.ok(questResponseAssembler.toRewardModel(id, reward));
  }

  @GetMapping("/{id}/habits")
  public ResponseEntity<EntityModel<HabitsResponse>> getHabits(@PathVariable String id)
      throws QuestNotFoundException {
    List<Habit> habits = questQueryService.getHabits(idOfQuest(id));
    log.info(habits, "Fetched quest habits");
    return ResponseEntity.ok(questResponseAssembler.toHabitsModel(id, habits));
  }

  @GetMapping("/progress/{avatarId}")
  public ResponseEntity<EntityModel<AvatarQuestProgressResponse>> getActiveQuestProgress(
      @PathVariable String avatarId) {
    List<QuestProgressView> progress =
        questQueryService.getActiveQuestProgressByAvatar(idOfAvatar(avatarId));
    return ResponseEntity.ok(questResponseAssembler.toProgressModel(avatarId, progress));
  }

  // ─── private helpers ────────────────────────────────────────────────────────

  private static Id<Quest> idOfQuest(String id) {
    return new Id<>(id);
  }

  private static Id<Avatar> idOfAvatar(String id) {
    return new Id<>(id);
  }
}
