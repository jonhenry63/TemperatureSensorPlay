package actors;

import akka.actor.AbstractActor;
import akka.actor.Props;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.ArrayList;
import java.util.List;

public class TemperatureActor extends AbstractActor {

    private List<Long> temps;
    private JedisPool pool = new JedisPool(new JedisPoolConfig(), "localhost");

    public TemperatureActor(){

        temps = new ArrayList<Long>();
    }

    public static Props props() {
        return Props.create(TemperatureActor.class, () -> new TemperatureActor());
    }


    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Long.class, tmp ->{
                    temps.add(tmp);
                })
                .matchEquals("latest", p -> {
                    switch (temps.size()) {
                        case 0:
                            getSender().tell(-1L,getSelf());
                            break;
                        default:
                            getSender().tell(temps.get(temps.size()-1),getSelf());
                            break;
                    }
                })
                .build();
    }
}

