package habitquest.notification.infrastructure.inbound.consumers.base;

import habitquest.notification.application.port.in.EventConsumer;
import habitquest.notification.application.port.out.NotificationService;
import habitquest.notification.application.port.out.UserEmailRepository;

public abstract class AvatarAwareEventConsumer implements EventConsumer {

  private final UserEmailRepository userEmailRepository;
  private final NotificationService notificationService;

  protected AvatarAwareEventConsumer(
      UserEmailRepository userEmailRepository, NotificationService notificationService) {
    this.userEmailRepository = userEmailRepository;
    this.notificationService = notificationService;
  }

  protected void sendToAvatar(String avatarId, String subject, String body) {
    userEmailRepository
        .findEmailByUserId(avatarId)
        .ifPresentOrElse(
            email -> notificationService.send(email, subject, body),
            () -> logger().warn("No email found for avatarId={}, notification not sent", avatarId));
  }

  protected void saveEmail(String avatarId, String email) {
    userEmailRepository.save(avatarId, email);
  }

  protected void sendNotification(String email, String subject, String body) {
    notificationService.send(email, subject, body);
  }
}
