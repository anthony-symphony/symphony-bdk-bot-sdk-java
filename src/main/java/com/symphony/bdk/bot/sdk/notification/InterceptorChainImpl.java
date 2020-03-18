package com.symphony.bdk.bot.sdk.notification;

import java.util.LinkedList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.symphony.bdk.bot.sdk.notification.model.NotificationRequest;
import com.symphony.bdk.bot.sdk.symphony.model.SymphonyMessage;

@Service
public class InterceptorChainImpl implements InterceptorChain {
  private static final Logger LOGGER = LoggerFactory.getLogger(InterceptorChainImpl.class);

  private List<NotificationInterceptor> interceptorList = new LinkedList<>();

  /**
   * {@inheritDoc}
   */
  @Override
  public void register(NotificationInterceptor notificationInterceptor) {
    interceptorList.add(notificationInterceptor);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void register(int index,
      NotificationInterceptor notificationInterceptor) {
    interceptorList.add(index, notificationInterceptor);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean execute(NotificationRequest notificationRequest,
      final SymphonyMessage notificationMessage) {
    LOGGER.debug("Going through notification interceptors chain");

    for (NotificationInterceptor interceptor : interceptorList) {
      if (!interceptor.intercept(notificationRequest, notificationMessage)) {
        LOGGER.debug("Discarding notification request {}",
            notificationRequest.getIdentifier());
        return false;
      }
    }

    return true;
  }

}
