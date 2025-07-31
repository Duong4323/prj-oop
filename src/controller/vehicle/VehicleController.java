package controller.vehicle;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.*;
import javafx.scene.input.MouseEvent;
import model.vehicle.Vehicle;
import model.vehicle.VehicleDatabase;

import java.util.List;

public class VehicleController {

    @FXML
    private TextField idField, brandField, modelField, yearField;

    @FXML
    private ListView<String> vehicleListView;

    private ObservableList<String> vehicleList = FXCollections.observableArrayList();
    private List<Vehicle> vehicleObjects;

    @FXML
    public void initialize() {
        loadVehiclesFromDB();

        // Gán sự kiện click chuột chọn xe
        vehicleListView.setOnMouseClicked((MouseEvent event) -> {
            int index = vehicleListView.getSelectionModel().getSelectedIndex();
            if (index >= 0 && index < vehicleObjects.size()) {
                Vehicle selected = vehicleObjects.get(index);
                idField.setText(selected.getId());
                brandField.setText(selected.getBrand());
                modelField.setText(selected.getModel());
                yearField.setText(String.valueOf(selected.getYear()));
            }
        });
    }

    private void loadVehiclesFromDB() {
        try (VehicleDatabase db = new VehicleDatabase()) {
            vehicleObjects = db.getAllVehicles();

            vehicleList.clear();
            for (Vehicle v : vehicleObjects) {
                vehicleList.add(formatVehicle(v));
            }

            vehicleListView.setItems(vehicleList);
        } catch (Exception e) {
            showAlert("Error", "Failed to load vehicles.");
            e.printStackTrace();
        }
    }

    private String formatVehicle(Vehicle v) {
        return v.getId() + " - " + v.getBrand() + " " + v.getModel() + " (" + v.getYear() + ")";
    }

    @FXML
    private void handleAddVehicle() {
        try {
            String id = idField.getText().trim();
            String brand = brandField.getText().trim();
            String model = modelField.getText().trim();
            int year = Integer.parseInt(yearField.getText().trim());

            if (id.isEmpty() || brand.isEmpty() || model.isEmpty()) {
                showAlert("Missing Fields", "Please fill in all fields.");
                return;
            }

            Vehicle v = new Vehicle(id, brand, model, year);
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
            String id = idField.getText().trim();
            String brand = brandField.getText().trim();
            String model = modelField.getText().trim();
            int year = Integer.parseInt(yearField.getText().trim());

            if (id.isEmpty()) {
                showAlert("Missing ID", "Please select a vehicle to update.");
                return;
            }

            Vehicle v = new Vehicle(id, brand, model, year);
            try (VehicleDatabase db = new VehicleDatabase()) {
                db.updateVehicle(v);
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
            String id = idField.getText().trim();
            if (id.isEmpty()) {
                showAlert("Missing ID", "Please select a vehicle to delete.");
                return;
            }

            try (VehicleDatabase db = new VehicleDatabase()) {
                db.deleteVehicleById(id);
            }

            loadVehiclesFromDB();
            clearFields();
        } catch (Exception e) {
            showAlert("Error", "Failed to delete vehicle.");
            e.printStackTrace();
        }
    }

    private void clearFields() {
        idField.clear();
        brandField.clear();
        modelField.clear();
        yearField.clear();
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
