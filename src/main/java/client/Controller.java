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


public class Controller implements Initializable {


    public AnchorPane registrationForm;
    public AnchorPane loginScene;
    public AnchorPane mainScene;
    public TextField loginInputField;
    public PasswordField passwordInputField;
    public Button enterButton ;
    public Button registrationButton;
    public ListView<String> clientViewField;
    public ListView<String> serverViewField;
    public Button downloadButton;
    public Button uploadButton;
    public TextField regLoginField;
    public PasswordField regPasswordField;
    public Button regConfirmButton;
    private Stage stage;
    private Scene scene;
    private Path baseDir;
    private ObjectDecoderInputStream is;
    private ObjectEncoderOutputStream os;
    private User user;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            Socket socket = new Socket("localhost", 8189);
            os = new ObjectEncoderOutputStream(socket.getOutputStream());
            is = new ObjectDecoderInputStream(socket.getInputStream());
            Thread thread = new Thread(this::read);
            thread.setDaemon(true);
            thread.start();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    private void read() {
        try {
            while (true) {
                AbstractMessage message = (AbstractMessage) is.readObject();
                switch (message.getMessageType()) {
                    case FILE:
                        FileMessage fileMessage = (FileMessage) message;
                        Files.write(
                                baseDir.resolve(fileMessage.getFileName()),
                                fileMessage.getBytes()
                        );
                        Platform.runLater(() -> fillClientView(getFileNames()));
                        break;
                    case FILES_LIST:
                        FilesList files = (FilesList) message;
                        Platform.runLater(() -> fillServerView(files.getFiles()));
                        break;
                    case USER_CONFIRMATION:
                        UserConfirmation confirmation = (UserConfirmation) message;
                        checkConfirmationOfAuthorization(confirmation);
                        break;
                    case REGISTRATION_CONFIRMED:
                        RegistrationConfirmed regConf = (RegistrationConfirmed) message;
                        isRegistrationConfirmed(regConf);
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    private void fillClientView(List<String> list) {
        clientViewField.getItems().clear();
        clientViewField.getItems().addAll(list);
    }

    private void fillServerView(List<String> list) {
        serverViewField.getItems().clear();
        serverViewField.getItems().addAll(list);
    }

    private List<String> getFileNames() {
        try {
            return Files.list(baseDir)
                    .map(p -> p.getFileName().toString())
                    .collect(Collectors.toList());
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private List<FileInfo> getClientFiles() throws IOException {
        return Files.list(baseDir)
                .map(FileInfo::new)
                .collect(Collectors.toList());
    }


    public void login(ActionEvent event) throws IOException {
        if (loginInputField.getText().isEmpty() || passwordInputField.getText().isEmpty()) {
            return;
        }
        user = new User(loginInputField.getText(), passwordInputField.getText());
        os.writeObject(user);
    }

    private void checkConfirmationOfAuthorization(UserConfirmation confirmation){
        if(confirmation.isAuthorized()){
            baseDir = Paths.get(System.getProperty("user.home"));
            Platform.runLater(()->{
                try {
                    Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("mainScene.fxml")));
                    stage = (Stage) enterButton.getScene().getWindow();
                    scene = new Scene(root);
                    stage.setScene(scene);
                    stage.show();
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

    public void showRegistrationForm(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("registrationForm.fxml")));
        stage = (Stage)((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    public void signIn(ActionEvent event) throws IOException {
        String login = regLoginField.getText();
        String password = regPasswordField.getText();
        if (!login.isEmpty() && !password.isEmpty()){
           os.writeObject(new RegistrationRequest(login,password));
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

    private void isRegistrationConfirmed (RegistrationConfirmed confirmation){
        if(confirmation.isConfirmed()){
            Platform.runLater(()->{
                try {
                    Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("client.fxml")));
                    stage = (Stage) regConfirmButton.getScene().getWindow();
                    scene = new Scene(root);
                    stage.setScene(scene);
                    stage.show();
                }catch (IOException e){
                    e.printStackTrace();
                }
            });
        }else {
            Platform.runLater(()->{
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("ERROR");
                alert.setHeaderText("Registration has been failed. Please try again");
                alert.showAndWait();
            });
        }

    }

    public void download(ActionEvent event) throws IOException {
        String file = serverViewField.getSelectionModel().getSelectedItem();
        os.writeObject(new FileRequest(file));
    }

    public void upload(ActionEvent event) throws IOException{
        String file = clientViewField.getSelectionModel().getSelectedItem();
        Path filePath = baseDir.resolve(file);
        os.writeObject(new FileMessage(filePath));
    }
}
