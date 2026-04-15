package habitquest.avatar.infrastructure.outbound;

import common.ddd.Id;
import common.hexagonal.Adapter;
import habitquest.avatar.application.port.out.AvatarRepository;
import habitquest.avatar.application.service.AvatarSearchQuery;
import habitquest.avatar.domain.avatar.Avatar;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
@Adapter
public class InMemoryAvatarRepository implements AvatarRepository {
  private final Map<Id<Avatar>, Avatar> store = new ConcurrentHashMap<>();

  @Override
  public Avatar save(Avatar avatar) {
    store.put(avatar.getId(), avatar);
    return avatar;
  }

  @Override
  public Optional<Avatar> findById(Id<Avatar> id) {
    return Optional.ofNullable(store.get(id));
  }

  @Override
  public void deleteById(Id<Avatar> id) {
    store.remove(id);
  }

  @Override
  public List<Avatar> search(AvatarSearchQuery criteria) {
    if (criteria == null) {
      return new ArrayList<>(store.values());
    }

    String nameCrit = criteria.name();
    Integer minLevel = criteria.minLevel();
    Integer maxLevel = criteria.maxLevel();

    return store.values().stream()
        .filter(
            avatar ->
                nameCrit == null
                    || nameCrit.isBlank()
                    || (avatar.getName() != null
                        && avatar
                            .getName()
                            .toLowerCase(Locale.getDefault())
                            .contains(nameCrit.toLowerCase(Locale.getDefault()))))
        .filter(
            avatar ->
                minLevel == null
                    || (avatar.getLevel() != null && avatar.getLevel().levelNumber() >= minLevel))
        .filter(
            avatar ->
                maxLevel == null
                    || (avatar.getLevel() != null && avatar.getLevel().levelNumber() <= maxLevel))
        .collect(Collectors.toList());
  }
}
