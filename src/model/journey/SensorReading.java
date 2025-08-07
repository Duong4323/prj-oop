package model.journey;

import java.time.LocalDateTime;
import org.bson.Document;

public class SensorReading {
    private LocalDateTime timestamp;
    private double instantSpeed; // km/h
    private double fuelConsumption; // liters
    private double rpm; // revolutions per minute

    public SensorReading(LocalDateTime timestamp, double instantSpeed, double fuelConsumption, double rpm) {
        this.timestamp = timestamp;
        this.instantSpeed = instantSpeed;
        this.fuelConsumption = fuelConsumption;
        this.rpm = rpm;
    }

    // Getters and Setters
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    public double getInstantSpeed() { return instantSpeed; }
    public void setInstantSpeed(double instantSpeed) { this.instantSpeed = instantSpeed; }
    public double getFuelConsumption() { return fuelConsumption; }
    public void setFuelConsumption(double fuelConsumption) { this.fuelConsumption = fuelConsumption; }
    public double getRpm() { return rpm; }
    public void setRpm(double rpm) { this.rpm = rpm; }

    public static SensorReading fromDocument(Document doc) {
        LocalDateTime timestamp = LocalDateTime.parse(doc.getString("timestamp"));
        double instantSpeed = doc.getDouble("instantSpeed");
        double fuelConsumption = doc.getDouble("fuelConsumption");
        double rpm = doc.getDouble("rpm");
        return new SensorReading(timestamp, instantSpeed, fuelConsumption, rpm);
    }

    public Document toDocument() {
        return new Document()
                .append("timestamp", timestamp.toString())
                .append("instantSpeed", instantSpeed)
                .append("fuelConsumption", fuelConsumption)
                .append("rpm", rpm);
    }
}