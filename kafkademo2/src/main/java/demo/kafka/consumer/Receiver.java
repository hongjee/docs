package demo.kafka.consumer;

import java.util.concurrent.CountDownLatch;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class Receiver {

	private static final Logger LOGGER = LoggerFactory.getLogger(Receiver.class);

	// For testing convenience, we added a CountDownLatch. 
	// This allows the POJO to signal that a message is received. 
	// This is something you are not likely to implement in a production application.
	private CountDownLatch latch = new CountDownLatch(1);

	public CountDownLatch getLatch() {
		return latch;
	}

	@KafkaListener(topics = "${kafka.topic.boot}")
	public void receive(ConsumerRecord<?, ?> consumerRecord) {
		LOGGER.info("received payload='{}'", consumerRecord.toString());
		latch.countDown();
	}
}
