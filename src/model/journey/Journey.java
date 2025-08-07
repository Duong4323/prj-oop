package model.journey;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.bson.Document;

public class Journey {
    private String id;
    private String vehicleId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private double distance;
    private List<SensorReading> sensorDataList;

    public Journey() {
        this.sensorDataList = new ArrayList<>();
    }

    public Journey(String vehicleId, LocalDateTime startTime, LocalDateTime endTime,
                   double distance, List<SensorReading> sensorDataList) {
        this.vehicleId = vehicleId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.distance = distance;
        this.sensorDataList = sensorDataList != null ? sensorDataList : new ArrayList<>();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getVehicleId() { return vehicleId; }
    public void setVehicleId(String vehicleId) { this.vehicleId = vehicleId; }
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    public double getDistance() { return distance; }
    public void setDistance(double distance) { this.distance = distance; }
    public List<SensorReading> getSensorDataList() { return sensorDataList; }
    public void setSensorDataList(List<SensorReading> sensorDataList) { this.sensorDataList = sensorDataList != null ? sensorDataList : new ArrayList<>(); }
    public void addSensorReading(SensorReading reading) { this.sensorDataList.add(reading); }

    public static Journey fromDocument(Document doc) {
        String id = doc.getObjectId("_id").toString();
        String vehicleId = doc.getString("vehicleId");
        LocalDateTime start = LocalDateTime.parse(doc.getString("startTime"));
        LocalDateTime end = LocalDateTime.parse(doc.getString("endTime"));
        double distance = doc.getDouble("distance");

        List<SensorReading> sensorReadings = new ArrayList<>();
        if (doc.containsKey("sensorData")) {
            List<Document> sensorDocs = (List<Document>) doc.get("sensorData");
            for (Document sensorDoc : sensorDocs) {
                sensorReadings.add(SensorReading.fromDocument(sensorDoc));
            }
        }

        Journey journey = new Journey(vehicleId, start, end, distance, sensorReadings);
        journey.setId(id);
        return journey;
    }

    public PerformanceReport generatePerformanceReport() {
        if (sensorDataList == null || sensorDataList.isEmpty() || startTime == null || endTime == null) {
            return new PerformanceReport(0.0, 0.0, 0.0, distance, java.time.Duration.between(startTime != null ? startTime : LocalDateTime.now(), endTime != null ? endTime : LocalDateTime.now()));
        }

        java.time.Duration duration = java.time.Duration.between(startTime, endTime);
        double totalHours = duration.toHours() + (duration.toMinutesPart() / 60.0) + (duration.toSecondsPart() / 3600.0);
        double avgSpeed = totalHours > 0 ? (distance / totalHours) : 0.0;
        double totalFuel = sensorDataList.stream().mapToDouble(SensorReading::getFuelConsumption).sum();
        double avgFuel = sensorDataList.isEmpty() ? 0.0 : totalFuel / sensorDataList.size();
        double maxRpm = sensorDataList.stream().mapToDouble(SensorReading::getRpm).max().orElse(0.0);

        return new PerformanceReport(avgSpeed, avgFuel, maxRpm, distance, duration);
    }

    // Thêm phương thức getAvgSpeed
    public Double getAvgSpeed() {
        if (startTime == null || endTime == null || distance <= 0) {
            return 0.0;
        }
        java.time.Duration duration = java.time.Duration.between(startTime, endTime);
        double totalHours = duration.toHours() + (duration.toMinutesPart() / 60.0) + (duration.toSecondsPart() / 3600.0);
        return totalHours > 0 ? (distance / totalHours) : 0.0;
    }
}