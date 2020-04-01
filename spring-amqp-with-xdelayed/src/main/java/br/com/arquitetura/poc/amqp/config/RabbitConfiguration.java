package br.com.arquitetura.poc.amqp.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfiguration {

	private static final String QUEUE_NAME = "delayed.queue";
	private static final String EXCHANGE_NAME = "delayed.exchange";
	public static final String PARKINGLOT_QUEUE = "delayed.parkingLot";

	@Bean
	Exchange exchange() {
		return ExchangeBuilder.directExchange(EXCHANGE_NAME).delayed().build();
	}

	@Bean
	Queue primaryQueue() {
		return QueueBuilder.durable(QUEUE_NAME).build();
	}

	@Bean
	Queue parkinglotQueue() {
		return new Queue(PARKINGLOT_QUEUE);
	}

	@Bean
	Binding binding(Queue primaryQueue, DirectExchange exchange) {
		return BindingBuilder.bind(primaryQueue).to(exchange).with(QUEUE_NAME);
	}

	@Bean
	Binding parkingBinding(Queue parkinglotQueue, DirectExchange exchange) {
		return BindingBuilder.bind(parkinglotQueue).to(exchange).with(PARKINGLOT_QUEUE);
	}

}
