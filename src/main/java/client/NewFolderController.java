package client;

import javafx.application.Platform;
import javafx.event.ActionEvent;

import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import lombok.Setter;
import messages.NewFolder;

import java.io.IOException;


public class NewFolderController {
    public TextField inputField;
    @Setter
    private Stage stage;
    @Setter
    private NetworkService networkService;


    public void confirmName(ActionEvent event) throws IOException {
        if (!inputField.getText().isEmpty()) {
            networkService.sendMessage(new NewFolder(inputField.getText()));
            stage.close();
        } else{
            Platform.runLater(()->{
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Folder name");
                alert.setContentText("Please insert folder name");
                alert.show();
            });

        }
    }
}
