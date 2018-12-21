import com.oceanos.roboCarPlatform.Car;

/**
 * @autor slonikmak on 19.12.2018.
 */
public class DeviationTest {
    public static void main(String[] args) {
        Car car = new Car();

        System.out.println(car.getDeviation(45, 300)); //-105
        System.out.println(car.getDeviation(45, 100)); //55
        System.out.println(car.getDeviation(5, 300)); //-65
        System.out.println(car.getDeviation(300, 10)); //70
    }
}
