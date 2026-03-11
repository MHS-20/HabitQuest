package habitquest.avatar.infrastructure;

import common.hexagonal.Adapter;
import habitquest.avatar.application.AvatarRepository;
import habitquest.avatar.domain.avatar.Avatar;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
@Adapter
public class InMemoryAvatarRepository implements AvatarRepository {
  private final Map<String, Avatar> store = new HashMap<>();

  @Override
  public Avatar save(Avatar avatar) {
    store.put(avatar.getId(), avatar);
    return avatar;
  }

  @Override
  public Optional<Avatar> findById(String id) {
    return Optional.ofNullable(store.get(id));
  }

  @Override
  public void deleteById(String id) {
    store.remove(id);
  }
}
