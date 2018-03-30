package tracking;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStreamBuilder;

import java.util.Properties;

public class AdImpressionKStream {
    public static void main(String[] args) {
        Properties config = new Properties();
        config.put(StreamsConfig.APPLICATION_ID_CONFIG, "AdImpression");
        config.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "118.69.135.198:9421,118.69.135.198:9422");
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        config.put(StreamsConfig.KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        config.put(StreamsConfig.VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        KStreamBuilder builder = new KStreamBuilder();
        builder.addSource("Source", "tracking-rawlog-test");
        builder.addProcessor("LogTokenizing", new LogTokenizing("tracking-rawlog-test"), "Source");

        KafkaStreams streams = new KafkaStreams(builder, config);
        streams.start();
        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
    }
}
