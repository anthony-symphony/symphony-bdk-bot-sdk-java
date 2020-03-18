package com.symphony.bdk.bot.sdk.elements;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.symphony.bdk.bot.sdk.command.CommandDispatcher;
import com.symphony.bdk.bot.sdk.command.CommandFilter;
import com.symphony.bdk.bot.sdk.command.model.BotCommand;
import com.symphony.bdk.bot.sdk.elements.ElementsHandler;
import com.symphony.bdk.bot.sdk.event.EventDispatcher;
import com.symphony.bdk.bot.sdk.event.model.MessageEvent;
import com.symphony.bdk.bot.sdk.event.model.SymphonyElementsEvent;
import com.symphony.bdk.bot.sdk.feature.FeatureManager;
import com.symphony.bdk.bot.sdk.symphony.MessageClientImpl;
import com.symphony.bdk.bot.sdk.symphony.UsersClient;
import com.symphony.bdk.bot.sdk.symphony.model.SymphonyMessage;

@ExtendWith(MockitoExtension.class)
public class ElementsHandlerTest {

  @Mock
  private EventDispatcher eventDispatcher;

  @Mock
  private CommandDispatcher commandDispatcher;

  @Mock
  private CommandFilter commandFilter;

  @Mock
  private MessageClientImpl messageClient;

  @Mock
  private FeatureManager featureManager;

  @Mock
  private UsersClient usersClient;

  @InjectMocks
  private TestElementsHandler elementsHandler;

  static class TestElementsHandler extends ElementsHandler {

    private BiConsumer<BotCommand, SymphonyMessage> internalDisplayElements;
    private BiConsumer<SymphonyElementsEvent, SymphonyMessage> internalHandleAction;
    private Predicate<String> commandMatcher;

    @Override
    protected Predicate<String> getCommandMatcher() {
      if (commandMatcher == null) {
        commandMatcher = Pattern
            .compile("^@BotName /test$")
            .asPredicate();
      }
      return commandMatcher;
    }

    @Override
    protected String getElementsFormId() {
      return "test-form-id";
    }

    @Override
    public void displayElements(BotCommand command,
        SymphonyMessage elementsResponse) {
      if (internalDisplayElements != null) {
        internalDisplayElements.accept(command, elementsResponse);
      }
    }

    @Override
    public void handleAction(SymphonyElementsEvent event,
        SymphonyMessage elementsResponse) {
      if (internalHandleAction != null) {
        internalHandleAction.accept(event, elementsResponse);
      }
    }

    // Helper to ease changing the behavior of displayElements method on each test
    private void setInternalDisplayElements(
        BiConsumer<BotCommand, SymphonyMessage> consumer) {
      this.internalDisplayElements = consumer;
    }

    // Helper to ease changing the behavior of handleAction method on each test
    private void setInternalHandleAction(
        BiConsumer<SymphonyElementsEvent, SymphonyMessage> consumer) {
      this.internalHandleAction = consumer;
    }
  }

  @Test
  public void onCommandTest() {
    ElementsHandler spyElementsHandler = spy(elementsHandler);
    BotCommand command = mock(BotCommand.class);

    spyElementsHandler.onCommand(command);

    verify(spyElementsHandler, times(1))
      .displayElements(eq(command), any(SymphonyMessage.class));
  }

  @Test
  public void onCommandGetCommandMessageTest() {
    elementsHandler.setInternalDisplayElements((cmd, msg) -> cmd.getMessageEvent());
    BotCommand command = mock(BotCommand.class);

    elementsHandler.onCommand(command);

    verify(command, times(2)).getMessageEvent();
    verify(messageClient, never())
        ._sendMessage(anyString(), any(SymphonyMessage.class));

  }

  @Test
  public void onCommandGetBotNameTest() {
    elementsHandler.setInternalDisplayElements(
        (cmd, msg) -> elementsHandler.getBotName());
    ElementsHandler spyElementsHandler = spy(elementsHandler);
    BotCommand command = mock(BotCommand.class);

    spyElementsHandler.onCommand(command);

    verify(usersClient, times(1)).getBotDisplayName();
    verify(messageClient, never())
      ._sendMessage(anyString(), any(SymphonyMessage.class));
  }

  @Test
  public void onCommandSendResponseMessageTest() {
    elementsHandler.setInternalDisplayElements(
        (cmd, msg) -> msg.setMessage("symphony elements form"));
    BotCommand command = mock(BotCommand.class);
    MessageEvent message = mock(MessageEvent.class);
    when(message.getStreamId()).thenReturn("12345");
    when(command.getMessageEvent()).thenReturn(message);

    elementsHandler.onCommand(command);

    verify(messageClient, times(1))
        ._sendMessage(eq("12345"), any(SymphonyMessage.class));
  }

