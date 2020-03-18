package com.symphony.bdk.bot.sdk.elements;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.symphony.bdk.bot.sdk.elements.ElementsActionHandler;
import com.symphony.bdk.bot.sdk.event.EventDispatcher;
import com.symphony.bdk.bot.sdk.event.model.SymphonyElementsEvent;
import com.symphony.bdk.bot.sdk.feature.FeatureManager;
import com.symphony.bdk.bot.sdk.symphony.MessageClientImpl;
import com.symphony.bdk.bot.sdk.symphony.model.SymphonyMessage;

@ExtendWith(MockitoExtension.class)
public class ElementsActionHandlerTest {

  @Mock
  private EventDispatcher eventDispatcher;

  @Mock
  private MessageClientImpl messageClient;

  @Mock
  private FeatureManager featureManager;

  @InjectMocks
  private TestElementsActionHandler elementsHandler;

  static class TestElementsActionHandler extends ElementsActionHandler {
    @Override
    protected String getElementsFormId() {
      return "test-form-id";
    }

    @Override
    public void handle(SymphonyElementsEvent event, SymphonyMessage eventResponse) {
    }
  }

  @Test
  public void onEventTest() {
    ElementsActionHandler spyElementsHandler = spy(elementsHandler);
    SymphonyElementsEvent event = mock(SymphonyElementsEvent.class);

    spyElementsHandler.onEvent(event);

    verify(spyElementsHandler, times(1))
      .handle(eq(event), any(SymphonyMessage.class));
  }

}
