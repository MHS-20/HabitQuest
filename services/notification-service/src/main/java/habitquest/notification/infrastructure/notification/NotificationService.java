package habitquest.notification.infrastructure.notification;

public interface NotificationService {
  void send(String to, String subject, String body);
}
