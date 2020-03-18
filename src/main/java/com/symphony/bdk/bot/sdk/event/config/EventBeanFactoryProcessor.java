package com.symphony.bdk.bot.sdk.event.config;

import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.stereotype.Component;

import com.symphony.bdk.bot.sdk.event.EventHandler;
import com.symphony.bdk.bot.sdk.scan.BaseBeanFactoryPostProcessor;

/**
 * Automatically scans for {@link EventHandler}, instantiates them, injects
 * all dependencies and registers to Spring bean registry.
 *
 * @author Marcus Secato
 *
 */
@Component
public class EventBeanFactoryProcessor extends BaseBeanFactoryPostProcessor {
  private static final Logger LOGGER = LoggerFactory.getLogger(EventBeanFactoryProcessor.class);

  @Override
  public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
    final BeanDefinitionRegistry beanDefinitionRegistry = (BeanDefinitionRegistry) beanFactory;
    Set<BeanDefinition> beanDefinitionSet = scanComponents(EventHandler.class);
    LOGGER.info("Scanning for event handlers found {} beans", beanDefinitionSet.size());

    for (BeanDefinition beanDefinition : beanDefinitionSet) {
      BeanDefinition eventHandler = BeanDefinitionBuilder
          .rootBeanDefinition(beanDefinition.getBeanClassName())
          .setInitMethodName("register")
          .addPropertyReference("eventDispatcher", "eventDispatcherImpl")
          .addPropertyReference("messageClient", "messageClientImpl")
          .addPropertyReference("featureManager", "featureManager")
          .getBeanDefinition();

      beanDefinitionRegistry.registerBeanDefinition(
          beanDefinition.getBeanClassName(), eventHandler);
    }
  }

}
