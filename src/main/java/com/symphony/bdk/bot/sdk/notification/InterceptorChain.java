package com.symphony.bdk.bot.sdk.notification;

import com.symphony.bdk.bot.sdk.notification.model.NotificationRequest;
import com.symphony.bdk.bot.sdk.symphony.model.SymphonyMessage;

/**
 * Mechanism to chain multiple {@link NotificationInterceptor} together and
 * iterate over them.
 *
 * @author Marcus Secato
 *
 */
public interface InterceptorChain {

  /**
   * Registers a {@link NotificationInterceptor} to process incoming requests
   *
   * @param notificationInterceptor
   */
  void register(NotificationInterceptor notificationInterceptor);

  /**
   * Registers a {@link NotificationInterceptor} in a specific order
   *
   * @param index
   * @param notificationInterceptor
   */
  void register(int index, NotificationInterceptor notificationInterceptor);

  /**
   * Iterates over all registered interceptors retrieving the result once all
   * interceptors are done.
   *
   * @param notificationRequest
   * @param notificationMessage
   * @return true if all interceptors allowed the request to proceed, false if
   *         request should be discarded.
   */
  boolean execute(NotificationRequest notificationRequest,
      final SymphonyMessage notificationMessage);

}
