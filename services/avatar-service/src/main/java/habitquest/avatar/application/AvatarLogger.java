package habitquest.avatar.application;

import common.hexagonal.OutBoundPort;

@OutBoundPort
public interface AvatarLogger {
  void info(Object domainObject, String message);

  void warn(Object domainObject, String message);

  void error(Object domainObject, String message, Throwable cause);
}
