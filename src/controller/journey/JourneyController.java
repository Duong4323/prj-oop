package controller.journey;

import javafx.collections.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.journey.Journey;
import model.journey.JourneyDatabase;
import model.journey.PerformanceReport;
import model.journey.SensorReading;
import model.vehicle.Vehicle;
import model.vehicle.VehicleDatabase;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class JourneyController {

    @FXML private Button addButton;
    @FXML private ListView<HBox> journeyListView;
    @FXML private Button generateReportButton;
    @FXML private TextField searchField;
    @FXML private Button searchButton;
    @FXML private Button importCsvButton;

    private ObservableList<HBox> journeyList = FXCollections.observableArrayList();
    private Stage addStage;

    @FXML
    public void initialize() {
        loadJourneys();
        setupListView();
        addButton.setOnAction(event -> showAddJourney());
        importCsvButton.setOnAction(event -> importFromCsv());
        searchButton.setOnAction(event -> setupSearch());
    }

    private void loadJourneys() {
        journeyList.clear();
        List<Journey> journeys = JourneyDatabase.getAllJourneys();
        for (Journey journey : journeys) {
            addJourneyToList(journey);
        }
        journeyListView.setItems(journeyList);
    }

    private void setupListView() {
        journeyListView.setCellFactory(param -> new ListCell<HBox>() {
            @Override
            protected void updateItem(HBox item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    setGraphic(item);
                }
            }
        });
    }

    private void addJourneyToList(Journey journey) {
        HBox hbox = new HBox(10);
        StringBuilder journeyInfo = new StringBuilder();
        journeyInfo.append("Xe: ").append(getLicensePlateForVehicleId(journey.getVehicleId()))
                  .append(" | Quãng đường: ").append(journey.getDistance()).append(" km")
                  .append(" | Tổng thời gian: ").append(String.format("%.2f", journey.getTotalTime())).append(" giờ");
        Label label = new Label(journeyInfo.toString());
        Button editButton = new Button("Edit");
        Button deleteButton = new Button("Delete");

        editButton.setOnAction(e -> editJourney(journey));
        deleteButton.setOnAction(e -> deleteJourney(journey));

        hbox.getChildren().addAll(label, editButton, deleteButton);
        journeyList.add(hbox);
    }

    @FXML
    private void setupSearch() {
        String searchText = searchField.getText().trim().toLowerCase();
        journeyList.clear();
        List<Journey> journeys = JourneyDatabase.getAllJourneys();
        for (Journey journey : journeys) {
            String licensePlate = getLicensePlateForVehicleId(journey.getVehicleId()).toLowerCase();
            String distance = String.valueOf(journey.getDistance()).toLowerCase();
            if (licensePlate.contains(searchText) || distance.contains(searchText)) {
                addJourneyToList(journey);
            }
        }
    }

    @FXML
    private void showAddJourney() {
        try {
            if (addStage == null || !addStage.isShowing()) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/journey/AddJourney.fxml"));
                Parent root = loader.load();
                AddJourneyController controller = loader.getController();
                controller.setParentController(this);
                controller.initializeAddForm(null);
                addStage = new Stage();
                addStage.setTitle("Add New Journey");
                addStage.setScene(new Scene(root));
                addStage.setOnHidden(e -> loadJourneys());
                addStage.show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Lỗi", "Không thể mở cửa sổ thêm hành trình.");
        }
    }

    private void editJourney(Journey journey) {
        try {
            if (addStage == null || !addStage.isShowing()) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/journey/AddJourney.fxml"));
                Parent root = loader.load();
                AddJourneyController controller = loader.getController();
                controller.setParentController(this);
                controller.initializeAddForm(journey);
                addStage = new Stage();
                addStage.setTitle("Edit Journey");
                addStage.setScene(new Scene(root));
                addStage.setOnHidden(e -> loadJourneys());
                addStage.show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Lỗi", "Không thể mở cửa sổ chỉnh sửa hành trình.");
        }
    }

    private void deleteJourney(Journey journey) {
        String vehicleId = journey.getVehicleId();
        JourneyDatabase.deleteJourney(journey.getId());
        
        try (VehicleDatabase vehicleDb = new VehicleDatabase()) {
            Vehicle vehicle = vehicleDb.getVehicleById(vehicleId);
            if (vehicle != null) {
                vehicle.decrementJourneyCount();
                vehicleDb.updateVehicle(vehicle);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Lỗi", "Không thể cập nhật tổng số hành trình của phương tiện: " + e.getMessage());
        }
        
        loadJourneys();
    }

    @FXML
    private void handleGenerateReport() {
        int selectedIndex = journeyListView.getSelectionModel().getSelectedIndex();
        if (selectedIndex < 0) {
            showAlert("Lỗi", "Vui lòng chọn một hành trình để tạo báo cáo.");
            return;
        }
        List<Journey> journeys = JourneyDatabase.getAllJourneys();
        if (selectedIndex < journeys.size()) {
            Journey journey = journeys.get(selectedIndex);
            PerformanceReport report = journey.generatePerformanceReport();
            showAlert("Báo cáo hiệu suất", report.toString());
        }
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

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public void refreshJourneys() {
        loadJourneys();
    }

    @FXML
    public void importFromCsv() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Chọn file CSV");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File selectedFile = fileChooser.showOpenDialog(addStage != null ? addStage : new Stage());

        if (selectedFile != null) {
            try (BufferedReader br = new BufferedReader(new FileReader(selectedFile))) {
                String line;
                boolean firstLine = true; // Bỏ qua header nếu có
                while ((line = br.readLine()) != null) {
                    if (firstLine) {
                        firstLine = false;
                        continue;
                    }
                    String[] data = line.split(",");
                    if (data.length < 3) continue; // Đảm bảo đủ cột cơ bản (vehicleId, totalTime, distance)

                    String vehicleId = data[0].trim();
                    double totalTime = Double.parseDouble(data[1].trim());
                    double distance = Double.parseDouble(data[2].trim());

                    List<SensorReading> sensorReadings = new ArrayList<>();
                    int measurementCount = 0;
                    for (int i = 3; i < data.length; i++) {
                        String[] sensorData = data[i].split("\\|");
                        if (sensorData.length == 3) { // Chỉ cần instantSpeed, fuelConsumption, rpm
                            LocalDateTime timestamp = LocalDateTime.now(); // Gán thời gian hiện tại
                            double instantSpeed = Double.parseDouble(sensorData[0].trim());
                            double sensorFuelConsumption = Double.parseDouble(sensorData[1].trim());
                            double rpm = Double.parseDouble(sensorData[2].trim());
                            sensorReadings.add(new SensorReading(timestamp, instantSpeed, sensorFuelConsumption, rpm));
                            measurementCount++;
                        }
                    }

                    // Tính toán các giá trị dựa trên sensorReadings
                    double averageSpeed = calculateAverageSpeed(sensorReadings, totalTime, distance);
                    double averageRpm = calculateAverageRpm(sensorReadings);
                    double maxRpm = calculateMaxRpm(sensorReadings);
                    double fuelConsumption = calculateFuelConsumption(sensorReadings, distance);

                    Journey journey = new Journey(vehicleId, totalTime, distance, averageSpeed, averageRpm, maxRpm, fuelConsumption, sensorReadings);
                    journey.setMeasurementCount(measurementCount);
                    JourneyDatabase.addJourney(journey);
                }
                loadJourneys(); // Cập nhật danh sách sau khi import
            } catch (IOException | NumberFormatException e) {
                e.printStackTrace();
                showAlert("Lỗi", "Không thể nhập file CSV: " + e.getMessage());
            }
        }
    }

    private double calculateAverageSpeed(List<SensorReading> sensorReadings, double totalTime, double distance) {
        if (sensorReadings.isEmpty() || totalTime <= 0) {
            return distance / totalTime; // Dùng distance và totalTime nếu có
        }
        double totalSpeed = sensorReadings.stream().mapToDouble(SensorReading::getInstantSpeed).sum();
        return totalSpeed / sensorReadings.size();
    }

    private double calculateAverageRpm(List<SensorReading> sensorReadings) {
        if (sensorReadings.isEmpty()) return 0.0;
        double totalRpm = sensorReadings.stream().mapToDouble(SensorReading::getRpm).sum();
        return totalRpm / sensorReadings.size();
    }

    private double calculateMaxRpm(List<SensorReading> sensorReadings) {
        if (sensorReadings.isEmpty()) return 0.0;
        return sensorReadings.stream().mapToDouble(SensorReading::getRpm).max().orElse(0.0);
    }

    private double calculateFuelConsumption(List<SensorReading> sensorReadings, double distance) {
        if (sensorReadings.isEmpty() || distance <= 0) return 0.0;
        double totalFuel = sensorReadings.stream().mapToDouble(SensorReading::getFuelConsumption).sum();
        return (totalFuel / distance) * 100; // L/100km
    }
}