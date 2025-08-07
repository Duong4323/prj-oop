package model.vehicle;

import java.util.UUID;

public class Vehicle {
    private String vehicleId;         // ID hệ thống tạo tự động
    private String licensePlate;      // Biển số xe
    private String brand;
    private String model;
    private int year;
    private int totalJourneys;        // Số hành trình đã thực hiện

    public Vehicle(String licensePlate, String brand, String model, int year) {
        this.vehicleId = UUID.randomUUID().toString();  // Tự động sinh mã ID duy nhất
        this.licensePlate = licensePlate;
        this.brand = brand;
        this.model = model;
        this.year = year;
        this.totalJourneys = 0;  // Khởi đầu bằng 0 hành trình
    }

    // Getter và Setter

    public String getVehicleId() {
        return vehicleId;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public void setLicensePlate(String licensePlate) {
        this.licensePlate = licensePlate;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getTotalJourneys() {
        return totalJourneys;
    }

    public void setTotalJourneys(int totalJourneys) {
        this.totalJourneys = totalJourneys;
    }

    public void incrementJourneyCount() {
        this.totalJourneys++;
    }

    public void decrementJourneyCount() {
        if (this.totalJourneys > 0) {
            this.totalJourneys--;
        }
    }
}