import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @autor slonikmak on 07.12.2018.
 */
public class MsgTest {
    static Map<String, List<String>> messages = new HashMap();

    static void processMessage(String msg){

        msg = msg.replace("$","");
        System.out.println("process message "+msg);
        String[] dataArray = msg.split(",");
        int length = dataArray.length;
        String timeStamp = dataArray[length-1];
        //List<String> messageList = messages.computeIfAbsent(timeStamp, k -> new ArrayList<>());
        List<String> messageList = messages.get(timeStamp);
        if (messageList == null) {
            messageList = new ArrayList<>();
            messages.put(timeStamp, messageList);
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < length - 1; i++) {
            builder.append(dataArray[i]);
            builder.append(" ");
        }
        messageList.add(builder.toString());
        if (messageList.size()==3){
            System.out.println("-----START MESSAGE------");
            for (String str :
                    messageList) {
                System.out.println(str);
            }
            System.out.println("-----END MESSAGE------");
            messages.remove(timeStamp);
        }
    }

    public static void main(String[] args) {
        processMessage("1,2,3,10");
        processMessage("2,4,6,10");
        processMessage("a,d,f,10");
    }
}
