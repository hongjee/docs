package demo.kafka.consumer;

import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;

public class Receiver {

	private static final Logger LOGGER = LoggerFactory.getLogger(Receiver.class);

	// The  CountDownLatch  value that is used in the unit test case is increased so that we can send out a batch of 20 messages.
	public static final int COUNT = 20;

	// For testing convenience, we added a CountDownLatch.
	// This allows the POJO to signal that a message is received.
	// This is something you are not likely to implement in a production
	// application.
	private CountDownLatch latch = new CountDownLatch(COUNT);

	public CountDownLatch getLatch() {
		return latch;
	}

	@KafkaListener(id = "batch-listener", topics = "${kafka.topic.kafkademo3}")

	// The  receive()  method of the  Receiver  listener POJO needs to be updated to receive a  List  of payloads
	//
	// For logging purposes, we also add the partition and offset headers of each message to the  receive()  method. 
	// These headers are available in a list and map to the received messages based on the index within the list.
	
//	public void receive(List<Message<?>> messages, @Header(KafkaHeaders.RECEIVED_PARTITION_ID) List<Integer> partitions,
//			@Header(KafkaHeaders.OFFSET) List<Long> offsets) {

//		public void receive(List<ConsumerRecord<?,?>> messages, @Header(KafkaHeaders.RECEIVED_PARTITION_ID) List<Integer> partitions,
//		@Header(KafkaHeaders.OFFSET) List<Long> offsets) {
		
	public void receive(List<String> data, @Header(KafkaHeaders.RECEIVED_PARTITION_ID) List<Integer> partitions,
			@Header(KafkaHeaders.OFFSET) List<Long> offsets) {
		LOGGER.info("start of batch receive");
		for (int i = 0; i < data.size(); i++) {
			LOGGER.info("received message='{}' with partition-offset='{}'", data.get(i),
					partitions.get(i) + "-" + offsets.get(i));
			// handle message

			latch.countDown();
		}
		LOGGER.info("end of batch receive");
	}
}
