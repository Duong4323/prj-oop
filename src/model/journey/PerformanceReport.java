package model.journey;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import java.io.FileOutputStream;
import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.text.SimpleDateFormat;

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

    public static String formatDuration(Duration duration) {
        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();
        long seconds = duration.toSecondsPart();
        return String.format("%d:%02d:%02d", hours, minutes, seconds);
    }

    public void exportToPdf(String filePath) {
        try {
            Document pdfDoc = new Document(PageSize.A4);
            PdfWriter.getInstance(pdfDoc, new FileOutputStream(filePath));
            pdfDoc.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            pdfDoc.add(new Paragraph("Báo cáo Hiệu suất Hành trình", titleFont));
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            pdfDoc.add(new Paragraph("Ngày xuất: " + dateFormat.format(new Date()), FontFactory.getFont(FontFactory.HELVETICA, 12)));

            pdfDoc.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10f);
            table.setSpacingAfter(10f);

            addRow(table, "Vận tốc trung bình", String.format("%.2f km/h", averageSpeed));
            addRow(table, "Nhiên liệu trung bình", String.format("%.2f L/100km", averageFuelConsumption));
            addRow(table, "Max RPM", String.format("%.0f", maxRpm));
            addRow(table, "Quãng đường", String.format("%.2f km", totalDistance));
            addRow(table, "Thời gian hoạt động", formatDuration(totalOperatingTime));

            pdfDoc.add(table);
            pdfDoc.close();

            System.out.println("✅ Đã xuất PDF: " + filePath);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void exportToPdfFromJourneys(List<Journey> journeys, String filePath) {
        if (journeys == null || journeys.isEmpty()) {
            System.out.println("Không có dữ liệu hành trình để xuất.");
            return;
        }

        try {
            Document pdfDoc = new Document(PageSize.A4);
            PdfWriter.getInstance(pdfDoc, new FileOutputStream(filePath));
            pdfDoc.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            pdfDoc.add(new Paragraph("Báo cáo Hiệu suất Hành trình Tổng hợp", titleFont));
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            pdfDoc.add(new Paragraph("Ngày xuất: " + dateFormat.format(new Date()), FontFactory.getFont(FontFactory.HELVETICA, 12)));

            pdfDoc.add(new Paragraph(" "));

            double totalDistance = journeys.stream().mapToDouble(Journey::getDistance).sum();
            double totalTime = journeys.stream().mapToDouble(Journey::getTotalTime).sum();
            double averageSpeed = (totalTime > 0) ? totalDistance / totalTime : 0.0;
            double totalFuel = journeys.stream()
                    .flatMap(j -> j.getSensorDataList().stream())
                    .mapToDouble(SensorReading::getFuelConsumption)
                    .sum();
            double averageFuelConsumption = (totalDistance > 0) ? (totalFuel * 100) / totalDistance : 0.0;
            double maxRpm = journeys.stream().mapToDouble(Journey::getMaxRpm).max().orElse(0.0);
            Duration totalOperatingTime = journeys.stream()
                    .map(j -> Duration.ofHours((long) j.getTotalTime()))
                    .reduce(Duration.ZERO, Duration::plus);

            PdfPTable summaryTable = new PdfPTable(2);
            summaryTable.setWidthPercentage(100);
            summaryTable.setSpacingBefore(10f);
            summaryTable.setSpacingAfter(10f);

            addRow(summaryTable, "Vận tốc trung bình", String.format("%.2f km/h", averageSpeed));
            addRow(summaryTable, "Nhiên liệu trung bình", String.format("%.2f L/100km", averageFuelConsumption));
            addRow(summaryTable, "Max RPM", String.format("%.0f", maxRpm));
            addRow(summaryTable, "Tổng quãng đường", String.format("%.2f km", totalDistance));
            addRow(summaryTable, "Tổng thời gian hoạt động", formatDuration(totalOperatingTime));

            pdfDoc.add(summaryTable);
            pdfDoc.close();

            System.out.println("✅ Đã xuất PDF tổng hợp: " + filePath);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void addRow(PdfPTable table, String label, String value) {
        table.addCell(new PdfPCell(new Phrase(label)));
        table.addCell(new PdfPCell(new Phrase(value)));
    }

    private static void addHeader(PdfPTable table, String... headers) {
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
            table.addCell(cell);
        }
    }

    // private static void addRow(PdfPTable table, String id, String vehicleId, String totalTime, String distance,
    //                          String avgSpeed, String avgRpm, String maxRpm, String fuelConsumption) {
    //     table.addCell(new PdfPCell(new Phrase(id)));
    //     table.addCell(new PdfPCell(new Phrase(vehicleId)));
    //     table.addCell(new PdfPCell(new Phrase(totalTime)));
    //     table.addCell(new PdfPCell(new Phrase(distance)));
    //     table.addCell(new PdfPCell(new Phrase(avgSpeed)));
    //     table.addCell(new PdfPCell(new Phrase(avgRpm)));
    //     table.addCell(new PdfPCell(new Phrase(maxRpm)));
    //     table.addCell(new PdfPCell(new Phrase(fuelConsumption)));
    // }

    public static PerformanceReport fromJourney(Journey journey) {
        if (journey == null) {
            return new PerformanceReport(0.0, 0.0, 0.0, 0.0, Duration.ZERO);
        }

        double calculatedAverageSpeed = (journey.getDistance() > 0 && journey.getTotalTime() > 0)
                ? journey.getDistance() / journey.getTotalTime()
                : 0.0; // Không dùng journey.getAverageSpeed() nếu dữ liệu không hợp lệ
        double calculatedAverageFuelConsumption = 0.0;
        double calculatedMaxRpm = journey.getMaxRpm();
        double calculatedTotalDistance = journey.getDistance();
        Duration calculatedTotalOperatingTime = Duration.ofHours((long) journey.getTotalTime());

        if (journey.getSensorDataList() != null && !journey.getSensorDataList().isEmpty()) {
            double totalFuel = journey.getSensorDataList().stream()
                    .mapToDouble(SensorReading::getFuelConsumption)
                    .sum();

            calculatedAverageFuelConsumption = (calculatedTotalDistance > 0)
                    ? (totalFuel * 100) / calculatedTotalDistance
                    : 0.0;

            calculatedMaxRpm = journey.getSensorDataList().stream()
                    .mapToDouble(SensorReading::getRpm)
                    .max()
                    .orElse(journey.getMaxRpm());
        } else if (journey.getFuelConsumption() > 0 && calculatedTotalDistance > 0) {
            calculatedAverageFuelConsumption = journey.getFuelConsumption() * 100 / calculatedTotalDistance;
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