package client;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import lombok.Getter;
import messages.RegistrationRequest;

import java.io.IOException;

@Getter
public class ControllerRegScene {
    public TextField regLoginField;
    public PasswordField regPasswordField;
    public Button regConfirmButton;
    private NetworkService networkService;

    public ControllerRegScene(NetworkService networkService){
        this.networkService = networkService;
    }

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



}
