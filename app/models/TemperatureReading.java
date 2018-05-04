package models;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class TemperatureReading {

    private long sensorId;
    private float reading;
    private LocalDateTime date;

    public TemperatureReading(long sensorId, float reading, LocalDateTime date) {
        this.sensorId = sensorId;
        this.reading = reading;
        this.date = date;
    }

    public TemperatureReading(long sensorId, float reading, long date) {
        this.sensorId = sensorId;
        this.reading = reading;
        this.date = LocalDateTime.ofInstant(Instant.ofEpochMilli(date), ZoneId.systemDefault());
    }

    public long getSensorId() {
        return sensorId;
    }

    public void setSensorId(long sensorId) {
        this.sensorId = sensorId;
    }

    public float getReading() {
        return reading;
    }

    public void setReading(float reading) {
        this.reading = reading;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }
}
