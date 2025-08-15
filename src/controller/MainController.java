package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.scene.Node;

import java.io.IOException;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import model.MongoDBConnection;

public class MainController {

    @FXML
    private StackPane mainContentPane;

    @FXML
    private VBox homeContent; // lưu home dashboard

    @FXML
    private Label totalVehicles;

    @FXML
    private Label totalJourneys;

    @FXML
    public void initialize() {
        // Mặc định hiển thị Home khi khởi động
        mainContentPane.getChildren().setAll(homeContent);
        updateDashboardStats();
    }

    @FXML
    private void goToVehicleManager() {
        loadView("/view/vehicle/vehicle_view.fxml");
    }

    @FXML
    private void goToJourneyManager() {
        loadView("/view/journey/JourneyView.fxml");
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/login/login_view.fxml"));
            Parent loginRoot = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(loginRoot);
            stage.setScene(scene);
            stage.setTitle("Login");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleHome() {
        // Hiển thị lại VBox homeContent thay vì clear toàn bộ
        mainContentPane.getChildren().setAll(homeContent);
        updateDashboardStats();
    }

    private void loadView(String fxmlPath) {
        try {
            Parent content = FXMLLoader.load(getClass().getResource(fxmlPath));
            mainContentPane.getChildren().setAll(content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateDashboardStats() {
        try {
            MongoDatabase db = MongoDBConnection.getDatabase();
            MongoCollection<Document> vehicleCollection = db.getCollection("vehicles");
            MongoCollection<Document> journeyCollection = db.getCollection("journeys");

            long totalVehicleCount = vehicleCollection.countDocuments();
            long totalJourneyCount = journeyCollection.countDocuments();

            if (totalVehicles != null) {
                totalVehicles.setText(String.valueOf(totalVehicleCount));
            }
            if (totalJourneys != null) {
                totalJourneys.setText(String.valueOf(totalJourneyCount));
            }

            System.out.println("📊 Vehicles: " + totalVehicleCount + " | Journeys: " + totalJourneyCount);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
