import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/login/login_view.fxml"));
            Scene scene = new Scene(root, 800, 600);
            primaryStage.setTitle("Vehicle Performance Tracker - Login");
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace(); // Log lỗi nếu không load được FXML
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
