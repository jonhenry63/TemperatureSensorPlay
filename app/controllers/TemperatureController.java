package controllers;

import actors.TemperatureActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Scheduler;
import akka.pattern.Patterns;
import akka.util.Timeout;
import play.mvc.Controller;
import play.mvc.Result;
import scala.concurrent.Await;
import scala.concurrent.ExecutionContext;
import scala.concurrent.ExecutionContextExecutor;
import scala.concurrent.Future;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
/**
 * This controller contains an action that demonstrates how to write
 * simple asynchronous code in a controller. It uses a timer to
 * asynchronously delay sending a response for 1 second.
 */
@Singleton
public class TemperatureController extends Controller {

    private ActorSystem actorSystem;
    private ExecutionContextExecutor exec;
    private ActorRef tempActor;
    private Timeout timeout = new Timeout(10L, TimeUnit.SECONDS);

    /**
     * @param actorSystem We need the {@link ActorSystem}'s
     * {@link Scheduler} to run code after a delay.
     * @param exec We need a Java {@link Executor} to apply the result
     * of the {@link CompletableFuture} and a Scala
     * {@link ExecutionContext} so we can use the Akka {@link Scheduler}.
     * An {@link ExecutionContextExecutor} implements both interfaces.
     */
    @Inject
    public TemperatureController(ActorSystem actorSystem, ExecutionContextExecutor exec) {
      this.actorSystem = actorSystem;
      this.exec = exec;
      tempActor = actorSystem.actorOf(TemperatureActor.props());
    }

    /**
     * An action that returns a plain text message after a delay
     * of 1 second.
     *
     * The configuration in the <code>routes</code> file means that this method
     * will be called when the application receives a <code>GET</code> request with
     * a path of <code>/message</code>.
     */
    public Result writeTemperature(Long temp){
        tempActor.tell(temp,tempActor);
        return ok("ok i did it");
    }

    public Result readTemperature() {
        Future<Object> future = Patterns.ask(tempActor, "latest", timeout);
        try {
            Long result = (Long) Await.result(future, timeout.duration());
            return ok(views.html.temps.render(result.toString(), null));
        } catch (Exception e) {
            e.printStackTrace();
            return status(422, views.html.temps.render("error", null));
        }
    }
}
