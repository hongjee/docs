package demo.kafka;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.TimeUnit;

import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.rule.KafkaEmbedded;
//import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import demo.kafka.consumer.Receiver;
import demo.kafka.producer.Sender;

@RunWith(SpringRunner.class)
@SpringBootTest
//@DirtiesContext
public class SpringKafkaApplicationTest {

	private static final String KAFKADEMO_TOPIC = "kafkademo2.t";

	@ClassRule
	public static KafkaEmbedded embeddedKafka = new KafkaEmbedded(1, true, KAFKADEMO_TOPIC);

	@Autowired
	private Receiver receiver;

	@Autowired
	private Sender sender;

	@Test
	public void testReceive() throws Exception {
		sender.send(KAFKADEMO_TOPIC, "Hello Spring Kafka!");

		receiver.getLatch().await(10000, TimeUnit.MILLISECONDS);
		assertThat(receiver.getLatch().getCount()).isEqualTo(0);
	}
}
