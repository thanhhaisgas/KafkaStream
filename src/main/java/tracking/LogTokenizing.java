package tracking;

import org.apache.kafka.streams.processor.Processor;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.processor.ProcessorSupplier;
import org.apache.kafka.streams.state.KeyValueStore;
import rfx.core.util.StringPool;
import rfx.core.util.StringUtil;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class LogTokenizing implements ProcessorSupplier<String, String> {

    private String topic;

    public LogTokenizing(String topic) {
        this.topic = topic;
    }

    @Override
    public Processor<String, String> get() {
        return new Processor<String, String>() {

            private ProcessorContext context;
            private KeyValueStore<String, Integer> kvStore;

            @Override
            public void init(ProcessorContext processorContext) {
                this.context = processorContext;
                this.context.schedule(1000);
//                this.kvStore = (KeyValueStore<String, Integer>) context.getStateStore("tracking");
            }

            @Override
            public void process(String s, String s2) {
                String[] logTokens = s2.split("\t\\| ");
                if (logTokens.length == 5){
                    Tuple tuple = new Tuple(StringUtil.safeSplitAndGet(logTokens[0], StringPool.COLON, 0),
                            StringUtil.safeParseInt(logTokens[1]),
                            StringUtil.safeString(logTokens[2]),
                            StringUtil.safeString(logTokens[3]),
                            StringUtil.safeString(logTokens[4]),
                            StringUtil.safeString(context.topic()),
                            StringUtil.safeParseLong(context.offset()),
                            StringUtil.safeParseInt(context.offset()));
                    new ParsingAdDataLog(tuple.getData());
                    System.out.println("----------------------");
                }
//          View data
//                for (HashMap.Entry<String, Object> entry : tuple.getData().entrySet()) {
//                    String key = entry.getKey().toString();
//                    Object value = entry.getValue();
//                    System.out.println(key +"  -  "+ value);
//                }
//
                context.commit();
            }

            @Override
            public void punctuate(long l) {
            }

            @Override
            public void close() {

            }
        };
    }
}
