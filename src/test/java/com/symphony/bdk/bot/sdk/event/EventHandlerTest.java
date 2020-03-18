package com.symphony.bdk.bot.sdk.event;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.function.BiConsumer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.symphony.bdk.bot.sdk.event.EventDispatcher;
import com.symphony.bdk.bot.sdk.event.EventHandler;
import com.symphony.bdk.bot.sdk.event.model.BaseEvent;
import com.symphony.bdk.bot.sdk.feature.FeatureManager;
import com.symphony.bdk.bot.sdk.symphony.MessageClientImpl;
import com.symphony.bdk.bot.sdk.symphony.model.SymphonyMessage;

@ExtendWith(MockitoExtension.class)
public class EventHandlerTest {

  @Mock
  private EventDispatcher eventDispatcher;

  @Mock
  private MessageClientImpl messageClient;

  @Mock
  private FeatureManager featureManager;

  @InjectMocks
  private TestEventHandler eventHandler;

  static class TestEventHandler extends EventHandler<BaseEvent> {

    private BiConsumer<BaseEvent, SymphonyMessage> internalHandle;

    @Override
    public void handle(BaseEvent event, SymphonyMessage eventResponse) {
      if (internalHandle != null) {
        internalHandle.accept(event, eventResponse);
      }
    }

    // Helper to ease changing the behavior of handle method on each test
    private void setInternalHandle(BiConsumer<BaseEvent, SymphonyMessage> consumer) {
      this.internalHandle = consumer;
    }
  }

  @Test
  public void onEventTest() {
    EventHandler<BaseEvent> spyEventHandler = spy(eventHandler);
    BaseEvent event = mock(BaseEvent.class);

    spyEventHandler.onEvent(event);

    verify(spyEventHandler, times(1))
      .handle(any(BaseEvent.class), any(SymphonyMessage.class));
  }

  @Test
  public void onEventReadEvtDetailsTest() {
    eventHandler.setInternalHandle((evt, msg) -> evt.getStreamId());
    BaseEvent event = mock(BaseEvent.class);

    eventHandler.onEvent(event);

    verify(event, times(2)).getStreamId();
  }

  @Test
  public void onEventFeedbackDisabledTest() {
    eventHandler.setInternalHandle(
        (cmd, msg) -> msg.setMessage("some response message"));
    BaseEvent event = mock(BaseEvent.class);
    when(featureManager.isCommandFeedbackEnabled()).thenReturn(false);

    eventHandler.onEvent(event);

    verify(featureManager, times(1)).isCommandFeedbackEnabled();
    verify(messageClient, never())
      ._sendMessage(any(String.class), any(SymphonyMessage.class));
  }

  @Test
  public void onEventSendResponseMessageTest() {
    eventHandler.setInternalHandle(
        (cmd, msg) -> msg.setMessage("some response message"));
    BaseEvent event = mock(BaseEvent.class);
    when(event.getStreamId()).thenReturn("STREAM_ID_1234");
    when(featureManager.isCommandFeedbackEnabled()).thenReturn(true);

    eventHandler.onEvent(event);

    verify(featureManager, times(1)).isCommandFeedbackEnabled();
    verify(messageClient, times(1))
      ._sendMessage(eq("STREAM_ID_1234"), any(SymphonyMessage.class));
  }

  @Test
  public void onEventProcessingErrorFeedbackDisabledTest() {
    EventHandler<BaseEvent> spyEventHandler = spy(eventHandler);
    BaseEvent event = mock(BaseEvent.class);
    doThrow(new RuntimeException())
      .when(spyEventHandler)
      .handle(any(BaseEvent.class), any(SymphonyMessage.class));

    spyEventHandler.onEvent(event);

    verify(messageClient, never())
      ._sendMessage(any(String.class), any(SymphonyMessage.class));
  }

}
