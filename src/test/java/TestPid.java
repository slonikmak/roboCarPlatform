/**
 * @autor slonikmak on 12.12.2018.
 */
public class TestPid {
    public static void main(String[] args) {
        for (int i = 0; i <= 180; i += 10) {
            System.out.println(i+": "+proportion(i));
        }
    }

    static int proportion(int inValue){
        return (int) (((float)inValue/180.0)*100);
    }
}
