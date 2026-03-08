package habitquest.guild.domain.factory;

import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class UUIDGenerator implements IdGenerator {
  @Override
  public String nextId() {
    return UUID.randomUUID().toString();
  }
}
