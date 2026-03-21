package habitquest.notification.infrastructure.consumers;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.mail.internet.MimeMessage;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MarketplaceEventConsumerTest extends BaseConsumerIntegrationTest {

  @BeforeEach
  void setUp() {
    resetGreenMail();
    userEmailRepository.save("avatar-1", "mario@example.com");
  }

  @Test
  void whenItemBought_thenEmailSentWithItemName() throws Exception {
    publish(
        "marketplace.item-bought",
        new MarketplaceEventConsumer.ItemBoughtMessage(
            "mkt-1", "Spada del Destino", "avatar-1", Instant.now()));

    MimeMessage[] mails = waitForEmails(1);

    assertThat(recipientOf(mails[0])).isEqualTo("mario@example.com");
    assertThat(subjectOf(mails[0])).isEqualTo("Acquisto completato!");
    assertThat(bodyOf(mails[0])).contains("Spada del Destino");
  }

  @Test
  void whenItemSold_thenEmailSentWithItemName() throws Exception {
    publish(
        "marketplace.item-sold",
        new MarketplaceEventConsumer.ItemSoldMessage(
            "mkt-1", "Scudo Antico", "avatar-1", Instant.now()));

    MimeMessage[] mails = waitForEmails(1);

    assertThat(recipientOf(mails[0])).isEqualTo("mario@example.com");
    assertThat(subjectOf(mails[0])).isEqualTo("Oggetto venduto!");
    assertThat(bodyOf(mails[0])).contains("Scudo Antico");
  }
}
