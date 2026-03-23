package habitquest.notification.infrastructure.consumers;

import habitquest.notification.infrastructure.notification.NotificationService;
import habitquest.notification.infrastructure.repository.UserEmailRepository;

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

  public UserEmailRepository getUserEmailRepository() {
    return userEmailRepository;
  }

  public NotificationService getNotificationService() {
    return notificationService;
  }
}
