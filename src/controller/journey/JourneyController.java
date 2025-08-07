package controller.journey;

import javafx.collections.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import model.journey.Journey;
import model.journey.JourneyDatabase;
import model.journey.PerformanceReport;
import model.vehicle.Vehicle;
import model.vehicle.VehicleDatabase;

import java.io.IOException;
import java.util.List;

public class JourneyController {

    @FXML private Button addButton;
    @FXML private ListView<HBox> journeyListView;
    @FXML private Button generateReportButton;
    @FXML private TextField searchField;
    @FXML private Button searchButton;

    private ObservableList<HBox> journeyList = FXCollections.observableArrayList();
    private Stage addStage;

    @FXML
    public void initialize() {
        loadJourneys();
        setupListView();
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
                  .append(" | Time: ").append(journey.getStartTime() != null ? journey.getStartTime() : "N/A")
                  .append(" - ").append(journey.getEndTime() != null ? journey.getEndTime() : "N/A");
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
                controller.setParentController(this); // Truyền tham chiếu để cập nhật danh sách
                controller.initializeAddForm(null); // Khởi tạo form thêm mới
                addStage = new Stage();
                addStage.setTitle("Add New Journey");
                addStage.setScene(new Scene(root));
                addStage.setOnHidden(e -> loadJourneys()); // Tải lại danh sách khi đóng
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
                controller.setParentController(this); // Truyền tham chiếu để cập nhật danh sách
                controller.initializeAddForm(journey); // Khởi tạo form chỉnh sửa
                addStage = new Stage();
                addStage.setTitle("Edit Journey");
                addStage.setScene(new Scene(root));
                addStage.setOnHidden(e -> loadJourneys()); // Tải lại danh sách khi đóng
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
        
        // Cập nhật tổng số hành trình của phương tiện
        try (VehicleDatabase vehicleDb = new VehicleDatabase()) {
            Vehicle vehicle = vehicleDb.getVehicleById(vehicleId);
            if (vehicle != null) {
                vehicle.decrementJourneyCount(); // Giảm số lượng hành trình
                vehicleDb.updateVehicle(vehicle); // Lưu thay đổi
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

    // Phương thức để AddJourneyController gọi khi cần cập nhật danh sách
    public void refreshJourneys() {
        loadJourneys();
    }
}