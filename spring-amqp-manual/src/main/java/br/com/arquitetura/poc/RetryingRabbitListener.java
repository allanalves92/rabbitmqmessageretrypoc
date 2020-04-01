package br.com.arquitetura.poc;

import static br.com.arquitetura.poc.config.RabbitConfiguration.PARKINGLOT_QUEUE;
import static br.com.arquitetura.poc.config.RabbitConfiguration.PRIMARY_QUEUE;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class RetryingRabbitListener {

	private Logger logger = LoggerFactory.getLogger(RetryingRabbitListener.class);

	private RabbitTemplate rabbitTemplate;

	private static final Integer MAX_RETRIES = 3;

	public RetryingRabbitListener(RabbitTemplate rabbitTemplate) {
		this.rabbitTemplate = rabbitTemplate;
	}

	@RabbitListener(queues = PRIMARY_QUEUE)
	public void getMessage(Message message) throws Exception {
		
		logger.info("Message read from workerQueue : " + message);

		try {
			throw new Exception("There was an error");
		} catch (Exception e) {
			if (hasExceededRetryCount(message)) {
				putIntoParkingLot(message);
				return;
			}

			throw new AmqpRejectAndDontRequeueException("There was an error");
		}
	}

	private boolean hasExceededRetryCount(Message message) {
		List<Map<String, ?>> xDeathHeader = message.getMessageProperties().getXDeathHeader();

		if (xDeathHeader != null && xDeathHeader.size() >= 1) {
			Long count = (Long) xDeathHeader.get(0).get("count");
			return count >= MAX_RETRIES;
		}

		return false;
	}

	private void putIntoParkingLot(Message failedMessage) {
		logger.info("Retries exeeded putting into parking lot");
		this.rabbitTemplate.send(PARKINGLOT_QUEUE, failedMessage);
	}
}
