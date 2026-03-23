package habitquest.edge.application;

import common.hexagonal.OutBoundPort;
import habitquest.edge.domain.User;

@OutBoundPort
public interface UserNotifier {
  void notifyUserRegistered(User user);
}
