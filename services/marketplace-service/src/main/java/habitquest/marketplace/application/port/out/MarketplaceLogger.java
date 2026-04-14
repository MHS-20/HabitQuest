package habitquest.marketplace.application.port.out;

import common.hexagonal.OutBoundPort;

@OutBoundPort
public interface MarketplaceLogger {
  void info(Object domainObject, String message);

  void warn(Object domainObject, String message);

  void error(Object domainObject, String message, Throwable cause);
}
