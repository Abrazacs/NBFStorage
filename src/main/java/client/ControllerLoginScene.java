package client;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;
import messages.*;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

@Getter
public class ControllerLoginScene implements Initializable, MessageProcessor {

    public TextField loginInputField;
    public PasswordField passwordInputField;
    public Button enterButton;
    public AnchorPane loginScene;
    @Setter
    private NetworkService networkService;
    private User user;
    @Setter
    private Stage stage;



    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.networkService = new NetworkService(this);
    }

    public void login(ActionEvent event) throws IOException {
        if (loginInputField.getText().isEmpty() || passwordInputField.getText().isEmpty()) {
            return;
        }

        user = new User(loginInputField.getText(), passwordInputField.getText());
        networkService.getOs().writeObject(user);
    }

    public void showRegistrationForm(ActionEvent event) throws IOException{
        FXMLLoader loader = new FXMLLoader(getClass().getResource("registrationForm.fxml"));
        Scene scene = new Scene(loader.load());
        stage.setScene(scene);
        stage.show();
        ControllerRegScene regScene = loader.getController();
        regScene.setNetworkService(networkService);
        networkService.setMsgProcessor(regScene);
        regScene.setStage(stage);
    }

    @Override
    public void processMessage(AbstractMessage message) {
        if (message.getMessageType().equals(MessageType.USER_CONFIRMATION)){
            UserConfirmation confirmation = (UserConfirmation) message;
            checkConfirmationOfAuthorization(confirmation);
        }
    }

    private void checkConfirmationOfAuthorization(UserConfirmation confirmation) {
        if(confirmation.isAuthorized()){
            Platform.runLater(()->{
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("mainScene.fxml"));
                    Scene scene = new Scene(loader.load());
                    stage.setScene(scene);
                    stage.show();
                    ControllerMainScene mainScene = loader.getController();
                    mainScene.setNetworkService(networkService);
                    networkService.setMsgProcessor(mainScene);
                    mainScene.launch();
                }catch (IOException e){
                    e.printStackTrace();
                }
            });
        }else {
            Platform.runLater(()->{
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("ERROR");
                alert.setHeaderText("Wrong credentials");
                alert.showAndWait();
            });
        }
    }


}