  @Test
  public void onCommandProcessingErrorFeedbackDisabledTest() {
    ElementsHandler spyElementsHandler = spy(elementsHandler);
    BotCommand command = mock(BotCommand.class);
    doThrow(new RuntimeException())
      .when(spyElementsHandler)
      .displayElements(eq(command), any(SymphonyMessage.class));
    when(featureManager.unexpectedErrorResponse()).thenReturn(null);

    spyElementsHandler.onCommand(command);

    verify(featureManager, times(1)).unexpectedErrorResponse();
    verify(messageClient, never())
      ._sendMessage(any(String.class), any(SymphonyMessage.class));
  }

  @Test
  public void onCommandProcessingErrorWithFeedbackTest() {
    ElementsHandler spyElementsHandler = spy(elementsHandler);
    BotCommand command = mock(BotCommand.class);
    doThrow(new RuntimeException())
        .when(spyElementsHandler)
        .displayElements(eq(command), any(SymphonyMessage.class));
    when(featureManager.unexpectedErrorResponse())
        .thenReturn("some error message");
    MessageEvent message = mock(MessageEvent.class);
    when(message.getStreamId()).thenReturn("STREAM_ID_1234");
    when(command.getMessageEvent()).thenReturn(message);

    spyElementsHandler.onCommand(command);

    verify(featureManager, times(2)).unexpectedErrorResponse();
    verify(messageClient, times(1))
        ._sendMessage(eq("STREAM_ID_1234"), any(SymphonyMessage.class));
  }

  @Test
  public void onEventTest() {
    ElementsHandler spyElementsHandler = spy(elementsHandler);
    SymphonyElementsEvent event = mock(SymphonyElementsEvent.class);

    spyElementsHandler.onEvent(event);

    verify(spyElementsHandler, times(1))
      .handleAction(eq(event), any(SymphonyMessage.class));
  }

  @Test
  public void onEventReadEvtDetailsTest() {
    elementsHandler.setInternalHandleAction((evt, msg) -> evt.getFormId());
    SymphonyElementsEvent event = mock(SymphonyElementsEvent.class);

    elementsHandler.onEvent(event);

    verify(event, times(2)).getFormId();
  }

  @Test
  public void onEventFeedbackDisabledTest() {
    elementsHandler.setInternalHandleAction(
        (cmd, msg) -> msg.setMessage("some response message"));
    SymphonyElementsEvent event = mock(SymphonyElementsEvent.class);
    when(featureManager.isCommandFeedbackEnabled()).thenReturn(false);

    elementsHandler.onEvent(event);

    verify(featureManager, times(1)).isCommandFeedbackEnabled();
    verify(messageClient, never())
      ._sendMessage(any(String.class), any(SymphonyMessage.class));
  }

  @Test
  public void onEventSendResponseMessageTest() {
    elementsHandler.setInternalHandleAction(
        (cmd, msg) -> msg.setMessage("some response message"));
    SymphonyElementsEvent event = mock(SymphonyElementsEvent.class);
    when(event.getStreamId()).thenReturn("STREAM_ID_1234");
    when(featureManager.isCommandFeedbackEnabled()).thenReturn(true);

    elementsHandler.onEvent(event);

    verify(featureManager, times(1)).isCommandFeedbackEnabled();
    verify(messageClient, times(1))
      ._sendMessage(eq("STREAM_ID_1234"), any(SymphonyMessage.class));
  }

  @Test
  public void onEventProcessingErrorFeedbackDisabledTest() {
    ElementsHandler spyElementsHandler = spy(elementsHandler);
    SymphonyElementsEvent event = mock(SymphonyElementsEvent.class);
    doThrow(new RuntimeException())
      .when(spyElementsHandler)
      .handleAction(any(SymphonyElementsEvent.class), any(SymphonyMessage.class));
    when(featureManager.unexpectedErrorResponse())
      .thenReturn(null);


    spyElementsHandler.onEvent(event);

    verify(messageClient, never())
      ._sendMessage(any(String.class), any(SymphonyMessage.class));
  }

  @Test
  public void onEventProcessingErrorFeedbackEnabledTest() {
    ElementsHandler spyElementsHandler = spy(elementsHandler);
    SymphonyElementsEvent event = mock(SymphonyElementsEvent.class);
    doThrow(new RuntimeException())
      .when(spyElementsHandler)
      .handleAction(any(SymphonyElementsEvent.class), any(SymphonyMessage.class));
    when(featureManager.unexpectedErrorResponse())
      .thenReturn("some error message");
    when(event.getStreamId()).thenReturn("STREAM_ID_1234");


    spyElementsHandler.onEvent(event);

    verify(messageClient, times(1))
      ._sendMessage(eq("STREAM_ID_1234"), any(SymphonyMessage.class));
  }

}
