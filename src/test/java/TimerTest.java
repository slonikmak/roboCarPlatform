import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @autor slonikmak on 25.12.2018.
 */
public class TimerTest {
    static long time;
    static long nextTime;
    static int delay = 50;
    public static void main(String[] args) {
        time = System.currentTimeMillis();
        /*ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleWithFixedDelay(myTask, 50, 50, TimeUnit.MILLISECONDS);*/
        /*Timer timer = new Timer();

        timer.schedule(new MyTimerTask(), 50, 50); // Время указывается в миллисекундах*/

        new Thread(()->{
            while (true){
                long st = System.currentTimeMillis();
                long newTime = System.currentTimeMillis();
                long diff = newTime-time;
                System.out.println(diff);
                time = newTime;
                try {
                    Thread.sleep(50-(System.currentTimeMillis()-st));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        while (true){}
    }

    static Runnable myTask = ()->{
        long newTime = System.currentTimeMillis();
        System.out.println(newTime-time);
        time = newTime;
        //System.out.println(new Date().getTime());
    };

    static class MyTimerTask extends TimerTask {

        public void run() {
            //Этот метод будет выполняться с нужным нам периодом
            long newTime = new Date().getTime();
            System.out.println(newTime-time);
            time = newTime;
        }
    }
}
