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
          .info(
              "Received QuestCreated: questId={}, name={}, avatarId={}",
              message.questId(),
              message.name(),
              message.avatarId());
      sendToAvatar(
          message.avatarId(),
          "Nuova quest disponibile!",
          "È stata creata una nuova quest: \""
              + message.name()
              + "\". Unisciti subito per partecipare!");
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
          "Quest completata!",
          "Congratulazioni! Hai completato la quest \"" + message.questId() + "\".");
    };
  }

  @Bean
  public Consumer<QuestNotCompletedMessage> questNotCompleted() {
    return message -> {
      logger().info("Received QuestNotCompleted: avatarId={}", message.avatarId());
      sendToAvatar(
          message.avatarId(),
          "Quest non completata",
          "Purtroppo non hai completato la quest in tempo. Riprova con la prossima!");
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
          "Sei entrato in una quest!",
          "Ti sei unito alla quest \"" + message.questId() + "\". Buona fortuna!");
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
          "Hai abbandonato la quest",
          "Hai abbandonato la quest \"" + message.questId() + "\".");
    };
  }

  public record QuestCreatedMessage(
      String questId, String avatarId, String name, Instant occurredOn) {}

  public record QuestCompletedMessage(String questId, String avatarId, Instant occurredOn) {}

  public record QuestNotCompletedMessage(String avatarId, Instant occurredOn) {}

  public record QuestJoinedMessage(String questId, String avatarId, Instant occurredOn) {}

  public record QuestLeftMessage(String questId, String avatarId, Instant occurredOn) {}
}
