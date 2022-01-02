package client;

import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;
import messages.*;

import java.io.IOException;
import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.Paths;


@Getter
public class NetworkService {
    private ObjectEncoderOutputStream os;
    private ObjectDecoderInputStream is;
    private Path baseDir;
    @Setter
    private Stage stage;


    public NetworkService(){
        try {
            Socket socket = new Socket("localhost", 8189);
            baseDir = Paths.get(System.getProperty("user.home"));
            os = new ObjectEncoderOutputStream(socket.getOutputStream());
            is = new ObjectDecoderInputStream(socket.getInputStream());
            Thread thread = new Thread(this::read);
            thread.setDaemon(true);
            thread.start();
        }catch (
                IOException e){
            e.printStackTrace();
        }
    }

    private void read() {
        try {
            while (true) {
                AbstractMessage message = (AbstractMessage) is.readObject();
                switch (message.getMessageType()) {
//                    case FILE:
//                        FileMessage fileMessage = (FileMessage) message;
//                        Files.write(
//                                baseDir.resolve(fileMessage.getFileName()),
//                                fileMessage.getBytes()
//                        );
//                        Platform.runLater(() -> fillClientView(getFileNames()));
//                        break;
//                    case FILES_LIST:
//                        FilesList files = (FilesList) message;
//                        Platform.runLater(() -> fillServerView(files.getFiles()));
//                        break;
                    case USER_CONFIRMATION:
                        UserConfirmation confirmation = (UserConfirmation) message;
                        checkConfirmationOfAuthorization(confirmation);
                        break;
//                    case REGISTRATION_CONFIRMED:
//                        RegistrationConfirmed regConf = (RegistrationConfirmed) message;
//                        isRegistrationConfirmed(regConf);
//                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
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
                    mainScene.setNetworkService(this);
                    mainScene.clientViewField.getItems().addAll(mainScene.getClientFiles());
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

    public void sendMessage (AbstractMessage message) throws IOException{
        os.writeObject(message);
    }





}
