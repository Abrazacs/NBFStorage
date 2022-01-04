package client;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;
import messages.AbstractMessage;
import messages.MessageType;
import messages.RegistrationConfirmed;
import messages.RegistrationRequest;

import java.io.IOException;

@Getter
public class ControllerRegScene implements MessageProcessor{
    public TextField regLoginField;
    public PasswordField regPasswordField;
    public Button regConfirmButton;
    @Setter
    private NetworkService networkService;
    @Setter
    private Stage stage;


    public void signIn(ActionEvent event) throws IOException {
        String login = regLoginField.getText();
        String password = regPasswordField.getText();
        if (!login.isEmpty() && !password.isEmpty()){
           networkService.getOs().writeObject(new RegistrationRequest(login,password));
        }
        else {
            Platform.runLater(()->{
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("ERROR");
                alert.setHeaderText("Please complete the form");
                alert.showAndWait();
            });
        }

    }


    @Override
    public void processMessage(AbstractMessage message) {
        if (message.getMessageType().equals(MessageType.REGISTRATION_CONFIRMED)){
            RegistrationConfirmed regConf = (RegistrationConfirmed) message;
            isRegistrationConfirmed(regConf);
        }
    }

    private void isRegistrationConfirmed (RegistrationConfirmed confirmation) {
        if (confirmation.isConfirmed()) {
            Platform.runLater(() -> {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("client.fxml"));
                    Scene scene = new Scene(loader.load());
                    stage.setScene(scene);
                    stage.show();
                    ControllerLoginScene logScene = loader.getController();
                    logScene.setNetworkService(networkService);
                    networkService.setMsgProcessor(logScene);
                    logScene.setStage(stage);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } else {
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("ERROR");
                alert.setHeaderText("Registration has been failed. Please try again");
                alert.showAndWait();
            });
        }
    }
}
