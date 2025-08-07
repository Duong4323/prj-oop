import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import model.MongoDBConnection; // import lớp kết nối

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // Kiểm tra kết nối MongoDB
            MongoDBConnection.getDatabase(); // Khởi tạo kết nối (nếu lỗi sẽ throw exception)

            // Load giao diện
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/login/login_view.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root);
            primaryStage.setTitle("Ứng dụng theo dõi hiệu suất xe");
            primaryStage.setScene(scene);
            primaryStage.setResizable(false);
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
