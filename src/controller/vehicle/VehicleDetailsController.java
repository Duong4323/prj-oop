package controller.vehicle;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import model.journey.Journey;
import model.journey.JourneyDatabase;
import model.vehicle.Vehicle;
import javafx.collections.FXCollections;
import javafx.scene.control.Alert;

import java.time.LocalDateTime;
import java.util.List;

public class VehicleDetailsController {

    @FXML private Label licensePlateLabel, brandLabel, modelLabel, yearLabel;
    @FXML private TableView<Journey> tripTableView;
    @FXML private TableColumn<Journey, String> tripIdColumn;
    @FXML private TableColumn<Journey, LocalDateTime> dateColumn;
    @FXML private TableColumn<Journey, Double> distanceColumn;
    @FXML private TableColumn<Journey, String> destinationColumn;
    @FXML private TableColumn<Journey, Double> avgSpeedColumn; // Thêm cột mới cho avgSpeed

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
        tripIdColumn.setCellValueFactory(new PropertyValueFactory<>("vehicleId"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("startTime"));
        distanceColumn.setCellValueFactory(new PropertyValueFactory<>("distance"));
        avgSpeedColumn.setCellValueFactory(new PropertyValueFactory<>("avgSpeed")); // Gán giá trị avgSpeed
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