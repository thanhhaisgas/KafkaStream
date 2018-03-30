package myapps;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.processor.Processor;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.processor.ProcessorSupplier;
import org.apache.kafka.streams.processor.TopologyBuilder;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.Stores;

import java.util.Locale;
import java.util.Properties;

public class WordCountDemoWithProcessor {
	private static class MyProcessorSupplier implements ProcessorSupplier<String, String>{
		@Override
		public Processor<String, String> get() {
			// TODO Auto-generated method stub
			return new Processor<String, String>() {
				private ProcessorContext context;
				private KeyValueStore<String, Integer> kvStore;

				@Override
				public void close() {
					// TODO Auto-generated method stub
				}

				@Override
				public void init(ProcessorContext context) {
					// TODO Auto-generated method stub
					this.context = context;
					this.context.schedule(1000);
					this.kvStore = (KeyValueStore<String, Integer>) context.getStateStore("Counts");
				}

				@Override
				public void process(String dummy, String line) {
					// TODO Auto-generated method stub
					String[] words = line.toLowerCase(Locale.getDefault()).split(" ");
					for (String word : words) {
						Integer oldValue = this.kvStore.get(word);
						System.out.println("=============");
						System.out.println(oldValue);
						System.out.println("=============");
						if (oldValue == null) {
							this.kvStore.put(word, 1);
						}else {
							this.kvStore.put(word, oldValue+1);
						}
					}
					context.commit();
				}

				@Override
				public void punctuate(long timestamp) {
					// TODO Auto-generated method stub
					try	(KeyValueIterator<String, Integer> iter = this.kvStore.all()){
						//System.out.println("----------- " + timestamp + " ----------- ");
						while (iter.hasNext()) {
							KeyValue<String, Integer> entry = iter.next();
							//System.out.println("[" + entry.key + ", " + entry.value + "]");
							context.forward(entry.key, entry.value.toString());
						}
					}
				}
			};
		}
	}
	
	public static class HaiProcessor implements ProcessorSupplier<String, String>{

		@Override
		public Processor<String, String> get() {
			// TODO Auto-generated method stub
			return new Processor<String, String>() {

				@Override
				public void close() {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void init(ProcessorContext arg0) {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void process(String arg0, String arg1) {
					// TODO Auto-generated method stub
					System.out.println("Hai dep trai");
					
				}

				@Override
				public void punctuate(long arg0) {
					// TODO Auto-generated method stub
					
				}
			};
		}
		
	}
	
	public static void main(String[] args) throws Exception {
		Properties props = new Properties();
		props.put(StreamsConfig.APPLICATION_ID_CONFIG, "world-count-processor");
		props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "118.69.135.198:9421,118.69.135.198:9422");
        props.put(StreamsConfig.KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        props.put(StreamsConfig.VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        
        TopologyBuilder builder = new TopologyBuilder();
        builder.addSource("Source", "output-tracking-event-HAIBD");
        builder.addProcessor("Process-word-count", new MyProcessorSupplier(), "Source");
        builder.addProcessor("Process-HAI", new HaiProcessor(), "Source");
        builder.addStateStore(Stores.create("Counts").withStringKeys().withIntegerValues().inMemory().build(), "Process-word-count");
        builder.addSink("Sink", "output-tracking-event-HAIGAY", "Process-word-count");
        
        KafkaStreams streams = new KafkaStreams(builder, props);
        streams.start();
        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
	}
}
