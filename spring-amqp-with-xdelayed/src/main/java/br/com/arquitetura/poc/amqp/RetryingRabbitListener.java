package br.com.arquitetura.poc.amqp;

import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class RetryingRabbitListener {

	private Logger logger = LoggerFactory.getLogger(RetryingRabbitListener.class);

	private RabbitTemplate rabbitTemplate;

	private static final String QUEUE_NAME = "delayed.queue";
	private static final String EXCHANGE_NAME = "delayed.exchange";
	public static final String PARKINGLOT_QUEUE = "delayed.parkingLot";
	private static final Integer MAX_RETRIES = 3;
	private static final String NUM_ATTEMPT_HEADER = "x-num-attempt";
	private static final long BASE_RETRY_DELAY = 5000;
	private static final String DELAY_HEADER = "x-delay";

	public RetryingRabbitListener(RabbitTemplate rabbitTemplate) {
		this.rabbitTemplate = rabbitTemplate;
	}

	@RabbitListener(queues = QUEUE_NAME)
	public void getMessage(Message message) throws Exception {

		String payload = new String(message.getBody(), StandardCharsets.UTF_8);
		Long numAttempt = (Long) message.getMessageProperties().getHeaders().getOrDefault(NUM_ATTEMPT_HEADER, 1L);

		logger.info("Message received, payload={}, attempt={}", payload, numAttempt);

		try {
			throw new Exception("There was an error");
		} catch (Exception e) {

			if (numAttempt <= MAX_RETRIES) {
				message.getMessageProperties().setHeader(DELAY_HEADER, numAttempt * BASE_RETRY_DELAY);
				message.getMessageProperties().setHeader(NUM_ATTEMPT_HEADER, numAttempt + 1);
				rabbitTemplate.send(EXCHANGE_NAME, QUEUE_NAME, message);
			} else {
				putIntoParkingLot(message);
			}
		}
	}

	private void putIntoParkingLot(Message failedMessage) {
		logger.info("Retries exeeded putting into parking lot");
		this.rabbitTemplate.send(PARKINGLOT_QUEUE, failedMessage);
	}
}
