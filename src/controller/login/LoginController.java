package controller.login;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import model.login.LoginModel;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    private LoginModel loginModel = new LoginModel();

    @FXML
    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (loginModel.authenticate(username, password)) {
            showMainView();
        } else {
            showAlert("Login Failed", "Invalid username or password.");
        }
    }

    private void showMainView() {
    try {
        System.out.println("Loading /view/main/main_view.fxml from: " + getClass().getResource("/view/main/main_view.fxml"));
        Parent mainView = FXMLLoader.load(getClass().getResource("/view/main/main_view.fxml"));
        if (mainView == null) {
            throw new IOException("Resource /view/main/main_view.fxml not found.");
        }
        Stage stage = (Stage) usernameField.getScene().getWindow();
        stage.setScene(new Scene(mainView, 1000, 600));
        stage.setTitle("Vehicle Performance Tracker - Dashboard");
    } catch (IOException e) {
        e.printStackTrace();
        showAlert("Error", "Cannot load main view. " + e.getMessage());
        System.out.println("Loading /view/main/main_view.fxml from: " + getClass().getResource("/view/main/main_view.fxml"));
    }
    
}

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
