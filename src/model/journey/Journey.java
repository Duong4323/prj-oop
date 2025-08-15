package model.journey;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.bson.Document;

public class Journey {
    private String id;
    private String vehicleId;
    private double totalTime; // Tổng số giờ đi
    private double distance;
    private double averageSpeed;
    private double averageRpm;
    private double maxRpm;
    private double fuelConsumption;
    private List<SensorReading> sensorDataList;
    private int measurementCount; // Số lần đo

    public Journey() {
        this.sensorDataList = new ArrayList<>();
        this.measurementCount = 0;
    }

    public Journey(String vehicleId, double totalTime, double distance, double averageSpeed, double averageRpm, double maxRpm, double fuelConsumption, List<SensorReading> sensorDataList) {
        this.vehicleId = vehicleId;
        this.totalTime = totalTime;
        this.distance = distance;
        this.averageSpeed = averageSpeed;
        this.averageRpm = averageRpm;
        this.maxRpm = maxRpm;
        this.fuelConsumption = fuelConsumption;
        this.sensorDataList = sensorDataList != null ? sensorDataList : new ArrayList<>();
        this.measurementCount = sensorDataList != null ? sensorDataList.size() : 0;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getVehicleId() { return vehicleId; }
    public void setVehicleId(String vehicleId) { this.vehicleId = vehicleId; }
    public double getTotalTime() { return totalTime; }
    public void setTotalTime(double totalTime) { this.totalTime = totalTime; }
    public double getDistance() { return distance; }
    public void setDistance(double distance) { this.distance = distance; }
    public double getAverageSpeed() { return averageSpeed; }
    public void setAverageSpeed(double averageSpeed) { this.averageSpeed = averageSpeed; }
    public double getAverageRpm() { return averageRpm; }
    public void setAverageRpm(double averageRpm) { this.averageRpm = averageRpm; }
    public double getMaxRpm() { return maxRpm; }
    public void setMaxRpm(double maxRpm) { this.maxRpm = maxRpm; }
    public double getFuelConsumption() { return fuelConsumption; }
    public void setFuelConsumption(double fuelConsumption) { this.fuelConsumption = fuelConsumption; }
    public List<SensorReading> getSensorDataList() { return sensorDataList; }
    public void setSensorDataList(List<SensorReading> sensorDataList) { 
        this.sensorDataList = sensorDataList != null ? sensorDataList : new ArrayList<>();
        this.measurementCount = this.sensorDataList.size();
    }
    public void addSensorReading(SensorReading reading) { 
        this.sensorDataList.add(reading);
        this.measurementCount = this.sensorDataList.size();
    }
    public int getMeasurementCount() { return measurementCount; }
    public void setMeasurementCount(int measurementCount) { this.measurementCount = measurementCount; }

    public static Journey fromDocument(Document doc) {
        String id = doc.getObjectId("_id").toString();
        String vehicleId = doc.getString("vehicleId");
        double totalTime = doc.getDouble("totalTime") != null ? doc.getDouble("totalTime") : 0.0;
        double distance = doc.getDouble("distance") != null ? doc.getDouble("distance") : 0.0;
        double averageSpeed = doc.getDouble("averageSpeed") != null ? doc.getDouble("averageSpeed") : 0.0;
        double averageRpm = doc.getDouble("averageRpm") != null ? doc.getDouble("averageRpm") : 0.0;
        double maxRpm = doc.getDouble("maxRpm") != null ? doc.getDouble("maxRpm") : 0.0;
        double fuelConsumption = doc.getDouble("fuelConsumption") != null ? doc.getDouble("fuelConsumption") : 0.0;

        List<SensorReading> sensorReadings = new ArrayList<>();
        if (doc.containsKey("sensorDataList")) {
            List<Document> sensorDocs = (List<Document>) doc.get("sensorDataList");
            for (Document sensorDoc : sensorDocs) {
                sensorReadings.add(SensorReading.fromDocument(sensorDoc));
            }
        }
        int measurementCount = sensorReadings.size();

        Journey journey = new Journey(vehicleId, totalTime, distance, averageSpeed, averageRpm, maxRpm, fuelConsumption, sensorReadings);
        journey.setId(id);
        journey.setMeasurementCount(measurementCount);
        return journey;
    }

    public PerformanceReport generatePerformanceReport() {
        return PerformanceReport.fromJourney(this);
    }

    public Document toDocument() {
        return new Document()
                .append("vehicleId", vehicleId)
                .append("totalTime", totalTime)
                .append("distance", distance)
                .append("averageSpeed", averageSpeed)
                .append("averageRpm", averageRpm)
                .append("maxRpm", maxRpm)
                .append("fuelConsumption", fuelConsumption)
                .append("sensorDataList", sensorDataList.stream().map(SensorReading::toDocument).toList())
                .append("measurementCount", measurementCount);
    }
}