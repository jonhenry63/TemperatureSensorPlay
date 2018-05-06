package actors;

import akka.actor.AbstractActor;
import akka.actor.Props;
import jedis.RedisClient;
import models.TemperatureReading;
import redis.clients.jedis.Tuple;

public class TemperatureActor extends AbstractActor {

    private RedisClient client = new RedisClient();

    private TemperatureActor(){ }

    public static Props props() {
        return Props.create(TemperatureActor.class, TemperatureActor::new);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
            .match(WriteTemperature.class, p -> client.zadd(String.valueOf(p.sensorId),
                p.epochMillis,
                String.valueOf(p.temperature)))
            .match(ReadTemperature.class, p -> getSender().tell(getLatestTemperature(p.sensorId), getSelf()))
            .build();
    }

    private TemperatureReading getLatestTemperature(long sensorId) {
        String sensorIdString = String.valueOf(sensorId);
        int latestReadingIndex = client.zcard(sensorIdString).intValue() - 1;
        Tuple latestReading = client
            .zrangeWithScores(sensorIdString, latestReadingIndex, latestReadingIndex)
            .iterator().next();

        long epochMillis = (long) latestReading.getScore();
        float tempReading = Float.parseFloat(latestReading.getElement());

        return new TemperatureReading(sensorId, tempReading, epochMillis);
    }

    public static final class WriteTemperature {
        long sensorId;
        float temperature;
        long epochMillis;

        public WriteTemperature(long sensorId, float temperature, long epochMillis) {
            this.sensorId = sensorId;
            this.temperature = temperature;
            this.epochMillis = epochMillis;
        }
    }

    public static final class ReadTemperature {
        long sensorId;

        public ReadTemperature(long sensorId) {
            this.sensorId = sensorId;
        }
    }
}

