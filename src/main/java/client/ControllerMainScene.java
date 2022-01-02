package client;

import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import lombok.Getter;
import lombok.Setter;
import messages.FileMessage;
import messages.FileRequest;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

@Getter
public class ControllerMainScene {
    public ListView clientViewField;
    public ListView serverViewField;
    public Button downloadButton;
    public Button uploadButton;
    @Setter
    private NetworkService networkService;


    public void download(ActionEvent event) throws IOException {
        String file = (String) serverViewField.getSelectionModel().getSelectedItem();
        networkService.getOs().writeObject(new FileRequest(file));
    }

    public void upload(ActionEvent event) throws IOException{
        String file = (String) clientViewField.getSelectionModel().getSelectedItem();
        Path filePath = networkService.getBaseDir().resolve(file);
        networkService.getOs().writeObject(new FileMessage(filePath));
    }



    private List<String> getFileNames() {
        try {
            return Files.list(networkService.getBaseDir())
                    .map(p -> p.getFileName().toString())
                    .collect(Collectors.toList());
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public List<FileInfo> getClientFiles() throws IOException {
        return Files.list(networkService.getBaseDir())
                .map(FileInfo::new)
                .collect(Collectors.toList());
    }


}
