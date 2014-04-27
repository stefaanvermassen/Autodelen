package schedulers;

import play.libs.Akka;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

import java.util.concurrent.TimeUnit;

/**
 * Created by Stefaan Vermassen on 26/04/14.
 */
public abstract class Scheduler implements Runnable{

    public void schedule(FiniteDuration repeatDuration){
        Akka.system().scheduler().schedule(
                Duration.create(0, TimeUnit.MILLISECONDS), //Initial delay 0 milliseconds
                repeatDuration,     //Frequency
                this,
                Akka.system().dispatcher()
        );

    }
}
