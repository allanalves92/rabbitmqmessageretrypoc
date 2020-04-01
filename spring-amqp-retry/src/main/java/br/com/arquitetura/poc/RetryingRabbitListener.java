package br.com.arquitetura.poc;

import static br.com.arquitetura.poc.config.RabbitConfiguration.PRIMARY_QUEUE;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class RetryingRabbitListener {

	private Logger logger = LoggerFactory.getLogger(RetryingRabbitListener.class);

	@RabbitListener(queues = PRIMARY_QUEUE)
	public void primary(Message message) throws Exception {
		logger.info("Message read from testq : " + message);
		throw new AmqpRejectAndDontRequeueException("There was an error");
	}

}
