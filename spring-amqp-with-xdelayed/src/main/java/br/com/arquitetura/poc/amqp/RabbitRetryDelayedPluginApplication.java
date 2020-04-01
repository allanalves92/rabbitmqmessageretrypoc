package br.com.arquitetura.poc.amqp;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class RabbitRetryDelayedPluginApplication implements CommandLineRunner {

	public static void main(String[] args) {
		SpringApplication.run(RabbitRetryDelayedPluginApplication.class, args);
	}

	private static final String ALPHA_NUMERIC_STRING = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
	private static final String EXCHANGE_NAME = "delayed.exchange";
	private static final String QUEUE_NAME = "delayed.queue";

	@Autowired
	RabbitTemplate rabbitTemplate;

	@Override
	public void run(String... args) throws Exception {
		do {
			MessageProperties messageProperties = new MessageProperties();
			messageProperties.setHeader("x-delay", 5000);
			Message message = new Message(generateRandomMessage(10).getBytes(), messageProperties);
			rabbitTemplate.convertAndSend(EXCHANGE_NAME, QUEUE_NAME, message);
			Thread.sleep(3000);
		} while (true);
	}

	public static String generateRandomMessage(int count) {
		StringBuilder builder = new StringBuilder();

		while (count-- != 0) {
			int character = (int) (Math.random() * ALPHA_NUMERIC_STRING.length());
			builder.append(ALPHA_NUMERIC_STRING.charAt(character));
		}

		return builder.toString();
	}

}
