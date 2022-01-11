package client;

import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import messages.*;

import java.io.IOException;
import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.Paths;


@Getter @Slf4j
public class NetworkService {
    private ObjectEncoderOutputStream os;
    private ObjectDecoderInputStream is;
    @Setter
    private Path baseDir;
    @Setter
    private MessageProcessor msgProcessor;


    public NetworkService(MessageProcessor msgProcessor){
        try {
            Socket socket = new Socket("localhost", 8189);
            baseDir = Paths.get(System.getProperty("user.home"));
            this.msgProcessor = msgProcessor;
            os = new ObjectEncoderOutputStream(socket.getOutputStream());
            is = new ObjectDecoderInputStream(socket.getInputStream());
            Thread thread = new Thread(this::read);
            thread.setDaemon(true);
            thread.start();
        }catch (IOException e) {
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("No connection");
                alert.setContentText("Server is offline. Please try again later");
                alert.show();
            });
        }
    }

    private void read() {
        try {
            while (true) {
                AbstractMessage message = (AbstractMessage) is.readObject();
                msgProcessor.processMessage(message);
            }
        } catch (Exception e) {
            log.info("e=", e);
        }
    }


    public void sendMessage (AbstractMessage message) throws IOException{
        os.writeObject(message);
    }





}
