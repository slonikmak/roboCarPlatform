import com.oceanos.roboCarPlatform.PID2;

/**
 * @autor slonikmak on 21.01.2019.
 */
public class TestPID2 {
    public static void main(String[] args) {
        PID2 pid2 = new PID2(2,0.1,0.2);

        for (int i = 0; i < 50; i++) {
            double error = 50;
            System.out.println(error+" "+pid2.getOutput(error));
        }

        for (int i = 0; i <= 50; i++) {
            double error = 50-i;
            System.out.println(error+" "+pid2.getOutput(error));
        }

        for (int i = 0; i <= 50; i++) {
            double error = -i;
            System.out.println(error+" "+pid2.getOutput(error));
        }

    }
}
