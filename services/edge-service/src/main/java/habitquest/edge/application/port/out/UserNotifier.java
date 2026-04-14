package habitquest.edge.application.port.out;

import common.hexagonal.OutBoundPort;
import habitquest.edge.domain.User;

@OutBoundPort
public interface UserNotifier {
  void notifyUserRegistered(User user);
}
