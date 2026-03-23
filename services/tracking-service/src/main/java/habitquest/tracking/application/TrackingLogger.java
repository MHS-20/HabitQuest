package habitquest.tracking.application;

import common.hexagonal.OutBoundPort;

@OutBoundPort
public interface TrackingLogger {
  void info(Object domainObject, String message);

  void warn(Object domainObject, String message);

  void error(Object domainObject, String message, Throwable cause);
}
