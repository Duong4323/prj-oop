package model.journey;

import java.time.Duration;

public class PerformanceReport {
    private double averageSpeed; // km/h
    private double averageFuelConsumption; // liters
    private double maxRpm; // revolutions per minute
    private double totalDistance; // km
    private Duration totalOperatingTime;

    public PerformanceReport(double averageSpeed, double averageFuelConsumption, double maxRpm, double totalDistance, Duration totalOperatingTime) {
        this.averageSpeed = averageSpeed;
        this.averageFuelConsumption = averageFuelConsumption;
        this.maxRpm = maxRpm;
        this.totalDistance = totalDistance;
        this.totalOperatingTime = totalOperatingTime;
    }

    // Getters
    public double getAverageSpeed() { return averageSpeed; }
    public double getAverageFuelConsumption() { return averageFuelConsumption; }
    public double getMaxRpm() { return maxRpm; }
    public double getTotalDistance() { return totalDistance; }
    public Duration getTotalOperatingTime() { return totalOperatingTime; }

    @Override
    public String toString() {
        return String.format(
    "Báo cáo hành trình:\n" +
    "Vận tốc trung bình = %.2f km/h\n" +
    "Nhiên liệu = %.2f L\n" +
    "Max RPM = %.0f\n" +
    "Quãng đường = %.2f km\n" +
    "Thời gian = %s",
    averageSpeed,
    averageFuelConsumption,
    maxRpm,
    totalDistance,
    formatDuration(totalOperatingTime)
);

    }

    private String formatDuration(Duration duration) {
        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();
        long seconds = duration.toSecondsPart();
        return String.format("%d:%02d:%02d", hours, minutes, seconds);
    }
}