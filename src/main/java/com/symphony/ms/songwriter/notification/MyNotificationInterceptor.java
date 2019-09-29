package com.symphony.ms.songwriter.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.symphony.ms.songwriter.internal.message.model.SymphonyMessage;
import com.symphony.ms.songwriter.internal.notification.NotificationInterceptor;
import com.symphony.ms.songwriter.internal.notification.model.NotificationRequest;

public class MyNotificationInterceptor extends NotificationInterceptor {
  private static final Logger LOGGER = LoggerFactory.getLogger(MyNotificationInterceptor.class);

  @Override
  public boolean process(NotificationRequest notificationRequest,
      SymphonyMessage notificationMessage) {

    // Webhook service not implemented yet - currently identifier == streamId
    String streamId = baseWebhookService.getStreamIdFromIdentifier(
        notificationRequest.getIdentifier());

    if (streamId != null) {
      notificationRequest.setStreamId(streamId);
      notificationMessage.setMessage(notificationRequest.getPayload());
      notificationMessage.setEnrichedMessage(
          "<b>Notification received:</b><br />" + notificationRequest.getPayload(), // Default message when extension app not present
          "MyEntity", // Root node in the payload received in extension app
          notificationRequest.getPayload(), // payload received in extension app
          "1.0"); // version
      return true; // true if notification interception chain should continue
    }

    return false; // false if notification interception chain should be stopped and notification request rejected
  }

}