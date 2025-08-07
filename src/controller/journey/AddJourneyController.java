package controller.journey;

import javafx.fxml.FXML;
import javafx.scene.control.*;
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
    @FXML private DatePicker startDatePicker;
    @FXML private Spinner<Integer> startHourSpinner;
    @FXML private Spinner<Integer> startMinuteSpinner;
    @FXML private DatePicker endDatePicker;
    @FXML private Spinner<Integer> endHourSpinner;
    @FXML private Spinner<Integer> endMinuteSpinner;
    @FXML private TextField distanceField;
    @FXML private DatePicker sensorDatePicker;
    @FXML private Spinner<Integer> sensorHourSpinner;
    @FXML private Spinner<Integer> sensorMinuteSpinner;
    @FXML private TextField sensorSpeedField;
    @FXML private TextField sensorFuelField;
    @FXML private TextField sensorRpmField;

    private Journey selectedJourney;
    private JourneyController parentController;

    @FXML
    public void initialize() {
        // Khởi tạo Spinner
        startHourSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23, 0));
        startMinuteSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59, 0));
        endHourSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23, 0));
        endMinuteSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59, 0));
        sensorHourSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23, 0));
        sensorMinuteSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59, 0));
    }

    public void setParentController(JourneyController parentController) {
        this.parentController = parentController;
    }

    public void initializeAddForm(Journey journey) {
        try (VehicleDatabase db = new VehicleDatabase()) {
            List<Vehicle> vehicles = db.getAllVehicles();
            vehicleComboBox.getItems().clear();
            for (Vehicle v : vehicles) {
                vehicleComboBox.getItems().add(v.getLicensePlate());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (journey != null) {
            selectedJourney = journey;
            vehicleComboBox.setValue(getLicensePlateForVehicleId(journey.getVehicleId()));
            startDatePicker.setValue(journey.getStartTime().toLocalDate());
            startHourSpinner.getValueFactory().setValue(journey.getStartTime().getHour());
            startMinuteSpinner.getValueFactory().setValue(journey.getStartTime().getMinute());
            endDatePicker.setValue(journey.getEndTime().toLocalDate());
            endHourSpinner.getValueFactory().setValue(journey.getEndTime().getHour());
            endMinuteSpinner.getValueFactory().setValue(journey.getEndTime().getMinute());
            distanceField.setText(String.valueOf(journey.getDistance()));
            if (!journey.getSensorDataList().isEmpty()) {
                SensorReading reading = journey.getSensorDataList().get(0);
                sensorDatePicker.setValue(reading.getTimestamp().toLocalDate());
                sensorHourSpinner.getValueFactory().setValue(reading.getTimestamp().getHour());
                sensorMinuteSpinner.getValueFactory().setValue(reading.getTimestamp().getMinute());
                sensorFuelField.setText(String.valueOf(reading.getFuelConsumption()));
                sensorRpmField.setText(String.valueOf(reading.getRpm()));
            }
        } else {
            selectedJourney = null;
            clearFields();
        }
    }

    @FXML
    private void handleAddJourney() {
        try {
            String selectedVehicle = vehicleComboBox.getValue();
            if (selectedVehicle == null) {
                showAlert("Thiếu thông tin", "Vui lòng chọn xe từ danh sách.");
                return;
            }

            String vehicleId = getVehicleIdForLicensePlate(selectedVehicle);
            if (vehicleId == null) {
                showAlert("Lỗi", "Không tìm thấy xe với biển số đã chọn.");
                return;
            }

            if (startDatePicker.getValue() == null || endDatePicker.getValue() == null) {
                showAlert("Thiếu thông tin", "Vui lòng chọn ngày bắt đầu và kết thúc.");
                return;
            }
            int startHour = startHourSpinner.getValue();
            int startMinute = startMinuteSpinner.getValue();
            int endHour = endHourSpinner.getValue();
            int endMinute = endMinuteSpinner.getValue();
            LocalDateTime start = startDatePicker.getValue().atTime(startHour, startMinute);
            LocalDateTime end = endDatePicker.getValue().atTime(endHour, endMinute);
            double distance = Double.parseDouble(distanceField.getText().trim());

            if (distance <= 0) {
                showAlert("Thiếu thông tin", "Vui lòng nhập quãng đường hợp lệ.");
                return;
            }

            List<SensorReading> sensorReadings = new ArrayList<>();
            if (sensorDatePicker.getValue() != null && !sensorSpeedField.getText().trim().isEmpty() &&
                !sensorFuelField.getText().trim().isEmpty() && !sensorRpmField.getText().trim().isEmpty()) {
                int sensorHour = sensorHourSpinner.getValue();
                int sensorMinute = sensorMinuteSpinner.getValue();
                double speed = Double.parseDouble(sensorSpeedField.getText().trim());
                double fuel = Double.parseDouble(sensorFuelField.getText().trim());
                double rpm = Double.parseDouble(sensorRpmField.getText().trim());

                if (speed <= 0 || fuel < 0 || rpm <= 0) {
                    showAlert("Lỗi dữ liệu", "Vui lòng nhập đầy đủ và hợp lệ các trường cảm biến.");
                    return;
                }

                LocalDateTime sensorTime = sensorDatePicker.getValue().atTime(sensorHour, sensorMinute);
                sensorReadings.add(new SensorReading(sensorTime, speed, fuel, rpm));
            } else {
                showAlert("Thiếu thông tin", "Vui lòng nhập ít nhất một bộ dữ liệu cảm biến.");
                return;
            }

            Journey journey = new Journey(vehicleId, start, end, distance, sensorReadings);
            if (selectedJourney != null) {
                journey.setId(selectedJourney.getId());
                JourneyDatabase.updateJourney(journey);
            } else {
                JourneyDatabase.addJourney(journey);
            }
            if (parentController != null) {
                parentController.refreshJourneys(); // Cập nhật danh sách trong JourneyController
            }
            closeAddJourney();
        } catch (NumberFormatException e) {
            showAlert("Lỗi dữ liệu", "Vui lòng nhập số hợp lệ cho quãng đường, tốc độ, nhiên liệu, và RPM.");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Lỗi", "Vui lòng kiểm tra dữ liệu nhập.");
        }
    }

    @FXML
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

    private void clearFields() {
        vehicleComboBox.setValue(null);
        startDatePicker.setValue(null);
        startHourSpinner.getValueFactory().setValue(0);
        startMinuteSpinner.getValueFactory().setValue(0);
        endDatePicker.setValue(null);
        endHourSpinner.getValueFactory().setValue(0);
        endMinuteSpinner.getValueFactory().setValue(0);
        distanceField.clear();
        sensorDatePicker.setValue(null);
        sensorHourSpinner.getValueFactory().setValue(0);
        sensorMinuteSpinner.getValueFactory().setValue(0);
        sensorSpeedField.clear();
        sensorFuelField.clear();
        sensorRpmField.clear();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}