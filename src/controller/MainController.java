package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.scene.Node;

import java.io.IOException;

public class MainController {

    @FXML
    private StackPane mainContentPane;

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
        // Load lại login.fxml
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/login/login_view.fxml"));
        Parent loginRoot = loader.load();

        // Lấy stage hiện tại
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

        // Đặt scene mới là login
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
    // Ví dụ: load lại trang chính, hoặc xóa nội dung trung tâm
    mainContentPane.getChildren().clear();  // nếu bạn dùng StackPane
}


    private void loadView(String fxmlPath) {
        try {
            Parent content = FXMLLoader.load(getClass().getResource(fxmlPath));
            mainContentPane.getChildren().setAll(content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
