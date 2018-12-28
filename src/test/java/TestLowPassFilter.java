import com.oceanos.roboCarPlatform.LowPassFilter;

/**
 * @autor slonikmak on 28.12.2018.
 */
public class TestLowPassFilter {
    public static void main(String[] args) {
        LowPassFilter filter = new LowPassFilter(0.9);
        for (int i = 0; i < 20; i++) {
            double error = Math.random();
            error = 0.7-error;
            System.out.println(170+error+" "+filter.calc(170+error));
        }
    }
}
