package com.symphony.bdk.bot.sdk.command;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.symphony.bdk.bot.sdk.command.CommandDispatcher;
import com.symphony.bdk.bot.sdk.command.CommandFilterImpl;
import com.symphony.bdk.bot.sdk.command.model.BotCommand;
import com.symphony.bdk.bot.sdk.event.model.MessageEvent;

import java.util.regex.Pattern;

@ExtendWith(MockitoExtension.class)
public class CommandFilterTest {

  @Mock
  private CommandDispatcher commandDispatcher;

  @InjectMocks
  private CommandFilterImpl commandFilter;

  @Test
  public void filterNoMatcherTest() {
    MessageEvent messageEvent = mock(MessageEvent.class);
    when(messageEvent.getMessage()).thenReturn("some message");

    commandFilter.filter(messageEvent);

    verify(commandDispatcher, never()).push(anyString(), any(BotCommand.class));
  }

  @Test
  public void filterWithMatcherNotMatchTest() {
    MessageEvent messageEvent = mock(MessageEvent.class);
    when(messageEvent.getMessage()).thenReturn("some message");

    commandFilter.addFilter("testCommand", Pattern
        .compile("^@BotName /test$")
        .asPredicate());
    commandFilter.filter(messageEvent);

    verify(commandDispatcher, never()).push(anyString(), any(BotCommand.class));
  }

  @Test
  public void filterWithMatcherSuccessMatchTest() {
    MessageEvent messageEvent = mock(MessageEvent.class);
    when(messageEvent.getMessage()).thenReturn("@BotName /test");

    commandFilter.addFilter("testCommand", Pattern
        .compile("^@BotName /test$")
        .asPredicate());
    commandFilter.filter(messageEvent);

    verify(commandDispatcher, times(1)).push(eq("testCommand"), any(BotCommand.class));
  }

  @Test
  public void filterDefaultMatcherTest() {
    MessageEvent messageEvent = mock(MessageEvent.class);
    when(messageEvent.getMessage()).thenReturn("@BotName /wrong command");

    commandFilter.setDefaultFilter("defaultCommand", Pattern
        .compile("^@BotName")
        .asPredicate());

    commandFilter.filter(messageEvent);

    verify(commandDispatcher, times(1)).push(eq("defaultCommand"), any(BotCommand.class));
  }

  @Test
  public void filterDefaultMatcherFailTest() {
    MessageEvent messageEvent = mock(MessageEvent.class);
    when(messageEvent.getMessage()).thenReturn("@BotName /wrong command");

    commandFilter.setDefaultFilter("defaultCommand", Pattern
        .compile("^@BotName2")
        .asPredicate());

    commandFilter.filter(messageEvent);

    verify(commandDispatcher, never()).push(eq("defaultCommand"), any(BotCommand.class));
  }

}
