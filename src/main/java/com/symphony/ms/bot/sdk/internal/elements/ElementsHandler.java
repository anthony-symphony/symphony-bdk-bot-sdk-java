package com.symphony.ms.bot.sdk.internal.elements;

import java.util.function.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.symphony.ms.bot.sdk.internal.command.BaseCommandHandler;
import com.symphony.ms.bot.sdk.internal.command.CommandDispatcher;
import com.symphony.ms.bot.sdk.internal.command.CommandFilter;
import com.symphony.ms.bot.sdk.internal.command.model.BotCommand;
import com.symphony.ms.bot.sdk.internal.event.BaseEventHandler;
import com.symphony.ms.bot.sdk.internal.event.EventDispatcher;
import com.symphony.ms.bot.sdk.internal.event.model.SymphonyElementsEvent;
import com.symphony.ms.bot.sdk.internal.feature.FeatureManager;
import com.symphony.ms.bot.sdk.internal.message.MessageService;
import com.symphony.ms.bot.sdk.internal.message.model.SymphonyMessage;
import com.symphony.ms.bot.sdk.internal.symphony.UsersClient;

/**
 * Symphony Elements Handler
 * <p>
 * Offers all necessary support to handle Symphony elements, from the command to display the
 * Symphony elements in a chat room to the callback triggered when the Symphony elements form is
 * submitted.
 *
 * @author Marcus Secato
 */
public abstract class ElementsHandler implements
    BaseCommandHandler, BaseEventHandler<SymphonyElementsEvent> {
  private static final Logger LOGGER = LoggerFactory.getLogger(ElementsHandler.class);

  private EventDispatcher eventDispatcher;

  private CommandDispatcher commandDispatcher;

  private CommandFilter commandFilter;

  private MessageService messageService;

  private FeatureManager featureManager;

  private UsersClient usersClient;

  /**
   * Registers the ElementsHandler to {@link CommandFilter}, {@link CommandDispatcher} and {@link
   * EventDispatcher}.
   */
  public void register() {
    init();
    commandDispatcher.register(getCommandName(), this);
    commandFilter.addFilter(getCommandName(), getCommandMatcher());
    eventDispatcher.register(getElementsFormId(), this);
  }

  /**
   * Initializes the EventHandler dependencies. This method can be overridden by the child classes
   * if the developers want to implement initialization logic.
   */
  protected void init() {
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void onCommand(BotCommand command) {
    LOGGER.debug("Received command to display elements form {}",
        command.getMessage());

    final SymphonyMessage elementsResponse = new SymphonyMessage();
    try {
      displayElements(command, elementsResponse);

      if (elementsResponse.hasContent()) {
        messageService.sendMessage(command.getStreamId(), elementsResponse);
      }

    } catch (Exception e) {
      LOGGER.error("Error processing command {}\n{}", getCommandName(), e);
      if (featureManager.unexpectedErrorResponse() != null) {
        messageService.sendMessage(command.getStreamId(),
            new SymphonyMessage(featureManager.unexpectedErrorResponse()));
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void onEvent(SymphonyElementsEvent event) {
    LOGGER.debug("Received action for elements form: {}", event.getFormId());

    final SymphonyMessage eventResponse = new SymphonyMessage();
    try {
      handleAction(event, eventResponse);

      if (eventResponse.hasContent()
          && featureManager.isCommandFeedbackEnabled()) {
        messageService.sendMessage(event.getStreamId(), eventResponse);
      }

    } catch (Exception e) {
      LOGGER.error("Error processing elements action {}", e);
      if (featureManager.unexpectedErrorResponse() != null) {
        messageService.sendMessage(event.getStreamId(),
            new SymphonyMessage(featureManager.unexpectedErrorResponse()));
      }
    }
  }

  protected String getCommandName() {
    return this.getClass().getCanonicalName();
  }

  protected String getBotName() {
    return usersClient.getBotDisplayName();
  }

  /**
   * Returns the pattern used by {@link CommandFilter} to filter out bot commands.
   *
   * @return the matcher object
   */
  protected abstract Predicate<String> getCommandMatcher();

  /**
   * Specifies which Symphony elements form this handler should listen events for.
   *
   * @return the Symphony elements formId
   */
  protected abstract String getElementsFormId();

  /**
   * Displays the Symphony elements form
   *
   * @param command
   * @param elementsResponse
   */
  public abstract void displayElements(BotCommand command,
      final SymphonyMessage elementsResponse);

  /**
   * Handle the action triggered when Symphony elements form is submitted
   *
   * @param event
   * @param elementsResponse
   */
  public abstract void handleAction(SymphonyElementsEvent event,
      final SymphonyMessage elementsResponse);

  public void setEventDispatcher(EventDispatcher eventDispatcher) {
    this.eventDispatcher = eventDispatcher;
  }

  public void setCommandDispatcher(CommandDispatcher commandDispatcher) {
    this.commandDispatcher = commandDispatcher;
  }

  public void setCommandFilter(CommandFilter commandFilter) {
    this.commandFilter = commandFilter;
  }

  public void setMessageService(MessageService messageService) {
    this.messageService = messageService;
  }

  public void setFeatureManager(FeatureManager featureManager) {
    this.featureManager = featureManager;
  }

  public void setUsersClient(UsersClient usersClient) {
    this.usersClient = usersClient;
  }

}