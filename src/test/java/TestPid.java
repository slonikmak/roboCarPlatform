import com.oceanos.roboCarPlatform.PID;

/**
 * @autor slonikmak on 12.12.2018.
 */
public class TestPid {
    public static void main(String[] args) {
        PID pid = new PID(1, 0.01, 0);

        for (int i = 5; i >= 0; i -= 1) {
            float diff = 50/180f;
            System.out.println(diff+" "+pid.calc(diff));
            //System.out.println(pid.calc(50));
        }

        for (int i = 50; i >= 0; i -= 10) {
            float diff = i/180f;
            System.out.println(diff+" "+pid.calc(diff));
            //System.out.println(pid.calc(50));
        }
    }
}
