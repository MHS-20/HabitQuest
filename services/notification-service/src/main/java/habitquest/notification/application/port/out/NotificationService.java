package habitquest.notification.application.port.out;

public interface NotificationService {
  void send(String to, String subject, String body);
}
