package client;

import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;
import messages.*;

import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

@Getter
public class ControllerLoginScene implements Initializable {

    public TextField loginInputField;
    public PasswordField passwordInputField;
    public Button enterButton;
    public AnchorPane loginScene;
    private NetworkService networkService;
    private User user;
    @Setter
    private Stage stage;



    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.networkService = new NetworkService();
    }

    public void login(ActionEvent event) throws IOException {
        if (loginInputField.getText().isEmpty() || passwordInputField.getText().isEmpty()) {
            return;
        }

        user = new User(loginInputField.getText(), passwordInputField.getText());
        networkService.getOs().writeObject(user);
        networkService.setStage(stage);
    }

    public void showRegistrationForm(ActionEvent event) {
    }


//
//
//
//    private void isRegistrationConfirmed (RegistrationConfirmed confirmation){
//        if(confirmation.isConfirmed()){
//            Platform.runLater(()->{
//                try {
//                    Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("client.fxml")));
//                    stage = (Stage) regConfirmButton.getScene().getWindow();
//                    scene = new Scene(root);
//                    stage.setScene(scene);
//                    stage.show();
//                }catch (IOException e){
//                    e.printStackTrace();
//                }
//            });
//        }else {
//            Platform.runLater(()->{
//                Alert alert = new Alert(Alert.AlertType.ERROR);
//                alert.setTitle("ERROR");
//                alert.setHeaderText("Registration has been failed. Please try again");
//                alert.showAndWait();
//            });
//        }
//
//    }


}
