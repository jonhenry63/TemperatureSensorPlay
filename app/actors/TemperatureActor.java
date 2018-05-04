package actors;

import akka.actor.AbstractActor;
import akka.actor.Props;
import jedis.RedisClient;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class TemperatureActor extends AbstractActor {

    private List<Long> temps;
    private RedisClient client = new RedisClient();

    public TemperatureActor(){

        temps = new ArrayList<Long>();
    }

    public static Props props() {
        return Props.create(TemperatureActor.class, TemperatureActor::new);
    }


    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Long.class, tmp ->{
                    temps.add(tmp);
                })
                .match(RecieveTemperature.class, p -> {
                        client.zadd(p.getSensorID().toString(),p.getTimeStampLong(), p.getTemp().toString());
                        })
                .match(GetTemperature.class, p -> {
                    LocalDateTime tempTime = LocalDateTime.now();
                    Long tempEpochTime = tempTime.atZone(ZoneId.systemDefault()).toEpochSecond();
                    client.zrangebyscore(p.sensorID.toString(), tempEpochTime-60, tempEpochTime);

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

    public static final class RecieveTemperature{
        final Float temp;
        final Long sensorID;
        final LocalDateTime timeStamp;

        public RecieveTemperature(Float temp, Long sensorID, LocalDateTime timeStamp) {
            this.temp = temp;
            this.sensorID = sensorID;
            this.timeStamp = timeStamp;
        }

        public RecieveTemperature(Float temp, Long sensorID, String timeStamp) {
            this.temp = temp;
            this.sensorID = sensorID;

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-ddTHH:mm:ssZ");
            LocalDateTime dateTime = LocalDateTime.parse(timeStamp, formatter);
            this.timeStamp = dateTime;
        }

        public Float getTemp() {
            return temp;
        }

        public LocalDateTime getTimeStamp() {
            return timeStamp;
        }

        public Long getTimeStampLong(){
            Long epoch = timeStamp.atZone(ZoneId.systemDefault()).toEpochSecond();
            return epoch;
        }


        public Long getSensorID() {
            return sensorID;
        }
    }

    public static final class GetTemperature{
        final Long sensorID;

        public GetTemperature(Long sensorID) {
            this.sensorID = sensorID;
        }
    }
}

