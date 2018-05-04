package actors;

import akka.actor.AbstractActor;
import akka.actor.Props;
import jedis.RedisClient;
import models.TemperatureReading;
import redis.clients.jedis.Tuple;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
        LocalDateTime tempTime = LocalDateTime.now().minusDays(1);
        Long tempEpochTime = tempTime.atZone(ZoneId.systemDefault()).toEpochSecond();
        Set<Tuple> readingsWithScores = client
            .zrangeByScoreWithScores(String.valueOf(sensorId), tempEpochTime, tempEpochTime);

        ArrayList<Tuple> tupleList = new ArrayList<>(readingsWithScores);

        Map<String, Double> tempScoreMap = tupleList.stream().collect(Collectors.toMap(Tuple::getElement, Tuple::getScore));

        Map.Entry<String, Double> latestTemperature = tempScoreMap
            .entrySet()
            .stream()
            .max((entry1, entry2) -> entry1.getValue() > entry2.getValue() ? 1 : -1)
            .get();

        long epochMillis = latestTemperature.getValue().longValue();
        float tempReading = Float.parseFloat(latestTemperature.getKey());

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

