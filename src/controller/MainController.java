package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Node;

import java.io.IOException;

public class MainController {

    // @FXML
    // private void goToVehicleManager(ActionEvent event) {
    //     try {
    //         Parent vehicleView = FXMLLoader.load(getClass().getResource("/view/vehicle/vehicle_view.fxml"));
    //         Scene scene = new Scene(vehicleView);
    //         Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
    //         stage.setScene(scene);
    //     } catch (IOException e) {
    //         e.printStackTrace();
    //     }
    // }

    // @FXML
    // private void goToTripManager(ActionEvent event) {
    //     try {
    //         Parent tripView = FXMLLoader.load(getClass().getResource("/view/trip/trip_view.fxml"));
    //         Scene scene = new Scene(tripView);
    //         Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
    //         stage.setScene(scene);
    //     } catch (IOException e) {
    //         e.printStackTrace();
    //     }
    // }

    @FXML
    private void handleLogout(ActionEvent event) {
        try {
            Parent loginView = FXMLLoader.load(getClass().getResource("/view/login/login_view.fxml"));
            Scene scene = new Scene(loginView);
            Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
