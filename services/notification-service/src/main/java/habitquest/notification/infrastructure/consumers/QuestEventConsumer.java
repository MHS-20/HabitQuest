package habitquest.notification.infrastructure.consumers;

import common.hexagonal.Adapter;
import habitquest.notification.infrastructure.notification.NotificationService;
import habitquest.notification.infrastructure.repository.UserEmailRepository;
import java.time.Instant;
import java.util.function.Consumer;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Adapter
@Component
public class QuestEventConsumer extends AvatarAwareEventConsumer {

  public QuestEventConsumer(
      UserEmailRepository userEmailRepository, NotificationService notificationService) {
    super(userEmailRepository, notificationService);
  }

  @Bean
  public Consumer<QuestCreatedMessage> questCreated() {
    return message -> {
      logger()
          .info("Received QuestCreated: questId={}, name={}", message.questId(), message.name());
      //      sendToAvatar(
      //          message.avatarId(),
      //          "New quest available!",
      //          "A new quest has been created: \"" + message.name() + "\". Join now to
      // participate!");
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
      sendToAvatar(
          message.avatarId(),
          "Quest completed!",
          "Congratulations! You completed the quest \"" + message.questId() + "\".");
    };
  }

  @Bean
  public Consumer<QuestNotCompletedMessage> questNotCompleted() {
    return message -> {
      logger().info("Received QuestNotCompleted:");
      //      sendToAvatar(
      //          message.avatarId(),
      //          "Quest not completed",
      //          "Unfortunately you did not complete the quest in time. Try again next time!");
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
      sendToAvatar(
          message.avatarId(),
          "You joined a quest!",
          "You've joined the quest \"" + message.questId() + "\". Good luck!");
    };
  }

  @Bean
  public Consumer<QuestLeftMessage> questLeft() {
    return message -> {
      logger()
          .info(
              "Received QuestLeft: questId={}, avatarId={}", message.questId(), message.avatarId());
      sendToAvatar(
          message.avatarId(),
          "You left the quest",
          "You left the quest \"" + message.questId() + "\".");
    };
  }

  public record QuestCreatedMessage(
      String questId, String avatarId, String name, Instant occurredOn) {}

  public record QuestCompletedMessage(String questId, String avatarId, Instant occurredOn) {}

  public record QuestNotCompletedMessage(String avatarId, Instant occurredOn) {}

  public record QuestJoinedMessage(String questId, String avatarId, Instant occurredOn) {}

  public record QuestLeftMessage(String questId, String avatarId, Instant occurredOn) {}
}
