import com.oceanos.roboCarPlatform.PID;

/**
 * @autor slonikmak on 12.12.2018.
 */
public class TestPid {
    public static void main(String[] args) {
        PID pid = new PID(0.01, 0.0001, 0.0001);

       /*for (int i = 5; i >= 0; i -= 1) {
            double diff = (50.0/90.0);
            System.out.println(diff+" "+pid.getOutput(diff));
            //System.out.println(pid.getOutput(50));
        }*/

        for (int i = 0; i < 100; i += 5) {
            //double diff = i/90.0;
            //if (diff>1) diff = 1;
            double diff = i;
            System.out.println(diff+" "+pid.getOutput(diff));
            //System.out.println(pid.getOutput(50));
        }
    }
}
