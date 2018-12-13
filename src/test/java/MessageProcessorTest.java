
import com.oceanos.ros.messages.MessageProcessor;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @autor slonikmak on 11.12.2018.
 */
public class MessageProcessorTest {
    public static void main(String[] args) {
        MessageProcessor messageProcessor = new MessageProcessor();
        messageProcessor.addConsumer("type1", System.out::println);
        messageProcessor.addConsumer("type1", System.out::println);
        messageProcessor.addConsumer("type2", System.out::println);

        for (Map.Entry<String, List<Consumer<String>>> e :
                messageProcessor.getMessageConsumers().entrySet()) {
            e.getValue().forEach(c->c.accept(e.getKey()));
        }

        messageProcessor.processMessage("type1;blabla");
    }
}
