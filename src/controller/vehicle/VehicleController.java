package controller.vehicle;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.*;
import javafx.scene.input.MouseEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import model.vehicle.Vehicle;
import model.vehicle.VehicleDatabase;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import java.io.IOException;
import java.util.List;

public class VehicleController {

    @FXML
    private TextField licensePlateField, brandField, modelField, yearField, searchField;

    @FXML
    private ListView<String> vehicleListView;

    private ObservableList<String> vehicleList = FXCollections.observableArrayList();
    private List<Vehicle> vehicleObjects;

    @FXML
    public void initialize() {
        loadVehiclesFromDB();

        // Set custom cell factory for ListView
        vehicleListView.setCellFactory(param -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    // Create HBox with Label and Details button
                    HBox hBox = new HBox(10);
                    hBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                    Label label = new Label(item);
                    Button detailsButton = new Button("Details");
                    detailsButton.setOnAction(event -> {
                        int index = getIndex();
                        if (index >= 0 && index < vehicleObjects.size()) {
                            Vehicle selectedVehicle = vehicleObjects.get(index);
                            try {
                                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/vehicle/VehicleDetails.fxml"));
                                AnchorPane root = loader.load();
                                VehicleDetailsController controller = loader.getController();
                                Stage detailsStage = new Stage();
                                controller.setStage(detailsStage);
                                controller.setVehicle(selectedVehicle);
                                detailsStage.setScene(new Scene(root));
                                detailsStage.setTitle("Vehicle Details: " + selectedVehicle.getLicensePlate());
                                detailsStage.show();
                            } catch (IOException e) {
                                showAlert("Error", "Failed to load vehicle details.");
                                e.printStackTrace();
                            }
                        } else {
                            showAlert("No Selection", "Please select a vehicle to view details.");
                        }
                    });
                    hBox.getChildren().addAll(label, detailsButton);
                    setGraphic(hBox);
                }
            }
        });

        vehicleListView.setOnMouseClicked((MouseEvent event) -> {
            int index = vehicleListView.getSelectionModel().getSelectedIndex();
            if (index >= 0 && index < vehicleObjects.size()) {
                Vehicle selected = vehicleObjects.get(index);
                licensePlateField.setText(selected.getLicensePlate());
                brandField.setText(selected.getBrand());
                modelField.setText(selected.getModel());
                yearField.setText(String.valueOf(selected.getYear()));
            }
        });
    }

    private void loadVehiclesFromDB() {
        try (VehicleDatabase db = new VehicleDatabase()) {
            vehicleObjects = db.getAllVehicles();
            updateListView(vehicleObjects);
        } catch (Exception e) {
            showAlert("Error", "Failed to load vehicles.");
            e.printStackTrace();
        }
    }

    private void updateListView(List<Vehicle> vehicles) {
        vehicleList.clear();
        for (Vehicle v : vehicles) {
            vehicleList.add(formatVehicle(v));
        }
        vehicleListView.setItems(vehicleList);
    }

    private String formatVehicle(Vehicle v) {
        return v.getLicensePlate() + " - " + v.getBrand() + " " + v.getModel() + " (" + v.getYear() + ") - Journeys: " + v.getTotalJourneys();
    }

    @FXML
    private void handleAddVehicle() {
        try {
            String licensePlate = licensePlateField.getText().trim();
            String brand = brandField.getText().trim();
            String model = modelField.getText().trim();
            int year = Integer.parseInt(yearField.getText().trim());

            if (licensePlate.isEmpty() || brand.isEmpty() || model.isEmpty()) {
                showAlert("Missing Fields", "Please fill in all fields.");
                return;
            }

            Vehicle v = new Vehicle(licensePlate, brand, model, year);

            try (VehicleDatabase db = new VehicleDatabase()) {
                db.insertVehicle(v);
            }

            loadVehiclesFromDB();
            clearFields();
        } catch (NumberFormatException e) {
            showAlert("Invalid Year", "Please enter a valid number for the year.");
        } catch (Exception e) {
            showAlert("Error", "Failed to insert vehicle.");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleUpdateVehicle() {
        try {
            String licensePlate = licensePlateField.getText().trim();
            String brand = brandField.getText().trim();
            String model = modelField.getText().trim();
            int year = Integer.parseInt(yearField.getText().trim());

            if (licensePlate.isEmpty()) {
                showAlert("Missing License Plate", "Please select a vehicle to update.");
                return;
            }

            Vehicle existing = vehicleObjects.stream()
                    .filter(v -> v.getLicensePlate().equalsIgnoreCase(licensePlate))
                    .findFirst()
                    .orElse(null);

            if (existing == null) {
                showAlert("Not Found", "Vehicle not found in list.");
                return;
            }

            existing.setBrand(brand);
            existing.setModel(model);
            existing.setYear(year);

            try (VehicleDatabase db = new VehicleDatabase()) {
                db.updateVehicle(existing);
            }

            loadVehiclesFromDB();
            clearFields();
        } catch (NumberFormatException e) {
            showAlert("Invalid Year", "Please enter a valid number for the year.");
        } catch (Exception e) {
            showAlert("Error", "Failed to update vehicle.");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleDeleteVehicle() {
        try {
            String licensePlate = licensePlateField.getText().trim();
            if (licensePlate.isEmpty()) {
                showAlert("Missing License Plate", "Please select a vehicle to delete.");
                return;
            }

            Vehicle existing = vehicleObjects.stream()
                    .filter(v -> v.getLicensePlate().equalsIgnoreCase(licensePlate))
                    .findFirst()
                    .orElse(null);

            if (existing == null) {
                showAlert("Not Found", "Vehicle not found.");
                return;
            }

            try (VehicleDatabase db = new VehicleDatabase()) {
                db.deleteVehicleById(existing.getVehicleId());
            }

            loadVehiclesFromDB();
            clearFields();
        } catch (Exception e) {
            showAlert("Error", "Failed to delete vehicle.");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSearchVehicle() {
        String keyword = searchField.getText().trim();
        if (keyword.isEmpty()) {
            loadVehiclesFromDB();
            return;
        }

        try (VehicleDatabase db = new VehicleDatabase()) {
            vehicleObjects = db.searchVehicleByLicensePlate(keyword);
            updateListView(vehicleObjects);
        } catch (Exception e) {
            showAlert("Error", "Failed to search vehicles.");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleShowVehicleDetails() {
        // This method is no longer used directly by the FXML but kept for potential future use
        int index = vehicleListView.getSelectionModel().getSelectedIndex();
        if (index >= 0 && index < vehicleObjects.size()) {
            Vehicle selectedVehicle = vehicleObjects.get(index);
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/vehicle/VehicleDetails.fxml"));
                AnchorPane root = loader.load();
                VehicleDetailsController controller = loader.getController();
                Stage detailsStage = new Stage();
                controller.setStage(detailsStage);
                controller.setVehicle(selectedVehicle);
                detailsStage.setScene(new Scene(root));
                detailsStage.setTitle("Vehicle Details: " + selectedVehicle.getLicensePlate());
                detailsStage.show();
            } catch (IOException e) {
                showAlert("Error", "Failed to load vehicle details.");
                e.printStackTrace();
            }
        } else {
            showAlert("No Selection", "Please select a vehicle to view details.");
        }
    }

    private void clearFields() {
        licensePlateField.clear();
        brandField.clear();
        modelField.clear();
        yearField.clear();
        searchField.clear();
        vehicleListView.getSelectionModel().clearSelection();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}