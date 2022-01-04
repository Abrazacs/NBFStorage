package client;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import lombok.Getter;
import lombok.Setter;
import messages.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class ControllerMainScene implements MessageProcessor {
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

    public void launch () throws  IOException{
        fillView(getFileNames(),clientViewField);
        networkService.sendMessage(new FilesListRequest());
    }

    @Override
    public void processMessage(AbstractMessage message) {
        switch (message.getMessageType()){
            case FILES_LIST:
                FilesList files = (FilesList) message;
                Platform.runLater(() -> fillView(files.getFiles(),serverViewField));
        }
    }

    private void fillView(List<String> list, ListView view) {
       view.getItems().clear();
       view.getItems().addAll(list);
    }
}
