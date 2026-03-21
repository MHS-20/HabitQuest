package habitquest.notification.infrastructure.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailNotificationService implements NotificationService {

  private static final Logger log = LoggerFactory.getLogger(EmailNotificationService.class);

  private final JavaMailSender mailSender;

  public EmailNotificationService(JavaMailSender mailSender) {
    this.mailSender = mailSender;
  }

  @Override
  public void send(String to, String subject, String body) {
    try {
      SimpleMailMessage message = new SimpleMailMessage();
      message.setTo(to);
      message.setSubject(subject);
      message.setText(body);
      message.setFrom("noreply@habitquest.it");
      mailSender.send(message);
      log.info("Email inviata a {}: {}", to, subject);
    } catch (Exception e) {
      log.error("Errore nell'invio dell'email a {}: {}", to, e.getMessage(), e);
      throw new RuntimeException("Errore nell'invio dell'email a " + to, e);
    }
  }
}
