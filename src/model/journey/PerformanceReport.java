package model.journey;

import java.time.Duration;
import java.util.List;

public class PerformanceReport {
    private double averageSpeed; // km/h
    private double averageFuelConsumption; // (liters × 100) / km
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
            "Nhiên liệu trung bình = %.2f L/100km\n" +
            "Max RPM = %.0f\n" +
            "Quãng đường = %.2f km\n" +
            "Thời gian hoạt động = %s",
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

    // Phương thức tạo nội dung LaTeX để xuất PDF
    public String toLatex() {
        return "\\documentclass[a4paper,12pt]{article}\n" +
               "\\usepackage[utf8]{inputenc}\n" +
               "\\usepackage{geometry}\n" +
               "\\geometry{margin=1in}\n" +
               "\\usepackage{amsmath}\n" +
               "\\usepackage{booktabs}\n" +
               "\\usepackage{longtable}\n" +
               "\\usepackage{colortbl}\n" +
               "\\usepackage{DejaVuSans} % Font phù hợp cho nhiều ngôn ngữ\n" +
               "\\begin{document}\n" +
               "\\section*{Báo cáo Hiệu suất Hành trình}\n" +
               "\\begin{tabular}{ll}\n" +
               "\\toprule\n" +
               "Tham số & Giá trị \\\\\n" +
               "\\midrule\n" +
               "Vận tốc trung bình & \\textbf{" + String.format("%.2f", averageSpeed) + "} km/h \\\\\n" +
               "Nhiên liệu trung bình & \\textbf{" + String.format("%.2f", averageFuelConsumption) + "} L/100km \\\\\n" +
               "Max RPM & \\textbf{" + String.format("%.0f", maxRpm) + "} \\\\\n" +
               "Quãng đường tổng cộng & \\textbf{" + String.format("%.2f", totalDistance) + "} km \\\\\n" +
               "Thời gian hoạt động & \\textbf{" + formatDuration(totalOperatingTime) + "} \\\\\n" +
               "\\bottomrule\n" +
               "\\end{tabular}\n" +
               "\\end{document}";
    }

    // Phương thức bổ sung để tính averageFuelConsumption từ sensorDataList
    public static PerformanceReport fromJourney(Journey journey) {
        if (journey == null) {
            return new PerformanceReport(0.0, 0.0, 0.0, 0.0, Duration.ZERO);
        }

        double calculatedAverageSpeed = (journey.getDistance() > 0 && journey.getTotalTime() > 0) ? journey.getDistance() / journey.getTotalTime() : journey.getAverageSpeed();
        double calculatedAverageFuelConsumption = 0.0;
        double calculatedMaxRpm = journey.getMaxRpm();
        double calculatedTotalDistance = journey.getDistance();
        Duration calculatedTotalOperatingTime = Duration.ofHours((long) journey.getTotalTime());

        if (journey.getSensorDataList() != null && !journey.getSensorDataList().isEmpty()) {
            double maxFuel = journey.getSensorDataList().stream().mapToDouble(SensorReading::getFuelConsumption).max().orElse(0.0);
            double minFuel = journey.getSensorDataList().stream().mapToDouble(SensorReading::getFuelConsumption).min().orElse(0.0);
            calculatedAverageFuelConsumption = (maxFuel - minFuel) * 100 / calculatedTotalDistance;
            calculatedMaxRpm = journey.getSensorDataList().stream().mapToDouble(SensorReading::getRpm).max().orElse(journey.getMaxRpm());
        } else if (journey.getFuelConsumption() > 0 && calculatedTotalDistance > 0) {
            calculatedAverageFuelConsumption = journey.getFuelConsumption() * 100 / calculatedTotalDistance; // Điều chỉnh nếu cần
        }

        return new PerformanceReport(
            calculatedAverageSpeed,
            calculatedAverageFuelConsumption,
            calculatedMaxRpm,
            calculatedTotalDistance,
            calculatedTotalOperatingTime
        );
    }
}