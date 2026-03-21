package habitquest.notification.infrastructure.consumers;

import habitquest.notification.infrastructure.notification.NotificationService;
import habitquest.notification.infrastructure.repository.UserEmailRepository;

public abstract class AvatarAwareEventConsumer implements EventConsumer {

  protected final UserEmailRepository userEmailRepository;
  protected final NotificationService notificationService;

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
            () ->
                logger()
                    .warn("Nessuna email trovata per avatarId={}, notifica non inviata", avatarId));
  }
}
