package client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Objects;

public class App extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(this.getClass().getResource("client.fxml"));
        Parent parent = loader.load();
        ControllerLoginScene controller = loader.getController();
        controller.setStage(primaryStage);
        primaryStage.setScene(new Scene(parent));
        primaryStage.show();

    }
}
