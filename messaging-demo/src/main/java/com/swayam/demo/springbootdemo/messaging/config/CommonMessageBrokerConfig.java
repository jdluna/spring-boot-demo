package com.swayam.demo.springbootdemo.messaging.config;

import javax.jms.ConnectionFactory;
import javax.jms.MessageListener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.springframework.jms.listener.MessageListenerContainer;
import org.springframework.jms.support.destination.DestinationResolver;
import org.springframework.jms.support.destination.DynamicDestinationResolver;

@Configuration
public class CommonMessageBrokerConfig {

	public static final String BANK_DATA_QUEUE_NAME = "app.config.message.queue.bank-data";

	@Autowired
	private Environment environment;

	@Bean
	public DefaultJmsListenerContainerFactory jmsListenerContainerFactory(ConnectionFactory connectionFactory,
			DestinationResolver destinationResolver) {
		DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
		factory.setConnectionFactory(connectionFactory);
		factory.setDestinationResolver(destinationResolver);
		factory.setConcurrency("3-10");
		return factory;
	}

	@Bean
	public DestinationResolver destinationResolver() {
		return new DynamicDestinationResolver();
	}

	@Bean
	public MessageListenerContainer defaultMessageListenerContainer(ConnectionFactory connectionFactory,
			MessageListener messageListener) {
		DefaultMessageListenerContainer defaultMessageListenerContainer = new DefaultMessageListenerContainer();
		defaultMessageListenerContainer.setConnectionFactory(connectionFactory);
		defaultMessageListenerContainer.setDestinationName(environment.getProperty(BANK_DATA_QUEUE_NAME));
		defaultMessageListenerContainer.setMessageListener(messageListener);
		return defaultMessageListenerContainer;
	}

	@Bean
	public JmsTemplate jmsTemplate(ConnectionFactory connectionFactory) {
		JmsTemplate jmsTemplate = new JmsTemplate();
		jmsTemplate.setConnectionFactory(connectionFactory);
		return jmsTemplate;
	}

}
