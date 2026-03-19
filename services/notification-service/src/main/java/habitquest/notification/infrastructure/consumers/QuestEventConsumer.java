package habitquest.notification.infrastructure.consumers;

import common.hexagonal.Adapter;
import habitquest.notification.infrastructure.notification.NotificationService;
import java.time.Instant;
import java.util.function.Consumer;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Adapter
@Component
public class QuestEventConsumer implements EventConsumer {

  private final NotificationService notificationService;

  public QuestEventConsumer(NotificationService notificationService) {
    this.notificationService = notificationService;
  }

  @Bean
  public Consumer<QuestCreatedMessage> questCreated() {
    return message -> {
      logger()
          .info("Received QuestCreated: questId={}, name={}", message.questId(), message.name());
      notificationService.send("È stata creata una nuova quest: \"" + message.name() + "\"!");
    };
  }

  @Bean
  public Consumer<QuestCompletedMessage> questCompleted() {
    return message -> {
      logger()
          .info(
              "Received QuestCompleted: questId={}, avatarId={}",
              message.questId(),
              message.avatarId());
      notificationService.send("Hai completato la quest " + message.questId() + "!");
    };
  }

  @Bean
  public Consumer<QuestNotCompletedMessage> questNotCompleted() {
    return message -> {
      logger().info("Received QuestNotCompleted");
      notificationService.send("Non hai completato la quest in tempo!");
    };
  }

  @Bean
  public Consumer<QuestJoinedMessage> questJoined() {
    return message -> {
      logger()
          .info(
              "Received QuestJoined: questId={}, avatarId={}",
              message.questId(),
              message.avatarId());
      notificationService.send("Ti sei unito alla quest " + message.questId() + "!");
    };
  }

  @Bean
  public Consumer<QuestLeftMessage> questLeft() {
    return message -> {
      logger()
          .info(
              "Received QuestLeft: questId={}, avatarId={}", message.questId(), message.avatarId());
      notificationService.send("Hai abbandonato la quest " + message.questId() + ".");
    };
  }

  public record QuestCreatedMessage(String questId, String name, Instant occurredOn) {}

  public record QuestCompletedMessage(String questId, String avatarId, Instant occurredOn) {}

  public record QuestNotCompletedMessage(Instant occurredOn) {}

  public record QuestJoinedMessage(String questId, String avatarId, Instant occurredOn) {}

  public record QuestLeftMessage(String questId, String avatarId, Instant occurredOn) {}
}
