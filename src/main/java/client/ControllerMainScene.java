package client;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;
import messages.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ControllerMainScene implements MessageProcessor {
    public ListView clientViewField;
    public ListView serverViewField;
    public Button downloadButton;
    public Button uploadButton;
    public Button changeDir;
    public Button changeServerDir;


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

//    public List<FileInfo> getClientFiles() throws IOException {
//        return Files.list(networkService.getBaseDir())
//                .map(FileInfo::new)
//                .collect(Collectors.toList());
//    }

    public void launch () throws  IOException{
        fillView(getFileNames(),clientViewField);
        networkService.sendMessage(new FilesListRequest());

        clientViewField.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                String file = (String) clientViewField.getSelectionModel().getSelectedItem();
                Path path = networkService.getBaseDir().resolve(file);
                if (Files.isDirectory(path)) {
                    networkService.setBaseDir(path);
                    fillView(getFileNames(), clientViewField);
                }
            }
        });

        serverViewField.setOnMouseClicked(e ->{
            if (e.getClickCount() == 2) {
                String file = (String) serverViewField.getSelectionModel().getSelectedItem();
                try {
                    networkService.sendMessage(new ChangeDir(file));
                } catch (IOException exception){
                    exception.printStackTrace();
                }
            }
        });

    }

    @Override
    public void processMessage(AbstractMessage message){
        switch (message.getMessageType()){
            case FILES_LIST:
                FilesList files = (FilesList) message;
                Platform.runLater(() -> fillView(files.getFiles(),serverViewField));
                break;
            case FILE:
                FileMessage file = (FileMessage) message;
                try {
                    Files.write(networkService.getBaseDir().resolve(file.getFileName()), file.getBytes());
                }catch (IOException e){
                    e.printStackTrace();
                }
                Platform.runLater(()->fillView(getFileNames(), clientViewField));
                break;
        }
    }

    private void fillView(List<String> list, ListView view) {
       view.getItems().clear();
       view.getItems().addAll(list);
    }

    private void clickOnTheView (){

    }

    public void changeDirOneLevelUp(ActionEvent event) {
        if(!networkService.getBaseDir().getParent().equals(null)){
            networkService.setBaseDir(networkService.getBaseDir().getParent());
            fillView(getFileNames(), clientViewField);
        }
    }

    public void changeServerDirUp(ActionEvent event) {
        try{
            networkService.sendMessage(new ChangeDirUp());
        }catch (IOException e){
            e.printStackTrace();
        }

    }

    public void createNewFolderWindow(ActionEvent event) throws Exception {
        Stage additionalStage = new Stage();
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(this.getClass().getResource("newFolderName.fxml"));
        Parent parent = loader.load();
        NewFolderController controller = loader.getController();
        controller.setStage(additionalStage);
        controller.setNetworkService(networkService);
        Scene scene = new Scene(parent);
        additionalStage.setScene(scene);
        additionalStage.show();
    }


}
