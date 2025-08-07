package controller.vehicle;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import model.journey.Journey;
import model.journey.JourneyDatabase;
import model.journey.PerformanceReport;
import model.vehicle.Vehicle;
import javafx.collections.FXCollections;
import javafx.scene.control.Alert;

import java.time.Duration;
import java.util.List;

public class VehicleDetailsController {

    @FXML private Label licensePlateLabel, brandLabel, modelLabel, yearLabel;
    @FXML private TableView<Journey> tripTableView;
    @FXML private TableColumn<Journey, String> tripIdColumn;
    @FXML private TableColumn<Journey, Double> averageSpeedColumn;
    @FXML private TableColumn<Journey, Double> averageRpmColumn;
    @FXML private TableColumn<Journey, Double> averageFuelConsumptionColumn;
    @FXML private TableColumn<Journey, Double> distanceColumn;
    @FXML private TableColumn<Journey, String> totalTimeColumn;

    private Stage stage;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setVehicle(Vehicle vehicle) {
        licensePlateLabel.setText(vehicle.getLicensePlate());
        brandLabel.setText(vehicle.getBrand());
        modelLabel.setText(vehicle.getModel());
        yearLabel.setText(String.valueOf(vehicle.getYear()));
        loadTrips(vehicle.getVehicleId());
    }

    @FXML
    private void initialize() {
        // Gán các cột với giá trị từ Journey
        tripIdColumn.setCellValueFactory(new PropertyValueFactory<>("vehicleId"));
        averageSpeedColumn.setCellValueFactory(new PropertyValueFactory<>("averageSpeed"));
        averageRpmColumn.setCellValueFactory(new PropertyValueFactory<>("averageRpm"));
        averageFuelConsumptionColumn.setCellValueFactory(cellData -> {
            Journey journey = cellData.getValue();
            PerformanceReport report = journey.generatePerformanceReport();
            return new javafx.beans.property.SimpleDoubleProperty(report.getAverageFuelConsumption()).asObject();
        });
        distanceColumn.setCellValueFactory(new PropertyValueFactory<>("distance"));
        totalTimeColumn.setCellValueFactory(cellData -> {
            Journey journey = cellData.getValue();
            PerformanceReport report = journey.generatePerformanceReport();
            long hours = report.getTotalOperatingTime().toHours();
            return new javafx.beans.property.SimpleStringProperty(String.format("%d giờ", hours));
        });
    }

    private void loadTrips(String vehicleId) {
        try {
            List<Journey> trips = JourneyDatabase.getAllJourneys().stream()
                    .filter(j -> j.getVehicleId().equals(vehicleId))
                    .collect(java.util.stream.Collectors.toList());
            tripTableView.setItems(FXCollections.observableArrayList(trips));
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Failed to load trip history.");
            alert.showAndWait();
        }
    }

    @FXML
    private void handleClose() {
        if (stage != null) {
            stage.close();
        }
    }
}