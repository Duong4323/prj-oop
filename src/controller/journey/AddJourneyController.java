package controller.journey;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import model.journey.Journey;
import model.journey.JourneyDatabase;
import model.journey.SensorReading;
import model.vehicle.Vehicle;
import model.vehicle.VehicleDatabase;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AddJourneyController {

    @FXML private ComboBox<String> vehicleComboBox;
    @FXML private TextField totalTimeField;
    @FXML private TextField distanceField;
    @FXML private TextField measurementCountField;
    @FXML private VBox measurementsVBox;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    private Journey selectedJourney;
    private JourneyController parentController;
    private List<Measurement> measurements = new ArrayList<>();

    @FXML
    public void initialize() {
        loadVehicles();
        setupMeasurementFields();
        if (selectedJourney != null) {
            populateFields(selectedJourney);
        }
    }

    private void loadVehicles() {
        try (VehicleDatabase db = new VehicleDatabase()) {
            List<Vehicle> vehicles = db.getAllVehicles();
            vehicleComboBox.getItems().clear();
            for (Vehicle v : vehicles) {
                vehicleComboBox.getItems().add(v.getLicensePlate());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setParentController(JourneyController parentController) {
        this.parentController = parentController;
    }

    public void initializeAddForm(Journey journey) {
        this.selectedJourney = journey;
        initialize();
    }

    private void setupMeasurementFields() {
        measurementsVBox.getChildren().clear();
        measurementCountField.textProperty().addListener((obs, oldVal, newVal) -> {
            try {
                int count = Integer.parseInt(newVal);
                updateMeasurementFields(count);
            } catch (NumberFormatException e) {
                // Bỏ qua nếu không phải số
            }
        });
        updateMeasurementFields(0); // Khởi tạo với 0 lần đo
    }

    private void updateMeasurementFields(int count) {
        measurementsVBox.getChildren().clear();
        measurements.clear();
        for (int i = 0; i < count; i++) {
            HBox hbox = new HBox(10);
            TextField speedField = new TextField();
            speedField.setPromptText("Speed (km/h)");
            TextField rpmField = new TextField();
            rpmField.setPromptText("RPM");
            TextField fuelLevelField = new TextField();
            fuelLevelField.setPromptText("Fuel Level");
            hbox.getChildren().addAll(new Label("Measurement " + (i + 1) + ":"), speedField, rpmField, fuelLevelField);
            measurementsVBox.getChildren().add(hbox);
            measurements.add(new Measurement(speedField, rpmField, fuelLevelField));
        }
    }

    private void populateFields(Journey journey) {
        if (journey != null) {
            vehicleComboBox.setValue(getLicensePlateForVehicleId(journey.getVehicleId()));
            totalTimeField.setText(String.valueOf(journey.getTotalTime()));
            distanceField.setText(String.valueOf(journey.getDistance()));
            // Cập nhật số lần đo dựa trên sensorDataList (nếu có)
            if (!journey.getSensorDataList().isEmpty()) {
                measurementCountField.setText(String.valueOf(journey.getSensorDataList().size()));
                updateMeasurementFields(journey.getSensorDataList().size());
                for (int i = 0; i < journey.getSensorDataList().size(); i++) {
                    SensorReading reading = journey.getSensorDataList().get(i);
                    measurements.get(i).speedField.setText(String.valueOf(reading.getInstantSpeed()));
                    measurements.get(i).rpmField.setText(String.valueOf(reading.getRpm()));
                    measurements.get(i).fuelLevelField.setText(String.valueOf(reading.getFuelConsumption()));
                }
            }
        }
    }

    @FXML
    private void handleSave() {
        try {
            String selectedVehicle = vehicleComboBox.getValue();
            if (selectedVehicle == null) {
                showAlert("Thiếu thông tin", "Vui lòng chọn xe.");
                return;
            }

            String vehicleId = getVehicleIdForLicensePlate(selectedVehicle);
            if (vehicleId == null) {
                showAlert("Lỗi", "Không tìm thấy xe với biển số đã chọn.");
                return;
            }

            double totalTime = Double.parseDouble(totalTimeField.getText().trim());
            double distance = Double.parseDouble(distanceField.getText().trim());
            int measurementCount = Integer.parseInt(measurementCountField.getText().trim());

            if (totalTime <= 0 || distance <= 0 || measurementCount <= 0) {
                showAlert("Thiếu thông tin", "Vui lòng nhập giá trị hợp lệ cho tổng thời gian, quãng đường và số lần đo.");
                return;
            }

            List<SensorReading> sensorReadings = new ArrayList<>();
            for (int i = 0; i < measurementCount; i++) {
                Measurement m = measurements.get(i);
                double speed = Double.parseDouble(m.speedField.getText().trim());
                double rpm = Double.parseDouble(m.rpmField.getText().trim());
                double fuelLevel = Double.parseDouble(m.fuelLevelField.getText().trim());
                if (speed <= 0 || rpm <= 0 || fuelLevel < 0) {
                    showAlert("Lỗi dữ liệu", "Vui lòng nhập giá trị hợp lệ cho vận tốc, RPM và mức nhiên liệu.");
                    return;
                }
                sensorReadings.add(new SensorReading(LocalDateTime.now(), speed, fuelLevel, rpm));
            }

            Journey journey = createJourney(vehicleId, totalTime, distance, sensorReadings);
            if (selectedJourney != null) {
                journey.setId(selectedJourney.getId());
                JourneyDatabase.updateJourney(journey);
            } else {
                JourneyDatabase.addJourney(journey);
                try (VehicleDatabase vehicleDb = new VehicleDatabase()) {
                    Vehicle vehicle = vehicleDb.getVehicleById(vehicleId);
                    if (vehicle != null) {
                        vehicle.incrementJourneyCount();
                        vehicleDb.updateVehicle(vehicle);
                    }
                }
            }
            closeAddJourney();
            if (parentController != null) {
                parentController.refreshJourneys();
            }
        } catch (NumberFormatException e) {
            showAlert("Lỗi dữ liệu", "Vui lòng nhập số hợp lệ.");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Lỗi", "Không thể lưu hành trình: " + e.getMessage());
        }
    }

    private Journey createJourney(String vehicleId, double totalTime, double distance, List<SensorReading> sensorReadings) {
        // Tính toán các thông số tổng hợp
        double averageSpeed = (distance > 0 && totalTime > 0) ? distance / totalTime : 0.0;
        double averageRpm = sensorReadings.stream().mapToDouble(SensorReading::getRpm).average().orElse(0.0);
        double maxRpm = sensorReadings.stream().mapToDouble(SensorReading::getRpm).max().orElse(0.0);
        double fuelConsumption = (sensorReadings.stream().mapToDouble(SensorReading::getFuelConsumption).max().orElse(0.0) -
                sensorReadings.stream().mapToDouble(SensorReading::getFuelConsumption).min().orElse(0.0)) / distance;

        // Tạo Journey
        return new Journey(vehicleId, totalTime, distance, averageSpeed, averageRpm, maxRpm, fuelConsumption, sensorReadings);
    }

    @FXML
    private void handleCancel() {
        closeAddJourney();
    }

    private void closeAddJourney() {
        Stage stage = (Stage) vehicleComboBox.getScene().getWindow();
        stage.close();
    }

    private String getLicensePlateForVehicleId(String vehicleId) {
        try (VehicleDatabase db = new VehicleDatabase()) {
            List<Vehicle> vehicles = db.getAllVehicles();
            for (Vehicle v : vehicles) {
                if (v.getVehicleId().equals(vehicleId)) {
                    return v.getLicensePlate();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Unknown";
    }

    private String getVehicleIdForLicensePlate(String licensePlate) {
        try (VehicleDatabase db = new VehicleDatabase()) {
            List<Vehicle> vehicles = db.getAllVehicles();
            for (Vehicle v : vehicles) {
                if (v.getLicensePlate().equals(licensePlate)) {
                    return v.getVehicleId();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // Lớp nội bộ để quản lý các lần đo
    private static class Measurement {
        TextField speedField;
        TextField rpmField;
        TextField fuelLevelField;

        Measurement(TextField speedField, TextField rpmField, TextField fuelLevelField) {
            this.speedField = speedField;
            this.rpmField = rpmField;
            this.fuelLevelField = fuelLevelField;
        }
    }
}